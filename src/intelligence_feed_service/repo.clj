(ns intelligence-feed-service.repo
  (:require [clojure.zip :as zip]
            [clojure.set :as set]))

(declare map->attr-lookup
         merge-attr-lookups)

(defrecord DocumentsRepo [documents
                          lookup-by-attr])

(defn new-documents-repo
  [docs]
  ;; without lookups:  235 MiB
  ;; with lookups:     635 MiB
  (let [lookup (mapv map->attr-lookup (take 100 docs))]
    (map->DocumentsRepo {:documents docs
                         :lookup-by-attr lookup})))

(defn- coll-zipper
  [v]
  (zip/zipper coll? seq (fn [_node kids] (vec kids)) v))

(defn zipper->locations
  [z]
  (->> z
       (iterate (fn [loc] (zip/next loc)))
       (take-while (complement zip/end?))))

(defn ancestors-at
  [loc]
  (->> loc
       (iterate (fn [x] (zip/up x)))
       (take-while (complement nil?))))

(defn key-path-at
  "Walks up from loc, returning vector of keys encountered."
  [loc]
  (->> (ancestors-at loc)
       (keep (fn [loc]
               (let [v (zip/node loc)]
                 (when (map-entry? v)
                   (key v)))))
       reverse
       vec))

(def scalar? (complement coll?))

(defn locations->scalar-map-val-locs
  [locs]
  (->> locs
       (filter (fn [loc]
                 (let [v (zip/node loc)]
                   (when (scalar? v)
                     (when-let [parent (zip/up loc)]
                       (let [pv (zip/node parent)]
                         (when (and (map-entry? pv)
                                    (= v (val pv)))
                           loc)))))))))

;;
;; An `attr-lookup` is a three-tier map:
;;  - tier 1 : key-path (vector of keywords) mapping to ...
;;  - tier 2 : "scalar" value arrived at via key-path, mapping to ...
;;  - tier 3 : set of maps in which the above occurred.
;;

(defn map->attr-lookup
  [m]
  (let [z (coll-zipper m)
        locs (zipper->locations z)
        scalar-map-val-locs (locations->scalar-map-val-locs locs)]
    (reduce (fn [acc loc]
              (let [scalar-val (zip/node loc)
                    key-path (key-path-at loc)]
                (if (seq key-path)
                  (update-in acc [key-path scalar-val]
                             (fn [v]
                               (if (nil? v)
                                 #{m}
                                 (conj v m))))
                  acc)))
            {}
            scalar-map-val-locs)))

(defn- merge-attr-lookups*
  [lookup1 lookup2]
  (reduce-kv (fn [acc key-path scalar-map]
               (update-in acc [key-path]
                          (fn [m]
                            (merge-with set/union scalar-map m))))
             lookup1
             lookup2))

(defn merge-attr-lookups
  [lookup1 & other-lookups]
  (reduce merge-attr-lookups*
          lookup1
          other-lookups))

(comment
  (require '[intelligence-feed-service.importer.registry :as registry])
  (require '[intelligence-feed-service.importer :as importer])
  (registry/report-registered-methods)
  (def importer (importer/kind->importer {:kind :json-file-importer
                                          :args ["indicators.json"]}))
  (def docs (.import! importer))
  (def lookups (mapv map->attr-lookup docs))
  (doseq [[i lu] (->> (map-indexed vector lookups) (take 2))]
    (let [;; body (with-out-str (clojure.pprint/pprint lu))
          filename (format "lookup-%d.edn" i)]
      (println filename) (flush)
      (clojure.pprint/pprint lu (clojure.java.io/writer filename))
      ;; (let [body (prn-str lu)]
      ;;   (spit filename body))
      ))
  ,)
