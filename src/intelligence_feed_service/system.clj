(ns intelligence-feed-service.system
  (:require [com.stuartsierra.component :as component]))

(defn system
  [env & args]
  (prn :env env :args args :lol component/system-map)
  :system)
