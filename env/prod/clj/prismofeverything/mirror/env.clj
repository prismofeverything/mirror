(ns prismofeverything.mirror.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[mirror starting]=-"))
   :start      (fn []
                 (log/info "\n-=[mirror started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[mirror has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
