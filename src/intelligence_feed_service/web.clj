(ns intelligence-feed-service.web
  (:require [io.pedestal.http :as http]))

(def api-interceptors
  [http/json-body])

(defn health
  [_request]
  (http/json-response {:status "ok"}))

(def routes
  #{["/health"
     :get (conj api-interceptors `health)
     :route-name ::health]})
