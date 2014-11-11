(ns agamate.api
  (:require [korma.core :as k]
            [korma.db :as db]
            [clojure.tools.logging :as log]))

(set! *warn-on-reflection* true)

(defn list-repos [request]
  (k/select "repos_latest"))

(defn create-repo [request]
  (let [inserted (k/insert "repos"
                           (k/values (:body-params request)))]
    (log/info "Repo created:" inserted)
    {:status 201 :body inserted}))
