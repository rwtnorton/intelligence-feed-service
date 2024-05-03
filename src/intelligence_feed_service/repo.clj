(ns intelligence-feed-service.repo
  (:require [intelligence-feed-service.ave-lookup :as ave-lookup]))

(defrecord DocumentsRepo [documents
                          ave-lookup])

(defn new-documents-repo
  [docs]
  ;; without lookups:  235 MiB
  ;; with lookups:     935 MiB
  (let [lookup (if (seq docs)
                 (->> docs
                      (map-indexed (fn [i d] (ave-lookup/map->ave-lookup d i)))
                      (apply ave-lookup/merge-ave-lookups))
                 {})]
    (map->DocumentsRepo {:documents docs
                         :ave-lookup lookup})))

(defn find-document-by-id
  [{:keys [ave-lookup
           documents]
    :as   _repo}
   id]
  (let [doc-index (first (get-in ave-lookup [[:id] id]))]
    (when (int? doc-index)
      (nth documents doc-index nil))))

(defn get-all-documents
  [{:keys [documents] :as _repo}]
  documents)

(defn get-documents-by-type
  [{:keys [ave-lookup
           documents]
    :as   _repo}
   doc-type]
  (let [doc-indexes (sort (get-in ave-lookup [[:indicators :type] doc-type]))]
    (mapv (partial nth documents) doc-indexes)))

(comment
  (require '[intelligence-feed-service.importer.registry :as registry])
  (require '[intelligence-feed-service.importer :as importer])
  (require '[clojure.pprint :as pprint])
  (require '[clojure.java.io :as io])
  (registry/report-registered-methods)
  (def importer (importer/kind->importer {:kind :json-file-importer
                                          :args ["indicators.json"]}))
  (def docs (.import! importer))
  (def repo (new-documents-repo docs))
  (def lookups (vec (map-indexed (fn [i d] (ave-lookup/map->ave-lookup d i)) docs)))
  (doseq [[i lu] (->> (map-indexed vector lookups))]
    (let [ ;; body (with-out-str (clojure.pprint/pprint lu))
          filename (format "lookup-%d.edn" i)]
      (println filename) (flush)
      (pprint/pprint lu (clojure.java.io/writer filename))
      ;; (let [body (prn-str lu)]
      ;;   (spit filename body))
      ))
  (pprint/pprint (:ave-lookup repo) (io/writer "lookup-merged")))
