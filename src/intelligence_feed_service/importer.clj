(ns intelligence-feed-service.importer)

(defprotocol Importer
  (import! [this]))
