(ns intelligence-feed-service.system.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn get-config
  ([src]
   (if-let [path (io/resource src)]
    (-> path
        (slurp)
        (edn/read-string))
    (throw (ex-info (format "bad config: %s" src)
                    {:src src}))))
  ([]
   (get-config "config.edn")))
