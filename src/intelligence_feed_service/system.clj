(ns intelligence-feed-service.system
  (:require [com.stuartsierra.component :as component]
            [intelligence-feed-service.system.config :as config]
            [intelligence-feed-service.system.pedestal :as pedestal]
            [intelligence-feed-service.web :as web]
            [io.pedestal.http :as http]
            [taoensso.telemere :as logger]))

(defn- resolve-config
  [args]
  (if (seq args)
    (apply config/get-config args)
    (config/get-config)))

(defn- refine-config
  [cfg env]
  (let [port (get-in cfg [:web :port])]
    (-> cfg
        ;; Unconditonally force our given env.
        (assoc :env env)

        ;; Populate routes and port if not provided.
        (merge {::http/routes web/routes
                ::http/port port}))))

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
      (logger/log! {:level :info, :id ::system-shut-down} "Shutting down"))
    (component/stop sys)))

(defn- add-shutdown-hook
  [env sys]
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (down-fn env sys))))

(defn system
  [env & args]
  (prn :env env :args args :lol component/system-map)
  (let [cfg     (resolve-config args)
        sub-sys (sub-systems cfg env)
        sys     (apply component/system-map (mapcat into sub-sys))]
    (add-shutdown-hook env sys)
    sys))
