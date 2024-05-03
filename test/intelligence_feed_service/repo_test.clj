(ns intelligence-feed-service.repo-test
  (:require [clojure.test :refer [deftest testing is]]
            [intelligence-feed-service.repo :as sut]
            [mockery.core :as mockery]))

(deftest new-documents-repo-test
  (testing "happy path"
    #_{:clj-kondo/ignore [:unresolved-symbol]}
    (mockery/with-mocks [docs->ave-lookup
                         {:target :intelligence-feed-service.repo/docs->ave-lookup
                          :return (constantly :totally-a-lookup)}]
      (let [docs [{:ichi 1} {:ni 2}]
            got (sut/new-documents-repo docs)]
        (is (= docs (:documents got)))
        (is (= :totally-a-lookup
               (:ave-lookup got)))))))

(deftest find-document-by-id-test
  (let [docs   [{:id 1} {:id 2}]
        lookup {[:id] {1 #{0}
                       2 #{1}}}
        repo   (sut/map->DocumentsRepo {:documents docs
                                        :ave-lookup lookup})]
    (testing "found"
      (is (= (first docs)
             (sut/find-document-by-id repo 1)))
      (is (= (second docs)
             (sut/find-document-by-id repo 2))))
    (testing "not found"
      (is (nil? (sut/find-document-by-id repo 3))))))

(deftest get-all-documents-test
  (let [docs   [{:id 1} {:id 2}]
        lookup :meh
        repo   (sut/map->DocumentsRepo {:documents docs
                                        :ave-lookup lookup})]
    (is (= docs
           (sut/get-all-documents repo)))))

(deftest get-documents-by-type-test
  (let [docs   [{:id 1, :indicators [{:type "type1"}]}
                {:id 2, :indicators [{:type "type2"}]}
                {:id 3, :indicators [{:type "type3"} {:type "type1"}]}]
        lookup {[:id]               {1 #{0}
                                     2 #{1}
                                     3 #{2}}
                [:indicators :type] {"type1" #{0 2}
                                     "type2" #{1}
                                     "type3" #{2}}}
        repo   (sut/map->DocumentsRepo {:documents  docs
                                        :ave-lookup lookup})]
    (testing "found"
      (is (= [(nth docs 0)
              (nth docs 2)]
             (sut/get-documents-by-type repo "type1")))
      (is (= [(nth docs 1)]
             (sut/get-documents-by-type repo "type2")))
      (is (= [(nth docs 2)]
             (sut/get-documents-by-type repo "type3"))))
    (testing "not found"
      (is (= []
             (sut/get-documents-by-type repo "foo"))))))

(deftest search-documents-test
  (let [docs   [{:id 1, :author_name "author1"}
                {:id 2, :author_name "author2"}
                {:id 3, :author_name "author1"}]
        lookup {[:id]          {1 #{0}
                                2 #{1}
                                3 #{2}}
                [:author_name] {"author1" #{0 2}
                                "author2" #{1}}}
        repo   (sut/map->DocumentsRepo {:documents  docs
                                        :ave-lookup lookup})]
    (testing "found"
      (is (= [(nth docs 0)
              (nth docs 2)]
             (sut/search-documents repo [:author_name] "author1")))
      (is (= [(nth docs 1)]
             (sut/search-documents repo [:author_name] "author2"))))
    (testing "not found"
      (is (= []
             (sut/search-documents repo [:author_name] "foo"))))))
