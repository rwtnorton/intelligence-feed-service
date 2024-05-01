(ns intelligence-feed-service.system.config
  (:require [clojure.java.io :as io]
            [config.core :refer [load-env]]))

(defn get-config
  ([src]
   (if (io/resource src)
     (load-env src)
     (throw (ex-info (format "bad config: %s" src)
                     {:src src}))))
  ([]
   (load-env)))
