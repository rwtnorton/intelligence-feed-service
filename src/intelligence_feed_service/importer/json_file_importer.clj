(ns intelligence-feed-service.importer.json-file-importer
  (:require [intelligence-feed-service.importer :as importer]
            [clojure.java.io :as io]
            [cheshire.core :as json]))

(defrecord JsonFileImporter [path data]
  importer/Importer
  (import! [this]
    (with-open [r (-> (io/resource path) io/reader)]
      (assoc this :data (json/parse-stream-strict r true)))))

(defn new-json-file-importer
  [path]
  (map->JsonFileImporter {:path path
                          :data nil}))
