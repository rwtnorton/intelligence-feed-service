(ns intelligence-feed-service.system.importer
  (:require [com.stuartsierra.component :as component]
            [intelligence-feed-service.importer :as importer]))

(defrecord ImporterSystem [config
                           importer]
  component/Lifecycle
  (start [this]
    (let [import-map (:importer config)
          importer   (importer/kind->importer import-map)]
      (assoc this :importer importer)))
  (stop [this]
    (assoc this :importer nil)))

(defn new-importer-system
  ([config]
   (map->ImporterSystem {:config   config
                         :importer nil}))
  ([]
   (map->ImporterSystem {})))
