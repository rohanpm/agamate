(ns agamate.core
  (:require [immutant.web :as web]
            [clojure.tools.logging :as log]
            [korma.db :refer :all]
            [agamate
             [routes :as routes]
             [migrate :as migrate]
             [db :as db]])
  (:gen-class))

(set! *warn-on-reflection* true)

(defn init-db []
  (migrate/ensure-migrations ["20141020161221-create-repos"
                              "20141021161233-repos-constraint"
                              "20141022161239-create-tests"
                              "20141023163041-create-fetch-queue"]))

(defn -main
  [& args]
  (let [dev? (some #(= % "--dev") args)]
    (if dev?
      (do
        (log/info "Starting in development mode")
        (immutant.util/set-log-level! :DEBUG)))
    (init-db)
    (if dev?
      (web/run-dmc routes/api)
      (web/run routes/api))))
