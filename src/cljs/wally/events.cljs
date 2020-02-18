(ns wally.events
  (:require
   [re-frame.core :as re-frame]
   [wally.db :as db]
   [cljs-web3.eth :as web3-eth]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(def interceptors [re-frame/trim-v])
(def web3 (js/Web3. js/window.ethereum))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
            db/default-db))

(re-frame/reg-event-db
 ::set-active-panel
 (fn-traced [db [_ active-panel]]
   (assoc db :active-panel active-panel)))


(re-frame/reg-event-fx
 ::error
 interceptors
 (fn-traced []
   ))


(re-frame/reg-event-fx
  ::load-accounts
  interceptors
  (fn-traced [_ []]
    {:web3/call {:web3 web3
                 :fns [{:fn web3-eth/accounts
                        :on-success [::accounts-loaded]
                        :on-error [::error]}]}}))

(re-frame/reg-event-fx
 ::accounts-loaded
 interceptors
 (fn-traced [{:keys [db]} [accounts]]
            {:db (assoc db :accounts accounts)
             :dispatch [::load-ether-balances accounts]}))


(re-frame/reg-event-fx
 ::load-ether-balances
 (fn [{:keys [db]} [_ addresses]]
   {:web3/get-balances {:web3 web3
                        :addresses (for [address addresses]
                                     {:id (str "balance-" address) ;; If you watch?, pass :id so you can stop watching later
                                      :address address
                                      :watch? true
                                      :on-success [::ether-balance-loaded address]
                                      :on-error [::error]})}}))

(re-frame/reg-event-fx
 ::ether-balance-loaded
 interceptors
 (fn [{:keys [:db]} [address balance]]
   {:db (assoc-in db [:balances address] (str balance))}))

(re-frame/reg-event-fx
 :network/changed
 interceptors
 (fn-traced [{:keys [db]} [network]]
   {:db (assoc db :network network)}))


(re-frame/reg-event-fx
 :dest/changed
 interceptors
 (fn-traced [{:keys [db]} [dest]]
   {:db (assoc db :dest dest)}))


(re-frame/reg-event-fx
 :login/metamask
 interceptors
 (fn-traced [{:keys [db]}]
   {:db (assoc-in db [:metamask :perm-requested] true)
    :init-w3 {:on-success [::logged-in]
              :on-failure [::login-error]}}))

(re-frame/reg-event-fx
 ::logged-in
 interceptors
 (fn-traced [{:keys [db]}]
            {:db (assoc db :connected true)
             :dispatch [::load-accounts]}))


(re-frame/reg-event-db
 ::login-error
 (fn-traced [db [_ reason]]
            (assoc db :connected false)))
