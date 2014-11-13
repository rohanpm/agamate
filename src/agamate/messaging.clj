(ns agamate.messaging
  (:require [immutant.messaging :refer :all]
            [clojure.tools.logging :as log]))

(def process-fetch-queue
  (delay (topic "process-fetch-queue")))

(defn publish-process-fetch-queue []
  (publish @process-fetch-queue nil))
