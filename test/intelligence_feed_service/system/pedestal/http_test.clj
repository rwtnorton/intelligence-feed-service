(ns intelligence-feed-service.system.pedestal.http-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clojure.string :as str]
            [io.pedestal.http :as pedestal.http]
            [io.pedestal.http.route :as pedestal.route]
            [intelligence-feed-service.system.pedestal.http :as sut]
            [mockery.core :as mockery])
  (:import [clojure.lang ExceptionInfo]))

(defn with-http-mocks
  [f]
  #_{:clj-kondo/ignore [:unresolved-symbol]}
  (mockery/with-mocks [expand-routes-mock
                       {:target ::pedestal.route/expand-routes
                        :return :the-expanded-routes}
                       create-server-mock
                       {:target ::pedestal.http/create-server
                        :return :the-server}
                       start-mock
                       {:target ::pedestal.http/start
                        :return :http-started}]
    (f)))

(use-fixtures :each with-http-mocks)

(deftest start-http
  (testing "when the env is unknown"
    (is (thrown-with-msg? ExceptionInfo #"^Unknown env: '.*'$"
                          (sut/start-http {:env :foo}))))
  (testing "when the env is prod"
    (is (str/ends-with? (-> (sut/start-http {:env :prod})
                            with-out-str
                            str/trim-newline)
                        "Creating your [PROD] server...")))
  (testing "when the env is test"
    (is (str/ends-with? (-> (sut/start-http {:env :test})
                            with-out-str
                            str/trim-newline)
                        "Creating your [TEST] server...")))
  (testing "when the env is dev"
    (is (str/ends-with? (-> (sut/start-http {:env :dev})
                            with-out-str
                            str/trim-newline)
                        "Creating your [DEV] server..."))))
