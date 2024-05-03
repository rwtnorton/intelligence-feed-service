(ns intelligence-feed-service.ave-lookup-test
  (:require [clojure.test :refer [deftest is testing]]
            [intelligence-feed-service.ave-lookup :as sut]
            [clojure.set :as set]
            [clojure.zip :as zip]))

;; (defn next-until=
;;   [target z]
;;   (->> z
;;        (iterate (fn [loc] (zip/next loc)))
;;        (take-while (fn [loc]
;;                      (or (not (zip/end? loc))
;;                          (not= target (zip/node loc)))))))

(deftest zipper->locations-test
  (testing "simple scalar"
    (is (= [42]
           (->> (sut/coll-zipper 42)
                sut/zipper->locations
                (mapv zip/node)))))
  (testing "simple vector"
    (is (= [[1 2 3] 1 2 3]
           (->> (sut/coll-zipper [1 2 3])
                sut/zipper->locations
                (mapv zip/node)))))
  (testing "simple map"
    (is (= [{:ichi 1, :ni 2}
            [:ichi 1]
            :ichi
            1
            [:ni 2]
            :ni
            2]
           (->> (sut/coll-zipper {:ichi 1, :ni 2})
                sut/zipper->locations
                (mapv zip/node)))))
  (testing "complex map"
    (is (= [{:ichi 1, :ni 2, :etc [3 4 5]}
            [:ichi 1]
            :ichi
            1
            [:ni 2]
            :ni
            2
            [:etc [3 4 5]]
            :etc
            [3 4 5]
            3
            4
            5]
           (->> (sut/coll-zipper {:ichi 1, :ni 2, :etc [3 4 5]})
                sut/zipper->locations
                (mapv zip/node))))))

(deftest ancestors-at-test
  (testing "simple scalar"
    (is (= [42]
           (->> (sut/coll-zipper 42)
                zip/next
                sut/ancestors-at
                (mapv zip/node)))))
  (testing "simple vector"
    (is (= [1 [1 2 3]]
           (->> (sut/coll-zipper [1 2 3])
                zip/next ;; at 1
                sut/ancestors-at
                (mapv zip/node)))))
  (testing "simple map"
    (let [loc (->> (sut/coll-zipper {:ichi 1, :ni 2})
                   zip/next ;; at [:ichi 1]
                   zip/next ;; at :ichi
                   zip/next ;; at 1
                   )]
      (is (= 1 (zip/node loc)))
      (is (= [1 [:ichi 1] {:ichi 1, :ni 2}]
             (->> loc
                  sut/ancestors-at
                  (mapv zip/node))))))
  (testing "deep nesting"
    (let [loc (->> (sut/coll-zipper {:nums {:ichi 1, :ni 2, :others [:san :shi]}})
                   zip/next     ;; at [:nums ...]
                   zip/next     ;; at :nums
                   zip/next     ;; at {:ichi 1 ...}
                   zip/next     ;; at [:ichi 1]
                   zip/rightmost ;; at [:others [:san :shi]]
                   zip/next      ;; at :others
                   zip/next      ;; at [:san :shi]
                   zip/next      ;; at :san
                   )]
      (is (= :san (zip/node loc)))
      (is (= [:san
              [:san :shi]
              [:others [:san :shi]]
              {:ichi 1, :ni 2, :others [:san :shi]}
              [:nums {:ichi 1, :ni 2, :others [:san :shi]}]
              {:nums {:ichi 1, :ni 2, :others [:san :shi]}}]
             (->> loc
                  sut/ancestors-at
                  (mapv zip/node)))))))

