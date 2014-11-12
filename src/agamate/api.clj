(ns agamate.api
  (:require [korma.core :as k]
            [korma.db :as db]
            [clojure.tools.logging :as log]
            [agamate.model :as model]))

(set! *warn-on-reflection* true)

(defn simple-lister [& {:keys [table]}]
  (fn [request]
    (k/select table)))

(defn simple-creator [& {:keys [table]}]
  (fn [request]
    (let [values (:body-params request)
          _      (log/debug "values for insert" values)
          inserted (k/insert table
                             (k/values (:body-params request)))]
      (log/info "inserted to" table inserted)
      {:status 201 :body inserted})))

(def list-repos (simple-lister :table model/repos_latest))
(def list-tests (simple-lister :table model/tests_latest))
(def create-repo (simple-creator :table model/repos))
(def create-test (simple-creator :table model/tests))
