(ns agamate.db
  (:require [korma.db :refer :all]
            [korma.config :refer :all]))

(set! *warn-on-reflection* true)

(defdb agamate-db (postgres {:db "agamate-dev"
                             :user "agamate-dev"}))

;(set-delimiters "\"")

