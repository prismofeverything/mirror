(ns prismofeverything.mirror.web.routes.ws
  (:require
   [reitit.ring.middleware.parameters :as parameters]
   [taoensso.sente :as sente]
   [taoensso.sente.server-adapters.undertow :refer [get-sch-adapter]]
   [integrant.core :as ig]))
   
(defn client-id [ring-req]
  (get-in ring-req [:params :client-id]))

(defmulti on-message :id)

(defmethod on-message :default
  [{:keys [id client-id ?data] :as message}]
  (println "on-message: id: " id "  client-id: " client-id " ?data: " ?data))

(defmethod on-message :guestbook/echo
  [{:keys [id client-id ?data send-fn] :as message}]
  (let [response "Hello from the server"]
    (send-fn client-id [id response])))

(defmethod on-message :guestbook/broadcast
  [{:keys [id client-id ?data send-fn connected-uids] :as message}]
  (let [response (str "Hello to everyone from the client " client-id)]
   (doseq [uid (:any @connected-uids)]
     (send-fn uid [id response]))))

(defonce worlds
  (atom {:worlds {}}))

(defn create-world
  [world-data])

;; (defmethod on-message :mirror/create
;;   [{:keys [id client-id ?data send-fn connected-uids] :as message}]
;;   (let [world-data (:create-world data) ;; we'll have dimensions/rules/states/etc....
;;         world (create-world world-data)]
;;     (swap! worlds assoc (:id world) world)
;;     (doseq [uid (:any @connected-uids)]
;;      (send-fn uid [id {:message :create-world :world-id (:id world)}]))))

;; (defmethod on-message :mirror/enter
;;   [{:keys [id client-id ?data send-fn connected-uids] :as message}]
;;   (let [world-id (:world-id data)
;;         world (get @worlds world-id)]
;;     (send-fn client-id [id world])))

;; (defmethod on-message :mirror/flip
;;   [{:keys [id client-id ?data send-fn connected-uids] :as message}])

;; (defmethod on-message :mirror/start
;;   [{:keys [id client-id ?data send-fn connected-uids] :as message}])

;; (defmethod on-message :mirror/stop
;;   [{:keys [id client-id ?data send-fn connected-uids] :as message}])

(defmethod ig/init-key :sente/connection
  [_ opts]
  (sente/make-channel-socket!
   (get-sch-adapter)
   {:packer :edn
    ;; :csrf-token-fn nil
    :user-id-fn client-id}))
    

(defn handle-message! [msg]
  ;; TODO - error handling
  (on-message msg))

(defmethod ig/init-key :sente/router
  [_ {:keys [connection] :as opts}]
  (sente/start-chsk-router! (:ch-recv connection) #'handle-message!))

(defmethod ig/halt-key! :sente/router
  [_ stop-fn]
  (when stop-fn (stop-fn)))

(defn route-data
  [opts]
  (merge
   opts
   {:middleware
    [ring.middleware.keyword-params/wrap-keyword-params
     ring.middleware.params/wrap-params]}))

(defn ws-routes [{:keys [connection] :as opts}]
  [["" {:get  (:ajax-get-or-ws-handshake-fn connection)
        :post (:ajax-post-fn connection)}]])

(derive :reitit.routes/ws :reitit/routes)

(defmethod ig/init-key :reitit.routes/ws
  [_ {:keys [base-path]
      :or   {base-path ""}
      :as   opts}]
  [base-path (route-data opts) (ws-routes opts)])
