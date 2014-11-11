(ns agamate.ring
  (:require [clojure.tools.logging :as log])
  (:import java.sql.SQLException))

(set! *warn-on-reflection* true)

(defn- method-str [request]
  (->> request
      (:request-method)
      (str)
      (rest)
      ^java.lang.String (apply str)
      (.toUpperCase)))

(defn- request-str [request]
  (str (method-str request) " " (:uri request)))

(defn wrap-logging [handler]
  (fn [request]
    (log/trace "request:" request)
    (log/debug (request-str request))
    (handler request)))

(defn wrap-defaults [handler]
  (fn [request]
    (merge {:status 200 :headers {}} (handler request))))

(defn- return-or-rethrow [^SQLException e]
  {:status 400 :body {:error (-> (.getMessage e)
                                 (clojure.string/split #"\n")
                                 (first))}})

(defn wrap-db-error [handler]
  (fn [request]
    (try
      (handler request)
      (catch SQLException e
        (let [code (.getErrorCode e)
              msg  (.getMessage e)]
          (log/warn "during" (request-str request) ":" msg)
          (return-or-rethrow e))))))
