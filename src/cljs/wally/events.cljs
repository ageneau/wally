(ns wally.events
  (:require
   [re-frame.core :as re-frame]
   [wally.db :as db]
   [wally.data :as data]
   [wally.wallet :as wallet]
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [day8.re-frame.tracing :refer-macros [fn-traced]]))

(def interceptors [re-frame/trim-v])
(def w3 (js/Web3. js/window.ethereum))
(def ^:const STREAM-START-DELTA 300)

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
    {:web3/call {:web3 w3
                 :fns [{:fn web3-eth/accounts
                        :on-success [::accounts-loaded]
                        :on-error [::error]}]}}))

(re-frame/reg-event-fx
 ::accounts-loaded
 interceptors
 (fn-traced [{:keys [db]} [accounts]]
            {:db (assoc db :accounts accounts)
             :dispatch-n [[::load-ether-balances accounts]
                          [::load-token-balances accounts]]}))


(re-frame/reg-event-fx
 ::load-ether-balances
 (fn [{:keys [db]} [_ addresses]]
   {:web3/get-balances {:web3 w3
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
 ::load-token-balances
 (fn [{:keys [:db]} [_ addresses]]
   {:web3/get-balances {:web3 w3
                        :addresses (for [address addresses]
                                     {:id (str "balance-" address) ;; If you watch?, pass :id so you can stop watching later
                                      :address address
                                      :instance (get-in data/app-data [:networks :rinkeby :tokens :testdai :address])
                                      :watch? true
                                      :on-success [::token-balance-loaded address]
                                      :on-error [::error]})}}))


(re-frame/reg-event-fx
 ::token-balance-loaded
 interceptors
 (fn [{:keys [:db]} [address balance]]
   {:db (assoc-in db [:token-balances address] (str balance))}))


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

(re-frame/reg-event-fx
 :dest-changed
 interceptors
 (fn-traced [{:keys [db]} [dest]]
            {:db (assoc-in db [:form :dest] dest)}))

(re-frame/reg-event-fx
 :amount-changed
 interceptors
 (fn-traced [{:keys [db]} [amount]]
            {:db (assoc-in db [:form :amount] amount)}))

(re-frame/reg-event-fx
 :duration-changed
 interceptors
 (fn-traced [{:keys [db]} [duration]]
            {:db (assoc-in db [:form :duration] duration)}))


(defn- compute-deposit [amount duration]
  (let [amount (js/BigInt amount)
        duration (js/BigInt duration)
        q (unchecked-divide-int (unchecked-multiply-int (js/BigInt 1e18) amount) (unchecked-multiply-int (js/BigInt 60) duration))]
    (unchecked-multiply-int q (js/BigInt 60) duration)))

(re-frame/reg-event-fx
 :submit-clicked
 interceptors
 (fn-traced [{:keys [db]} _]
            {:approve-spender {:on-success [::spender-approved]
                               :on-failure [::error]
                               :token :testdai
                               :amount (compute-deposit (get-in db [:form :amount]) (get-in db [:form :duration]))}}))

(re-frame/reg-event-fx
 ::spender-approved
 interceptors
 (fn-traced [{:keys [db]} _]
            (let [time (wallet/epoch-time-seconds)
                  start-time (+ time STREAM-START-DELTA)
                  stop-time (+ start-time (* 60 (get-in db [:form :duration])))]
              {:create-stream {:on-success [::stream-created]
                               :on-failure [::error]
                               :recipient (get-in db [:form :dest])
                               :deposit (compute-deposit (get-in db [:form :amount]) (get-in db [:form :duration]))
                               :token :testdai
                               :start-time start-time
                               :stop-time stop-time}})))
