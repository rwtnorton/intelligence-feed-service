(ns markets.system.pedestal.http-test
  (:require [clojure.test :refer :all]
            [io.pedestal.http :as pedestal.http]
            [io.pedestal.http.route :as pedestal.route]
            [markets.system.pedestal.http :as http]
            [mockery.core :as mockery])
  (:import [clojure.lang ExceptionInfo]))

(defn with-http-mocks
  [f]
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
                          (http/start-http {:env :foo}))))
  (testing "when the env is prod"
    (is (= "\nCreating your [PROD] server...\n"
           (with-out-str
             (http/start-http {:env :prod})))))
  (testing "when the env is test"
    (is (= "\nCreating your [TEST] server...\n"
           (with-out-str
             (http/start-http {:env :test})))))
  (testing "when the env is dev"
    (is (= "\nCreating your [DEV] server...\n"
           (with-out-str
             (http/start-http {:env :dev}))))))
