(ns agamate.api
  (:require [korma.core :as k]
            [clojure.tools.logging :as log]
            [agamate
             [model :as model]
             [resource :as resource]
             [messaging :as messaging]]))

(set! *warn-on-reflection* true)

(defn extract-body-params [request]
  (:body-params request))

(defn extract-fetch-queue-params [request]
  (-> request
      (extract-body-params)
      (resource/fetch-queue-resource-to-db)))

(defn simple-lister [& {:keys [table]}]
  (fn [request]
    (k/select table)))

(defn simple-creator [& {:keys [table extract-params] :or {extract-params extract-body-params}}]
  (fn [request]
    (let [values (extract-params request)
          _      (log/debug "values for insert" values)
          inserted (k/insert table
                             (k/values values))]
      (log/info "inserted to" table inserted)
      {:status 201 :body inserted})))

(def list-repos (simple-lister :table model/repos_latest))
(def list-tests (simple-lister :table model/tests_latest))
(def create-repo (simple-creator :table model/repos))
(def create-test (simple-creator :table model/tests))

(def git-ensure-fetched* (simple-creator
                         :table model/fetch_queue
                         :extract-params extract-fetch-queue-params))

(defn git-ensure-fetched [request]
  (let [out (git-ensure-fetched* request)]
    (messaging/publish-process-fetch-queue)
    {:status 200}))
