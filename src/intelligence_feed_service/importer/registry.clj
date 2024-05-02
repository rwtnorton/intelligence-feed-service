(ns intelligence-feed-service.importer.registry
  "This is a convenient approach to ensuring all importers are required without
   needing to do so manually.

  Rationale:  In order to use a multimethod with defmulti's spread across different
              namespaces, those namespaces need to be loaded beforehand.
              By adding a new participant of `kind->importer` in the `require`
              list here, all participants should be honored.
              Then, usage should just be `require`-ing this registry.
  "
  (:require [intelligence-feed-service.importer :as base.importer]
            [intelligence-feed-service.importer.json-file-importer :as json.importer]
            [taoensso.telemere :as logger]))

(defn report-registered-methods
  []
  (logger/log! {:level :info, :id ::importer-dispatch-methods}
               (keys (methods base.importer/kind->importer))))
