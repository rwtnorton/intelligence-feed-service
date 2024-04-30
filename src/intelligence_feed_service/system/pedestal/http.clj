(ns intelligence-feed-service.system.pedestal.http
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [intelligence-feed-service.web :as web]
            [taoensso.telemere :as logger]))

(defmulti start-http (fn [m & _interceptors] (:env m)))

(def common-map
  {::http/type   :jetty
   ;; Routes can be a function that resolve routes,
   ;;  we can use this to set the routes to be reloadable
   ::http/routes #(route/expand-routes (deref #'web/routes))})

(def dev-map
  (merge common-map
         { ;; do not block thread that starts web server
          ::http/join?
          false

          ;; all origins are allowed in dev mode
          ::http/allowed-origins
          {:creds true :allowed-origins (constantly true)}

          ;; Content Security Policy (CSP) is mostly turned off in dev mode
          ::http/secure-headers
          {:content-security-policy-settings {:object-src "none"}}}))

(def non-dev-map common-map)

(defn- with-custom-interceptors
  [interceptors]
  (fn [service-map]
    (reduce (fn [acc f]
              (f acc))
            service-map
            interceptors)))

(defmethod start-http :dev
  "Starts a Pedestal server for development at a REPL."
  [service-map & interceptors]
  (logger/log! {:level :info,  :id ::start-http-dev}
               "Creating your [DEV] server...")
  (let [custom-interceptors (with-custom-interceptors interceptors)]
    (-> service-map
        (merge dev-map)
        http/default-interceptors
        http/dev-interceptors
        custom-interceptors
        http/create-server
        http/start)))

(defmethod start-http :test
  "Starts a Pedestal server for unit and integration testing."
  [service-map & interceptors]
  (logger/log! {:level :info, :id ::start-http-test}
               "Creating your [TEST] server...")
  (let [custom-interceptors (with-custom-interceptors interceptors)]
    (-> service-map
        (merge non-dev-map)
        http/default-interceptors
        custom-interceptors
        (http/create-server))))

(defmethod start-http :prod
  "Starts a Pedestal server appropriate for deployment via uberjar."
  [service-map & interceptors]
  (logger/log! {:level :info, :id ::start-http-prod}
               "Creating your [PROD] server...")
  (let [custom-interceptors (with-custom-interceptors interceptors)]
    (-> service-map
        (merge non-dev-map)
        http/default-interceptors
        custom-interceptors
        (http/create-server)
        (http/start))))

(defmethod start-http :default
  [{:keys [env] :as _service-map}]
  (throw (ex-info (format "Unknown env: '%s'" env)
                  {:env env})))