(deftest key-path-at-test
  (testing "simple scalar"
    (let [loc (->> (sut/coll-zipper 42)
                   zip/next)]
      (is (= 42 (zip/node loc)))
      (is (= []
             (->> loc
                  sut/key-path-at)))))
  (testing "simple vector"
    (let [loc (->> (sut/coll-zipper [1 2 3])
                   zip/next
                   zip/next)]
      (is (= 2 (zip/node loc)))
      (is (= []
             (->> loc
                  sut/key-path-at)))))
  (testing "simple map"
    (let [loc (->> (sut/coll-zipper {:ichi 1, :ni 2})
                   zip/next ;; at [:ichi 1]
                   zip/next ;; at :ichi
                   zip/next ;; at 1
                   )]
      (is (= 1 (zip/node loc)))
      (is (= [:ichi]
             (->> loc
                  sut/key-path-at)))))
  (testing "compound map"
    (let [loc (->> (sut/coll-zipper {:stuff {:nums {:ichi 1, :ni 2}}})
                   zip/next ;; at [:stuff ...]
                   zip/next ;; at :stuff
                   zip/next ;; at {:nums ...}
                   zip/next ;; at [:nums ...]
                   zip/next ;; at :nums
                   zip/next ;; at {:ichi 1, :ni 2}
                   zip/next ;; at [:ichi 1]
                   zip/next ;; at :ichi
                   zip/next ;; at 1
                   )]
      (is (= 1
             (zip/node loc)))
      (is (= [:stuff :nums :ichi]
               (->> loc
                    sut/key-path-at)))))
  (testing "compound map/vec jumble"
    (let [loc (->> (sut/coll-zipper {:stuff [{:nums [{:ichi 1, :ni 2}]}]})
                   zip/next ;; at [:stuff ...]
                   zip/next ;; at :stuff
                   zip/next ;; at [{:nums ...}]
                   zip/next ;; at {:nums ...}
                   zip/next ;; at [:nums ...]
                   zip/next ;; at :nums
                   zip/next ;; at [{:ichi 1, :ni 2}]
                   zip/next ;; at {:ichi 1, :ni 2}
                   zip/next ;; at [:ichi 1]
                   zip/next ;; at :ichi
                   zip/next ;; at 1
                   zip/next ;; at [:ni 2]
                   zip/next ;; at :ni
                   zip/next ;; at 2
                   )]
      (is (= 2 (zip/node loc)))
      (is (= [:stuff :nums :ni]
             (->> loc
                  sut/key-path-at)))))
  (testing "loc at vector element within map"
    (let [loc (->> (sut/coll-zipper {:foo {:tags ["bar" "baz"]}})
                   zip/next ;; at [:foo ...]
                   zip/next ;; at :foo
                   zip/next ;; at {:tags ["bar" "baz"]}
                   zip/next ;; at [:tags ...]
                   zip/next ;; at :tags
                   zip/next ;; at ["bar" "baz"]
                   zip/next ;; at "bar"
                   )]
      (is (= "bar" (zip/node loc)))
      (is (= [:foo :tags]
             (->> loc
                  sut/key-path-at))))))

(deftest scalar?-test
  (testing "vector"
    (is (not (sut/scalar? [1 2 3]))))
  (testing "map"
    (is (not (sut/scalar? {:ichi 1, :ni 2, :san 3}))))
  (testing "int"
    (is (sut/scalar? 1)))
  (testing "string"
    (is (sut/scalar? "foo")))
  (testing "nil"
    (is (sut/scalar? nil))))

