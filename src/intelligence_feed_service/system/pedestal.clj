(ns intelligence-feed-service.system.pedestal
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [intelligence-feed-service.system.pedestal.env :as service.env]
            [intelligence-feed-service.system.pedestal.http :as service.http]))

(declare conn-msg
         disconn-msg
         repo->interceptor)

(defrecord Pedestal [service-map
                     service
                     repo]
  component/Lifecycle
  (start [this]
    (if service
      this
      (let [r (repo->interceptor repo)
            m service-map
            d #(update % ::http/interceptors conj r)
            s (do (println (conn-msg m)) (flush)
                  (service.http/start-http m d))]
        (assoc this
               :service s
               :service-map m
               :repo r))))
  (stop [this]
    (let [test? (service.env/test? service-map)]
      (when-not test?
        (do (println (disconn-msg service-map)) (flush)))
      (when (and service (not test?))
        (http/stop service)))
    (assoc this :service nil)))

(defn new-pedestal
  ([{:keys [env port]}]
   (map->Pedestal {:service-map {:env env, ::http/port port}}))
  ([]
   (map->Pedestal {})))

(defn- conn-msg
  [{:keys [io.pedestal.http/port]}]
  (format "\n>>> Starting pedestal service on port :%s" port))

(defn- disconn-msg
  [{:keys [io.pedestal.http/port]}]
  (format "\n>>> Stopping pedestal service on port :%s" port))

(defn repo->interceptor
  [repo]
  (interceptor/interceptor
   {:name ::repo-interceptor
    :enter
    (fn [ctx]
      (update ctx :request assoc :repo repo))}))
