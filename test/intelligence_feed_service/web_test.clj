(ns intelligence-feed-service.web-test
  (:require [intelligence-feed-service.web :as sut]
            [clojure.test :refer [deftest testing is]]
            [intelligence-feed-service.system :as service.system]
            [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as http.route]
            [io.pedestal.test :refer [response-for]]
            [cheshire.core :as json]))

;;
;; Borrowed from http://pedestal.io/pedestal/0.7/guides/pedestal-with-component.html#_testing
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def
  ^{:doc "helper allows us to refer to routes by route-name"}
  url-for
  (-> sut/routes
      http.route/expand-routes
      http.route/url-for-routes))

(defn- service-fn
  "helper extracts the Pedestal ::http/service-fn from the started system"
  [system]
  (get-in system [:pedestal
                  :service
                  ::http/service-fn]))

(defmacro with-system
  "allows us to start/stop systems between test executions"
  [[bound-var binding-expr] & body]
  `(let [~bound-var (component/start ~binding-expr)]
     (try
       ~@body
       (finally
         (component/stop ~bound-var)))))

;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest health-test
  (testing "GET"
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (with-system [sys (service.system/system :test)]
      (let [service               (service-fn sys)
            {:keys [status body]} (response-for service
                                                :get
                                                (url-for ::sut/health))]
        (is (= 200 status))
        (is (= {"status" "ok"}
               (json/parse-string body)))))))
