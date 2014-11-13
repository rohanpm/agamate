(ns agamate.model
  (:require [korma.core :refer :all]
            [agamate.pg :as pg]))

(defn- column-updater [key f]
  (fn [rec]
    (if-let [val (key rec)]
      (update-in rec [key] f)
      rec)))

(defn- prepare-json-column [key]
  (column-updater key pg/to-pg-json))

(defn- transform-json-column [key]
  (column-updater key pg/from-pg-json))

(defn- transform-array-column [key]
  (column-updater key pg/from-pg-array))

(defmacro defentity-versioned [name & body]
  "Defines an entity twice: with and without _latest suffix"
  `(do
     (defentity ~name ~@body)
     (defentity ~(symbol (str name "_latest")) ~@body)))

(defentity-versioned tests
  (prepare (prepare-json-column :definition))
  (transform (transform-json-column :definition)))

(defentity-versioned repos)

(defentity fetch_queue
  (belongs-to repos_latest {:fk :repos_id})
  (transform (transform-array-column :urls)))
