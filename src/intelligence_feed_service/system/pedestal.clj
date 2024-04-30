(ns intelligence-feed-service.system.pedestal
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.interceptor :as interceptor]
            [intelligence-feed-service.system.pedestal.env :as service.env]
            [intelligence-feed-service.system.pedestal.http :as service.http]
            [taoensso.telemere :as logger]))

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
            s (service.http/start-http m d)]
        (logger/log! {:level :info, :id ::pedestal-connect}
                     (conn-msg m))
        (assoc this
               :service s
               :service-map m
               :repo r))))
  (stop [this]
    (when (and service
               (not (service.env/test? service-map)))
      (http/stop service))
    (logger/log! {:level :info, :id ::pedestal-disconnect}
                 (disconn-msg service-map))
    (assoc this :service nil)))

(defn new-pedestal
  ([{:keys [env port]}]
   (map->Pedestal {:service-map {:env env, ::http/port port}}))
  ([]
   (map->Pedestal {})))

(defn- conn-msg
  [{:keys [io.pedestal.http/port]}]
  (format ">>> Starting pedestal service on port :%s" port))

(defn- disconn-msg
  [{:keys [io.pedestal.http/port]}]
  (format ">>> Stopping pedestal service on port :%s" port))

(defn repo->interceptor
  [repo]
  (interceptor/interceptor
   {:name ::repo-interceptor
    :enter
    (fn [ctx]
      (update ctx :request assoc :repo repo))}))
