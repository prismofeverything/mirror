(ns prismofeverything.mirror.env
  (:require
    [clojure.tools.logging :as log]
    [prismofeverything.mirror.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[mirror starting using the development or test profile]=-"))
   :start      (fn []
                 (log/info "\n-=[mirror started successfully using the development or test profile]=-"))
   :stop       (fn []
                 (log/info "\n-=[mirror has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile       :dev
                :persist-data? true}})
