(ns intelligence-feed-service.main
  (:gen-class))

(defn -main
  [& _args]
  (while true
    (prn :ohai)
    (Thread/sleep 2000)))
