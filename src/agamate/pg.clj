(ns agamate.pg
  (:require [cheshire.core :as json])
  (:import org.postgresql.util.PGobject))

(set! *warn-on-reflection* true)

(defn to-pg-json [value]
  (doto (PGobject.)
    (.setType "json")
    (.setValue (json/encode value))))

(defn from-pg-json [^PGobject object]
  (json/decode (.getValue object)))

(defn from-pg-array [^java.sql.Array object]
  (-> object
      (.getArray)
      (concat)))
