(ns wally.wallet
  (:require
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as eth]
   [cljs-web3.net :as net]
   [re-frame.core :as rf]
   [wally.data :as data])
  (:require-macros [wally.macros :refer [inline-resource]]))

(def ^:const abi (js/JSON.parse (inline-resource "public/contracts/Sablier.json")))
(def web3 (js/Web3. js/window.ethereum))

(rf/reg-fx
 :init-w3
 (fn [{:keys [on-success on-failure]}]
   (if js/window.ethereum
     (.then (js/ethereum.enable)
            #(rf/dispatch on-success)
            #(rf/dispatch (conj on-failure :user-denied-perm)))
     (rf/dispatch (conj on-failure :no-api)))))
