(ns intelligence-feed-service.system.pedestal.env)

(def allowed-envs
  #{:dev
    :test
    :prod})

(defn dev?
  [{:keys [env] :as _service-map}]
  (= :dev env))

(defn test?
  [{:keys [env] :as _service-map}]
  (= :test env))

(defn prod?
  [{:keys [env] :as _service-map}]
  (= :prod env))
