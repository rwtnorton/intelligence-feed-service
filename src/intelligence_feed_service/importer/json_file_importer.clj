(ns intelligence-feed-service.importer.json-file-importer
  (:require [intelligence-feed-service.importer :as importer]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [taoensso.telemere :as logger]))

(defrecord JsonFileImporter [path]
  importer/Importer
  (import! [this]
    (try
      (logger/log! {:level :info, :id ::json-file-importer-import!}
                   (format "importing from file: %s" path))
      (with-open [r (-> (io/resource path) io/reader)]
        (json/parse-stream-strict r true))
      (catch IllegalArgumentException iae
        (throw (ex-info (format "invalid path: %s" path)
                        {:path path
                         :orig-ex iae}))))))

(defn new-json-file-importer
  [path]
  (map->JsonFileImporter {:path path}))

(defmethod importer/kind->importer :json-file-importer
  [{:keys [args]}]
  (let [path (first args)]
    (when-not path
      (throw (ex-info "missing required path"
                      {:args args})))
    (logger/log! {:level :info, :id ::kind->json-file-importer}
                 "dispatched to json-file-importer")
    (new-json-file-importer path)))
