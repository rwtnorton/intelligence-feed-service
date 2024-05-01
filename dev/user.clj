(ns user
  (:require [reloaded.repl :as rr :refer [go stop start]]
            [intelligence-feed-service.system :as service.system]))

(rr/set-init! (fn []
                (service.system/system :dev)))

(comment
  (go)
  (stop)
  ,)
