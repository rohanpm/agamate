(ns agamate.routes
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [agamate
             [api :as api]
             [ring :as ring]]
            ring.middleware.format))

(set! *warn-on-reflection* true)

(defn- unimplemented [request]
  {:status 500 :body "not yet implemented"})

(defroutes repo-api
  (GET "/repos" [] api/list-repos)
  (POST "/repos" [] api/create-repo))

(defroutes test-api
  (GET "/tests" [] api/list-tests)
  (POST "/tests" [] api/create-test))

(defroutes api*
  repo-api
  test-api
  (route/not-found nil))

(def api (-> api*
          (ring/wrap-logging)
          (ring/wrap-defaults)
          (ring/wrap-db-error)
          (ring.middleware.format/wrap-restful-format :formats [:json-kw :edn :yaml-kw :yaml-in-html :transit-msgpack :transit-json])))
