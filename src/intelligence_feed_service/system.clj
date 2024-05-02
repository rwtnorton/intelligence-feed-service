(ns intelligence-feed-service.system
  (:require [com.stuartsierra.component :as component]
            [intelligence-feed-service.importer.json-file-importer :as json.importer]
            [intelligence-feed-service.importer :as base.importer]
            [intelligence-feed-service.system.config :as config]
            [intelligence-feed-service.system.importer :as importer]
            [intelligence-feed-service.system.pedestal :as pedestal]
            [intelligence-feed-service.system.repo :as repo]
            [intelligence-feed-service.web :as web]
            [io.pedestal.http :as http]
            [taoensso.telemere :as logger]))

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

   :importer
   (component/using (importer/new-importer-system)
                    {:config :config})

   :repo
   (component/using (repo/new-repo-system)
                    {:importer :importer})

   :pedestal
   (component/using (pedestal/new-pedestal-system)
                    {:service-map :config
                     ;; :repo        :repo
                     })
   })

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
  (logger/log! {:level :debug, :id ::importer-dispatch-methods}
               (keys (methods base.importer/kind->importer)))
  (let [cfg     (config/get-config)
        sub-sys (sub-systems cfg env)
        sys     (apply component/system-map (mapcat into sub-sys))]
    (add-shutdown-hook env sys)
    sys))
