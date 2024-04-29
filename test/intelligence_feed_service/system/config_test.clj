(ns intelligence-feed-service.system.config-test
  (:require [intelligence-feed-service.system.config :as config]
            [clojure.test :refer [deftest is testing]]
            [mockery.core :as mockery])
  (:import (clojure.lang ExceptionInfo)))

(deftest get-config
  (testing "when given a good resource"
    (is (= [{:foo :bar}
            {:baz :quux}]
           #_{:clj-kondo/ignore [:unresolved-symbol]}
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
         #_{:clj-kondo/ignore [:unresolved-symbol]}
         (mockery/with-mocks [io-resource-mock
                              {:target :clojure.java.io/resource
                               :return (constantly nil)}]
           (config/get-config :some-thing-else))))))
