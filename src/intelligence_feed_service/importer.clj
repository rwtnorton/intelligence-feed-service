(ns intelligence-feed-service.importer)

(defprotocol Importer
  (import! [this]))

(defmulti kind->importer :kind)

(defmethod kind->importer :default
  [{:keys [kind] :as m}]
  (throw (ex-info (format "unknown importer kind: %s" kind)
                  {:given m})))
