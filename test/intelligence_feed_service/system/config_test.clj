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
           (mockery/with-mocks [io-resource-mock
                                {:target :clojure.java.io/resource
                                 :return :totally-a-resource}
                                load-env-mock
                                {:target :config.core/load-env
                                 :return (constantly
                                          [{:foo :bar} {:baz :quux}])}]
             (config/get-config :some-resource)))))
  (testing "when given a bad resource"
    (is (thrown-with-msg?
         ExceptionInfo
         #"^bad config: \S+$"
         #_{:clj-kondo/ignore [:unresolved-symbol]}
         (mockery/with-mocks [io-resource-mock
                              {:target :clojure.java.io/resource
                               :return (constantly nil)}]
           (config/get-config :some-thing-else)))))
  (testing "without mocks"
    (is (pos-int? (get-in (config/get-config) [:server-port])))))
