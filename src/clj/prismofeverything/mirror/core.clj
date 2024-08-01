(ns prismofeverything.mirror.core
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [prismofeverything.mirror.config :as config]
    [prismofeverything.mirror.env :refer [defaults]]

    ;; Edges       
    [kit.edge.server.undertow]
    [prismofeverything.mirror.web.handler]

    ;; Routes
    [prismofeverything.mirror.web.routes.api]
    
    [kit.edge.db.sql.conman] 
    [kit.edge.db.sql.migratus] 
    [kit.edge.db.postgres] 
    [prismofeverything.mirror.web.routes.pages] 
    [prismofeverything.mirror.web.routes.ws] 
    [kit.edge.utils.nrepl])
  (:gen-class))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
  (reify Thread$UncaughtExceptionHandler
    (uncaughtException [_ thread ex]
      (log/error {:what :uncaught-exception
                  :exception ex
                  :where (str "Uncaught exception on" (.getName thread))}))))

(defonce system (atom nil))

(defn stop-app []
  ((or (:stop defaults) (fn [])))
  (some-> (deref system) (ig/halt!))
  (shutdown-agents))

(defn start-app [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/prep)
       (ig/init)
       (reset! system))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main [& _]
  (start-app))
