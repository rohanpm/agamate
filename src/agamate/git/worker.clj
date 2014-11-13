(ns agamate.git.worker
  (:require agamate.messaging
            immutant.messaging
            [immutant.messaging.pipeline :as pl]
            [immutant.scheduling :as sched]
            [clojure.tools.logging :as log]
            [korma.core :as k]
            [agamate
             [model :as model]])
  (:import org.eclipse.jgit.storage.file.FileRepositoryBuilder
           org.eclipse.jgit.lib.ObjectId
           org.eclipse.jgit.api.Git
           org.eclipse.jgit.transport.RefSpec
           org.eclipse.jgit.lib.Repository))

(set! *warn-on-reflection* true)

(defn- on-fetch-completed [fetch]
  (log/debug "fetch completed" fetch)
  (k/delete model/fetch_queue
            (k/where {:id (:id fetch)})))

(defn- on-fetch-failed [exception fetch]
  (log/warn exception "fetch failed" fetch)
  (k/update model/fetch_queue
            (k/where {:id (:id fetch)})
            (k/set-fields {:attempts (inc (:attempts fetch))})))

(defn- wrap-update-fetch-queue [f]
  (fn [fetch]
    (try
      (f fetch)
      (on-fetch-completed fetch)
      (catch Exception e
        (on-fetch-failed e fetch)))))

(defn- ^java.io.File repo-file [{:keys [slug]}]
  (clojure.java.io/file "git" slug))

(def ^{:private true} jgit-repo-create-lock)

(defn- ^Repository jgit-repo [record]
  (let [file (repo-file record)
        repo (-> (FileRepositoryBuilder.)
                 (.setBare)
                 (.setGitDir file)
                 (.build))]
    (if (not (.exists file))
      (locking jgit-repo-create-lock
        (if (not (.exists file))
          (.create repo true))))
    repo))

(defn- object-id [^java.lang.String str]
  (ObjectId/fromString str))

(defn- do-fetch* [{:keys [ref commit_hash canonical-url] :as record}]
  (let [repo  (jgit-repo record)
        id    (object-id commit_hash)
        have? #(.hasObject repo id)]
    (if (have?)
      (log/info "already have" id "in" repo)
      (do
        (log/info "fetch now" record "into" repo)
        (-> (Git. repo)
            (.fetch)
            (.setCheckFetchedObjects true)
            (.setRemote canonical-url)
            (.setRefSpecs (java.util.Collections/singletonList (RefSpec. (str "+" ref ":refs/heads/test-fetch"))))
            (.call))
        (if (not (have?))
          (throw (IllegalArgumentException.
                  (str "fetch of " ref " succeeded but did not retrieve " commit_hash))))))))

(def do-fetch (comp (constantly nil) (wrap-update-fetch-queue do-fetch*)))

(defn- pending-fetches [&_]
  (k/select model/fetch_queue
            (k/with model/repos_latest)
            (k/order :attempts :asc)
            (k/order :created :asc)))

(defonce fetch-all-pipeline
  (delay
   (pl/pipeline "git-fetch-all"
                (pl/step pending-fetches :fanout? true)
                (pl/step do-fetch :concurrency 4))))

(defn- on-process-fetch-queue [& _]
  ;(do-all-fetch)
  (@fetch-all-pipeline nil))

(defn start []
  (immutant.messaging/listen @agamate.messaging/process-fetch-queue on-process-fetch-queue)
  (sched/schedule agamate.messaging/publish-process-fetch-queue
                  {:in [10 :seconds]
                   :every [1 :hour]}))
