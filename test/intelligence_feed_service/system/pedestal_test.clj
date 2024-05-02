(ns intelligence-feed-service.system.pedestal-test
  (:require [intelligence-feed-service.system.pedestal :as sut]
            [clojure.test :refer [deftest testing is]]
            [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]))

(deftest new-pedestal-system-test
  (testing "no args"
    (let [got (sut/new-pedestal-system)]
      (is (satisfies? component/Lifecycle got))))
  (testing "with args"
    (let [got (sut/new-pedestal-system {:env :the-env, :port :the-port})]
      (is (satisfies? component/Lifecycle got))
      (is (= :the-env (get-in got [:service-map :env])))
      (is (= :the-port (get-in got [:service-map ::http/port]))))))
