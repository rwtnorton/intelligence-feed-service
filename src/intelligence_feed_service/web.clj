(ns intelligence-feed-service.web
  (:require [io.pedestal.http :as http]
            [intelligence-feed-service.repo :as doc.repo]))

(def api-interceptors
  [http/json-body])

(defn not-found
  []
  {:status 404})

(defn health
  [_request]
  (http/json-response {:status "ok"}))

(defn find-document-by-id
  [{:keys [repo path-params] :as _request}]
  (let [documents-repo (:repo repo)
        {:keys [id]}   path-params
        doc            (doc.repo/find-document-by-id documents-repo id)]
    (if-not doc
      (not-found)
      (http/json-response doc))))

(defn find-documents
  [{:keys [repo query-params] :as _request}]
  (let [documents-repo (:repo repo)
        doc-type       (:type query-params)]
    (if-not doc-type
      (-> documents-repo
          doc.repo/get-all-documents
          http/json-response)
      (-> documents-repo
          (doc.repo/get-documents-by-type doc-type)
          http/json-response))))

(def routes
  #{["/health"
     :get (conj api-interceptors `health)
     :route-name ::health]
    ["/indicators/:id"
     :get (conj api-interceptors `find-document-by-id)
     :route-name ::find-document-by-id]
    ["/indicators"
     :get (conj api-interceptors `find-documents)
     :route-name ::find-documents]})
