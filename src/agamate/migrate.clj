(ns agamate.migrate
  (:require [clojure.java.io :as io]
            [korma.core :refer :all]
            [korma.db :refer :all]
            [clojure.java.jdbc :as jdbc]
            [clojure.set :as set]
            [clojure.tools.logging :as log]
            [agamate.db :as db]))

(set! *warn-on-reflection* true)

(defn- migration-sql [name direction]
  (let [filename (format "migrations/%s.%s.sql" name direction)]
    (if-let [resource (io/resource filename)]
      (slurp resource)
      (throw (java.io.IOException. (str "Could not locate resource on classpath: " filename))))))

(defn- applied-migrations []
  (map :migration (exec-raw ["SELECT migration FROM migrations ORDER BY migration ASC"] :results)))

(defn- migrate-one [name & {:keys [suffix done-sql]}]
  (log/info "Begin migrate" suffix "-" name "...")
  (let [conn (get-connection db/agamate-db)]
    (jdbc/with-db-transaction [conn conn]
      (jdbc/execute! conn [(migration-sql name suffix)])
      (jdbc/execute! conn [done-sql name])))
  (log/info "Migrated" suffix name "OK!"))

(defn- migrate-up-one [name]
  (migrate-one name
               :suffix "up"
               :done-sql "INSERT INTO migrations(migration) VALUES(?)"))

(defn- migrate-down-one [name]
  (migrate-one name
               :suffix "down"
               :done-sql "DELETE FROM migrations WHERE migration=?"))

(defn- ensure-migration-table []
  (exec-raw
"CREATE TABLE IF NOT EXISTS migrations (
  migration varchar(200) NOT NULL PRIMARY KEY)"))

(defn ensure-migrations [migrations]
  (ensure-migration-table)
  (let [have    (set (applied-migrations))
        want    (set migrations)
        down    (set/difference have want)
        down    (reverse (sort down))
        up      (set/difference want have)
        up      (sort up)]
    (log/debug "migrations - have" have "want" want "down" down "up" up)
    (doseq [name down] (migrate-down-one name))
    (doseq [name up]   (migrate-up-one name))))
