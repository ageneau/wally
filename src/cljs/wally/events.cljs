(ns wally.events
  (:require
   [re-frame.core :as re-frame]
   [wally.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   [ajax.core :as ajax]
   ))

(def interceptors [re-frame/trim-v])


(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))

(re-frame/reg-event-fx                             ;; note the trailing -fx
 :handler-with-http                      ;; usage:  (dispatch [:handler-with-http])
 (fn [{:keys [db]} _]                    ;; the first param will be "world"
   {:db   (assoc db :show-twirly true)   ;; causes the twirly-waiting-dialog to show??
    :http-xhrio {:method          :get
                 :uri             "/contracts/Sablier.json"
                 :timeout         8000                                           ;; optional see API docs
                 :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                 :on-success      [:good-http-result]
                 :on-failure      [:bad-http-result]}}))

(re-frame/reg-event-db
 :good-http-result
 (fn [db [_ result]]
   (assoc db :success-http-result result)))

(re-frame/reg-event-fx
 ::error
 interceptors
 (fn []
   ))


#_(re-frame/reg-event-fx
  ::load-accounts
  interceptors
  (fn [_ []]
    {:web3/call {:web3 web3
                 :fns [{:fn web3-eth/accounts
                        :on-success [::accounts-loaded]
                        :on-error [::error]}]}}))

(re-frame/reg-event-fx
 ::accounts-loaded
 interceptors
 (fn [{:keys [:db]} [accounts]]
   {:db (assoc db :accounts accounts)}))


(re-frame/reg-event-fx
 :network/changed
 interceptors
 (fn [{:keys [:db]} [network]]
   {:db (assoc db :network network)}))


(re-frame/reg-event-fx
 :dest/changed
 interceptors
 (fn [{:keys [:db]} [dest]]
   {:db (assoc db :dest dest)}))
