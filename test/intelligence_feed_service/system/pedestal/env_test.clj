(ns intelligence-feed-service.system.pedestal.env-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest is]]
            [intelligence-feed-service.system.pedestal.env :as sut]))

(defonce service-map (zipmap [:ichi :ni :san] (map inc (range))))

(deftest allowed-envs
  (is (set/subset? #{:dev :test :prod}
                   sut/allowed-envs)))

(deftest dev?
  (is (sut/dev? {:env :dev}))
  (is (sut/dev? (assoc service-map :env :dev)))
  (is (not (sut/dev? {})))
  (is (not (sut/dev? {:env :test})))
  (is (not (sut/dev? {:env :prod}))))

(deftest test?
  (is (sut/test? {:env :test}))
  (is (sut/test? (assoc service-map :env :test)))
  (is (not (sut/test? {})))
  (is (not (sut/test? {:env :dev})))
  (is (not (sut/test? {:env :prod}))))

(deftest prod?
  (is (sut/prod? {:env :prod}))
  (is (sut/prod? (assoc service-map :env :prod)))
  (is (not (sut/prod? {})))
  (is (not (sut/prod? {:env :test})))
  (is (not (sut/prod? {:env :dev}))))
