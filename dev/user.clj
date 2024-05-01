(ns user
  (:require [reloaded.repl :as rr]
            [intelligence-feed-service.system :as service.system]))

(rr/set-init! (fn []
                (service.system/system :dev)))

(comment
  (rr/go)
  (rr/stop)
  ,)
