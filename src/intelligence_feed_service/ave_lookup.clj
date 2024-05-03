(ns intelligence-feed-service.ave-lookup
  "Transforms a map into an Attribute-Value-Entity mapping."
  (:require [clojure.zip :as zip]
            [clojure.set :as set]))

(defn coll-zipper
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

(defn locations->leaf-locs
  [locs]
  (->> locs
       (filter (fn [loc]
                 (let [v (zip/node loc)]
                   (when (scalar? v)
                     ;; ignore non-scalars
                     (let [parent (zip/up loc)]
                       (if-not parent
                         true
                         ;; if parent is a map-entry, then true if this is a val
                         ;; but if this is a key, we should reject.
                         (let [pv (zip/node parent)]
                           (if-not (map-entry? pv)
                             true
                             (= v (val pv))))))))))))

;;
;; An `ave-lookup` is a three-tier map:
;;  Given a map (document) and token (document-index),
;;  - tier 1 : key-path (vector of keywords) mapping to ...
;;  - tier 2 : "scalar" value arrived at via key-path, mapping to ...
;;  - tier 3 : set of tokens into vector of maps in which the above occurred.
;;

(defn map->ave-lookup
  [m token]
  (let [z (coll-zipper m)
        locs (zipper->locations z)
        leaf-locs (locations->leaf-locs locs)]
    (reduce (fn [acc loc]
              (let [scalar-val (zip/node loc)
                    key-path (key-path-at loc)]
                (if (seq key-path)
                  (update-in acc [key-path scalar-val]
                             (fn [v]
                               (if (nil? v)
                                 #{token}
                                 (conj v token))))
                  acc)))
            {}
            leaf-locs)))

(defn merge-ave-lookups*
  [lookup1 lookup2]
  (reduce-kv (fn [acc key-path scalar-map]
               (update-in acc [key-path]
                          (fn [m]
                            (merge-with set/union scalar-map m))))
             lookup1
             lookup2))

(defn merge-ave-lookups
  [lookup1 & other-lookups]
  (reduce merge-ave-lookups*
          lookup1
          other-lookups))
