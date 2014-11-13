(ns agamate.resource)

(defn fetch-queue-resource-to-db [x]
  {:repos_id (:repo x)
   :commit_hash (:revision x)
   :ref (:ref x)})
