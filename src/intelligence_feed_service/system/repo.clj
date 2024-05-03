(ns intelligence-feed-service.system.repo
  (:require [com.stuartsierra.component :as component]
            [intelligence-feed-service.repo :as repo]))

(defrecord RepoSystem [importer
                       repo]
  component/Lifecycle
  (start [this]
    (let [underlying-importer (:importer importer)
          docs (.import! underlying-importer)
          repo (repo/new-documents-repo docs)]
      ;; (prn :found-docs-count (count docs))
      ;; (prn :found-docs-type (type docs))
      (assoc this :repo repo)))
  (stop [this]
    (assoc this :repo nil)))

(defn new-repo-system
  ([importer]
   (map->RepoSystem {:importer importer
                     :repo     nil}))
  ([]
   (map->RepoSystem {})))