(deftest locations->leaf-locs-test
  (testing "simple scalar"
    (let [locs (->> (sut/coll-zipper 42)
                    sut/zipper->locations)]
      (is (= [42]
             (->> locs
                  sut/locations->leaf-locs
                  (map zip/node))))))
  (testing "simple vector"
    (let [locs (->> (sut/coll-zipper [1 2 3])
                    sut/zipper->locations)]
      (is (= [1 2 3]
             (->> locs
                  sut/locations->leaf-locs
                  (map zip/node))))))
  (testing "simple map"
    (let [locs (->> (sut/coll-zipper {:ichi 1, :ni 2})
                    sut/zipper->locations)]
      (is (= [1 2]
             (->> locs
                  sut/locations->leaf-locs
                  (map zip/node))))))
  (testing "loc at vector element within map"
    (let [locs (->> (sut/coll-zipper {:foo {:tags ["bar" "baz"]}})
                    sut/zipper->locations)]
      (is (= ["bar" "baz"]
             (->> locs
                  sut/locations->leaf-locs
                  (map zip/node))))))
  (testing "trimmed-down doc"
    (let [doc {:description
               "Some active DiamondFox domains generated via DGA for 20180703.",
               :tags               ["DiamondFox" "DGA"],
               :revision           1,
               :extract_source     [],
               :name               "Active DiamondFox DGA(s) for 20180703",
               :public             1,
               :indicators
               [{:indicator   "1111111.net",
                 :description "desc-0",
                 :created     "2018-07-03T23:55:12",
                 :title       "title-0",
                 :content     "content-0",
                 :type        "domain",
                 :id          38808714}
                {:indicator   "11111111.net",
                 :description "desc-1",
                 :created     "2018-07-03T23:55:12",
                 :title       "title-1",
                 :content     "content-1",
                 :type        "domain",
                 :id          38808715}
                {:indicator   "16161616.com",
                 :description "desc-2",
                 :created     "2018-07-03T23:55:12",
                 :title       "title-2",
                 :content     "content-2",
                 :type        "domain",
                 :id          995691589}],
               :created            "2018-07-03T23:55:11.319000",
               :references         [],
               :modified           "2018-07-03T23:55:11.319000",
               :id                 "5b3c0cdf9a311967b65a5eb4",
               :targeted_countries [],
               :tlp                "green",
               :author_name        "MalwarePatrol",
               :adversary          "adversary-val",
               :more_indicators    false,
               :industries         []}
          locs (->> (sut/coll-zipper doc)
                    sut/zipper->locations)]
      (is (= #{
               "Some active DiamondFox domains generated via DGA for 20180703."
               "DiamondFox" "DGA"
               1
               "Active DiamondFox DGA(s) for 20180703"
               ;; indicators-0
               "1111111.net"
               "desc-0"
               "2018-07-03T23:55:12"
               "title-0"
               "content-0"
               "domain"
               38808714
               ;; indicators-1
               "11111111.net"
               "desc-1"
               "title-1"
               "content-1"
               38808715
               ;; indicators-2
               "16161616.com"
               "desc-2"
               "title-2"
               "content-2"
               995691589
               ;; end indicators
               "2018-07-03T23:55:11.319000"
               "5b3c0cdf9a311967b65a5eb4"
               "green"
               "MalwarePatrol"
               "adversary-val"
               false
               }
             (->> locs
                  sut/locations->leaf-locs
                  (map zip/node)
                  (into #{})))))))

(deftest merge-ave-lookups*-test
  (testing "empty maps"
    (is (= {}
           (sut/merge-ave-lookups* {} {}))))
  (testing "one empty map, one non-empty map"
    (is (= {:ichi 1}
           (sut/merge-ave-lookups* {:ichi 1} {}))
        "non-empty empty")
    (is (= {:ichi 1}
           (sut/merge-ave-lookups* {} {:ichi 1}))
        "empty non-empty"))
  (testing "non-empty map with itself"
    (is (= {[:ichi] {1 #{42}}}
           (sut/merge-ave-lookups* {[:ichi] {1 #{42}}}
                                   {[:ichi] {1 #{42}}}))))
  (testing "differing tier-1 keys"
    (is (= {[:ichi] {1 #{42}}
            [:one]  {1 #{42}}}
           (sut/merge-ave-lookups* {[:ichi] {1 #{42}}}
                                   {[:one] {1 #{42}}}))))
  (testing "same tier-1 keys, but differing tier-2 keys"
    (is (= {[:ichi] {1 #{42}
                     2 #{84}}}
           (sut/merge-ave-lookups* {[:ichi] {1 #{42}}}
                                   {[:ichi] {2 #{84}}}))))
  (testing "same tier-1 keys, same tier-2 keys, but differing tier-3 values"
    (is (= {[:ichi] {1 #{42 84}}}
           (sut/merge-ave-lookups* {[:ichi] {1 #{42}}}
                                   {[:ichi] {1 #{84}}}))))
  (testing "complex example"
    (is (= {[:ichi]      {1     #{42 84}
                          "one" #{21}}
            [:tags]      {"foo"  #{101 400}
                          "bar"  #{202}
                          "quux" #{9 11 13}}
            [:ichi :ban] {"best" #{1}}}
           (sut/merge-ave-lookups* {[:ichi] {1 #{42}}
                                    [:tags] {"foo" #{101}
                                             "bar" #{202}}}
                                   {[:ichi]      {1     #{84}
                                                  "one" #{21}}
                                    [:tags]      {"foo"  #{400}
                                                  "quux" #{9 11 13}}
                                    [:ichi :ban] {"best" #{1}}})))))

(deftest merge-ave-lookups-test
  (testing "with single arg"
    (is (= {:answer 42}
           (sut/merge-ave-lookups {:answer 42}))))
  (testing "with two differing args"
    (is (= {[:ichi]      {1     #{42 84}
                          "one" #{21}}
            [:tags]      {"foo"  #{101 400}
                          "bar"  #{202}
                          "quux" #{9 11 13}}
            [:ichi :ban] {"best" #{1}}}
           (sut/merge-ave-lookups {[:ichi] {1 #{42}}
                                   [:tags] {"foo" #{101}
                                            "bar" #{202}}}
                                  {[:ichi]      {1     #{84}
                                                 "one" #{21}}
                                   [:tags]      {"foo"  #{400}
                                                 "quux" #{9 11 13}}
                                   [:ichi :ban] {"best" #{1}}}))))
  (testing "with three differing args"
    (is (= {[:ichi]      {1     #{42 84}
                          "one" #{21}
                          171   #{171}}
            [:tags]      {"foo"       #{101 400}
                          "bar"       #{202}
                          "quux"      #{9 11 13}
                          "third-one" #{333}}
            [:ichi :ban] {"best" #{1}}
            [:bleh]      {69 #{0}}}
           (sut/merge-ave-lookups {[:ichi] {1 #{42}}
                                   [:tags] {"foo" #{101}
                                            "bar" #{202}}}
                                  {[:ichi]      {1     #{84}
                                                 "one" #{21}}
                                   [:tags]      {"foo"  #{400}
                                                 "quux" #{9 11 13}}
                                   [:ichi :ban] {"best" #{1}}}
                                  {[:ichi] {171 #{171}}
                                   [:tags] {"third-one" #{333}}
                                   [:bleh] {69 #{0}}})))))
