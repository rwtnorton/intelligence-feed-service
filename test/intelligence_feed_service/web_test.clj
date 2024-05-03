(ns intelligence-feed-service.web-test
  (:require [intelligence-feed-service.web :as sut]
            [clojure.test :refer [deftest testing is]]
            [intelligence-feed-service.system :as service.system]
            [com.stuartsierra.component :as component]
            [io.pedestal.http :as http]
            [io.pedestal.http.route :as http.route]
            [io.pedestal.test :refer [response-for]]
            [cheshire.core :as json]
            [mockery.core :as mockery]))

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

(deftest find-document-by-id-test
  (testing "found"
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (with-system [sys (service.system/system :test)]
      (mockery/with-mocks [doc-repo-find-document-by-id
                           {:target :intelligence-feed-service.repo/find-document-by-id
                            :return (constantly {:id "the-document"})}]
        (let [service               (service-fn sys)
              {:keys [status body]} (response-for service
                                                  :get
                                                  (url-for ::sut/find-document-by-id
                                                           :path-params {:id 1}))]
          (is (= 200 status))
          (is (= {:id "the-document"}
                 (json/parse-string body true)))))))
  (testing "not found"
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (with-system [sys (service.system/system :test)]
      (mockery/with-mocks [doc-repo-find-document-by-id
                           {:target :intelligence-feed-service.repo/find-document-by-id
                            :return (constantly nil)}]
        (let [service               (service-fn sys)
              {:keys [status body]} (response-for service
                                                  :get
                                                  (url-for ::sut/find-document-by-id
                                                           :path-params {:id 42}))]
          (is (= 404 status))
          (is (= "" body)))))))

(deftest find-documents-test
  (testing "with no query param type"
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (with-system [sys (service.system/system :test)]
      (mockery/with-mocks [doc-repo-get-documents-by-type
                           {:target :intelligence-feed-service.repo/get-all-documents
                            :return (constantly [{:id "doc1"} {:id "doc2"} {:id "doc3"}])}]
        (let [service               (service-fn sys)
              {:keys [status body]} (response-for service
                                                  :get
                                                  (url-for ::sut/find-documents))]
          (is (= 200 status))
          (is (= [{:id "doc1"} {:id "doc2"} {:id "doc3"}]
                 (json/parse-string body true)))))))
  (testing "with query param type specified"
    (testing "found"
      #_{:clj-kondo/ignore [:unresolved-symbol]}
      (with-system [sys (service.system/system :test)]
        (mockery/with-mocks [doc-repo-get-documents-by-type
                             {:target :intelligence-feed-service.repo/get-documents-by-type
                              :return (constantly [{:id "doc2", :indicators [{:type "type1"}]}
                                                   {:id "doc3", :indicators [{:type "type1"}]}])}]
          (let [service               (service-fn sys)
                {:keys [status body]} (response-for service
                                                    :get
                                                    (url-for ::sut/find-documents
                                                             :query-params {:type "type1"}))]
            (is (= 200 status))
            (is (= [{:id "doc2", :indicators [{:type "type1"}]}
                    {:id "doc3", :indicators [{:type "type1"}]}]
                   (json/parse-string body true)))))))
    (testing "not found"
      #_{:clj-kondo/ignore [:unresolved-symbol]}
      (with-system [sys (service.system/system :test)]
        (mockery/with-mocks [doc-repo-get-documents-by-type
                             {:target :intelligence-feed-service.repo/get-documents-by-type
                              :return (constantly [])}]
          (let [service               (service-fn sys)
                {:keys [status body]} (response-for service
                                                    :get
                                                    (url-for ::sut/find-documents
                                                             :query-params {:type "unknown-type"}))]
            (is (= 200 status))
            (is (= []
                   (json/parse-string body true)))))))))

(deftest search-documents-test
  (testing "found"
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (with-system [sys (service.system/system :test)]
      (mockery/with-mocks [doc-repo-search-documents
                           {:target :intelligence-feed-service.repo/search-documents
                            :return (constantly [{:id "doc1", :author_name "author1"}
                                                 {:id "doc3", :author_name "author1"}])}]
        (let [service               (service-fn sys)
              {:keys [status body]} (response-for service
                                                  :post
                                                  (url-for ::sut/search-documents)
                                                  :headers {"Content-Type" "application/json"}
                                                  :body (json/generate-string {:field ["author_name"]
                                                                               :value "author1"}))]
          (is (= 200 status))
          (is (= [{:id "doc1", :author_name "author1"}
                  {:id "doc3", :author_name "author1"}]
                 (json/parse-string body true)))))))
  (testing "not found"
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (with-system [sys (service.system/system :test)]
      (mockery/with-mocks [doc-repo-search-documents
                           {:target :intelligence-feed-service.repo/search-documents
                            :return (constantly [])}]
        (let [service               (service-fn sys)
              {:keys [status body]} (response-for service
                                                  :post
                                                  (url-for ::sut/search-documents)
                                                  :headers {"Content-Type" "application/json"}
                                                  :body (json/generate-string {:field ["author_name"]
                                                                               :value "xyzzy"}))]
          (is (= 200 status))
          (is (= []
                 (json/parse-string body true)))))))
  (testing "called with bad body json"
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (with-system [sys (service.system/system :test)]
      (mockery/with-mocks [doc-repo-search-documents
                           {:target :intelligence-feed-service.repo/search-documents
                            :return (constantly [{:id 1}])}]
        (let [service               (service-fn sys)
              {:keys [status body]} (response-for service
                                                  :post
                                                  (url-for ::sut/search-documents)
                                                  :headers {"Content-Type" "application/json"}
                                                  :body (json/generate-string {:thingy "author_name"
                                                                               :needle "quux_the_great"}))]
          (is (= 400 status))
          (is (= "" body)))))))
