(ns intelligence-feed-service.web
  (:require [io.pedestal.http :as http]))

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
  (let [{:keys [id]}        path-params
        documents-repo      (:repo repo)
        {:keys [ave-lookup
                documents]} documents-repo
        doc-index           (first (get-in ave-lookup [[:id] id]))
        doc                 (when (int? doc-index)
                              (nth documents doc-index nil))]
    (if-not doc
      (not-found)
      (http/json-response doc))))

(def routes
  #{["/health"
     :get (conj api-interceptors `health)
     :route-name ::health]
    ["/indicators/:id"
     :get (conj api-interceptors `find-document-by-id)
     :route-name ::find-document-by-id]})
