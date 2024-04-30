(ns intelligence-feed-service.system
  (:require [com.stuartsierra.component :as component]
            [intelligence-feed-service.system.config :as config]))

(defn- resolve-config
  [args]
  (if (seq args)
    (apply config/get-config args)
    (config/get-config)))

(defn- refine-config
  [cfg env]
  (-> cfg
      ;; Unconditonally force our given env.
      (assoc :env env)
      ;; Populate routes if not provided.
      ;; (merge {::http/routes web/routes})
      ))

(defn- sub-systems
  [cfg env]
  {:config (refine-config cfg env)})

(defn- down-fn
  [env sys]
  (fn []
    (when-not (#{:test} env)
      (println "Shutting down")
      (flush))
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
