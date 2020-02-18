(ns wally.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [wally.events :as events]
   [wally.routes :as routes]
   [wally.views :as views]
   [wally.config :as config]
   [wally.wallet]
   [day8.re-frame.http-fx]
   [district0x.re-frame.web3-fx]
   ))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (routes/app-routes)
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
