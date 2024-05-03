(ns intelligence-feed-service.web
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as http.body-params]
            [intelligence-feed-service.repo :as doc.repo]))

(def api-interceptors
  [http/json-body])

(defn not-found
  []
  {:status 404})

(defn bad-request
  []
  {:status 400})

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

(defn- valid-search-field?
  [v]
  (and (sequential? v)
       (seq v)
       (every? string? v)))

(defn search-documents
  [{:keys [repo json-params] :as _request}]
  (prn :req-keys (-> _request keys sort)) (flush)
  (let [{:keys [field value]} json-params]
    (prn :field field :value value)
    (if (or (not (valid-search-field? field))
            (nil? value))
      (bad-request)
      (let [key-path        (mapv keyword field)
            documents-repo  (:repo repo)]
        (-> documents-repo
            (doc.repo/search-documents key-path value)
            http/json-response)))))

;; (def search-documents-handler
;;   {:name ::search-documents-handler
;;    :leave (fn [context]
;;             (let [{:keys [request]} context]
;;               (prn :req-keys (-> request keys sort))
;;               (prn :json-params (:json-params request))
;;               (flush)
;;               (assoc context
;;                      :response {:status 200
;;                                 :headers {"Content-Type" "application/json"}
;;                                 :body {:foo :butts}})))})

(def routes
  #{["/health"
     :get (conj api-interceptors `health)
     :route-name ::health]
    ["/search"
     :post [(http.body-params/body-params) search-documents]
     :route-name ::search-documents]
    ["/indicators/:id"
     :get (conj api-interceptors `find-document-by-id)
     :route-name ::find-document-by-id]
    ["/indicators"
     :get (conj api-interceptors `find-documents)
     :route-name ::find-documents]
    ;; ["/indicators/search"
    ;;  :post (conj api-interceptors `search-documents)
    ;;  :route-name ::search-documents]
    })
