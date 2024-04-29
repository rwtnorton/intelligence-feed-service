(ns intelligence-feed-service.system.config-test
  (:require [intelligence-feed-service.system.config :as config]
            [clojure.test :refer :all]
            [mockery.core :as mockery])
  (:import (clojure.lang ExceptionInfo)))

(deftest get-config
  (testing "when given a good resource"
    (is (= [{:foo :bar}
            {:baz :quux}]
           (mockery/with-mocks [slurp-mock
                                {:target :clojure.core/slurp
                                 :return (constantly
                                          "[{:foo :bar} {:baz :quux}]")}
                                io-resource-mock
                                {:target :clojure.java.io/resource
                                 :return (constantly :totally-a-resource)}]
             (config/get-config :some-resource)))))
  (testing "when given a bad resource"
    (is (thrown-with-msg?
         ExceptionInfo
         #"^bad config: \S+$"
         (mockery/with-mocks [io-resource-mock
                              {:target :clojure.java.io/resource
                               :return (constantly nil)}]
           (config/get-config :some-thing-else))))))
