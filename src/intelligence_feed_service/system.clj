(ns intelligence-feed-service.system
  (:require [com.stuartsierra.component :as component]
            [intelligence-feed-service.system.config :as config]
            [intelligence-feed-service.system.pedestal :as pedestal]
            [intelligence-feed-service.web :as web]
            [io.pedestal.http :as http]))

(defn- refine-config
  [cfg env]
  (let [port (get-in cfg [:server-port])]
    (-> cfg
        ;; Unconditonally force our given env.
        (assoc :env env)

        ;; Populate routes and port if not provided.
        (merge {::http/routes web/routes
                ::http/port   port}))))

(defn- sub-systems
  [cfg env]
  {:config (refine-config cfg env)

   :pedestal
   (component/using (pedestal/new-pedestal)
                    {:service-map :config
                     ;; :repo        :repo
                     })})

(defn- down-fn
  [env sys]
  (fn []
    (when-not (#{:test} env)
      (println "Shutting down")
      (flush))
    (component/stop-system sys)))

(defn- add-shutdown-hook
  [env sys]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (down-fn env sys))))

(defn system
  [env]
  (let [cfg     (config/get-config)
        sub-sys (sub-systems cfg env)
        sys     (apply component/system-map (mapcat into sub-sys))]
    (add-shutdown-hook env sys)
    sys))
