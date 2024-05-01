(ns intelligence-feed-service.system-test
  (:require [intelligence-feed-service.system :as sut]
            [clojure.test :refer [deftest testing is]]))

(deftest system-test
  (testing "happy path"
    (let [sys (sut/system :test)]
      (is (pos-int? (get-in sys [:config :server-port])))
      (is (= :test (get-in sys [:config :env]))))))
