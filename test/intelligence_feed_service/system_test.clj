(ns intelligence-feed-service.system-test
  (:require [intelligence-feed-service.system :as sut]
            [clojure.test :refer [deftest testing is]]))

(deftest system-test
  (testing "happy path"
    (let [sys (sut/system :test "config.edn")]
      (is (pos-int? (get-in sys [:config :web :port])))
      (is (= :test (get-in sys [:config :env]))))))
