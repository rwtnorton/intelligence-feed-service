(ns intelligence-feed-service.main
  (:require [clojure.string :as str]
            [clojure.tools.cli :as cli :refer [parse-opts]]
            [intelligence-feed-service.system :as sys]
            [intelligence-feed-service.system.pedestal.env :as env]
            ;; [markets.web :as web]
            [reloaded.repl :refer [system
                                   init
                                   start
                                   stop
                                   go
                                   reset
                                   reset-all]])
  (:gen-class))

(declare cli-opts
         die!
         run-for-env
         usage
         usage!)

(defn -main
  [& args]
  (let [{:keys [options
                arguments
                summary
                errors]} (parse-opts args cli-opts)
        {:keys [config
                help
                env]}    options
        env'             (keyword env)]
    (when help
      (usage! summary))
    (when errors
      (die! summary errors))
    (prn :env env')
    (prn :config config)
    (apply (partial run-for-env env') [config])
    ;; TODO:  remove this
    (while true
      (prn :ohai)
      (Thread/sleep 2000))))

(defn- exit!
  [n]
  (System/exit n))

(defn die!
  [summary errors]
  (binding [*out* *err*]
    (doseq [e errors]
      (println e))
    (println (usage summary))
    (exit! 1)))

(defn usage!
  [summary]
  (println (usage summary))
  (exit! 0))

(defn usage
  [summary]
  (str/join \newline
            [""
             "Usage: java -jar intelligence-feed-service.jar [options]"
             ""
             "Options:"
             summary]))

(defn- parse-env
  [s]
  (when s
    (->> s
         (re-seq #"^\s*:?(\S+)\s*")
         first
         second
         keyword)))

(def cli-opts
  [["-h" "--help" "prints this usage text and exits"]
   ["-C" "--config CONFIG" "configuration EDN file/resource"
    :default "config.edn"]
   (let [envs (->> env/allowed-envs
                   (sort)
                   (mapv name))]
     ["-E" "--env ENV" (format "environment: %s" envs)
      :default "prod"
      :parse-fn parse-env
      :validate [#(get env/allowed-envs (keyword %))
                 (format "Must be one of: %s" envs)]])])

(defn- run-for-env
  [env & optionals]
  (let [args (into [env] optionals)]
    (reloaded.repl/set-init! #(apply sys/system args)))
  (go))
