(ns wally.subs
  (:require
   [re-frame.core :as re-frame]))

(defn contract [db]
  (:contract db))

(defn accounts [db]
  (:accounts db))

(defn balance [db address]
  (get-in db [:balances address]))

(defn total-supply [db]
  (:total-supply db))

(defn token-balance [db address]
  (get-in db [:token-balances address]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::active-panel
 (fn [db _]
   (:active-panel db)))


(re-frame/reg-sub
 ::contract
 contract)

(re-frame/reg-sub
  ::accounts
  accounts)


(re-frame/reg-sub
 ::token-balance
 (fn [db [_ & args]]
   (apply token-balance db args)))

(re-frame/reg-sub
 ::balance
 (fn [db [_ & args]]
   (apply balance db args)))

(re-frame/reg-sub
 :network/network
 (fn [db]
   (:network db)))

(re-frame/reg-sub
 :dest/address
 (fn [db]
   (:dest db)))
