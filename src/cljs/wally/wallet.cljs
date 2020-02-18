(ns wally.wallet
  (:require
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as eth]
   [re-frame.core :as rf])
  (:require-macros [wally.macros :refer [inline-resource]]))

(def ^:const contract (js/JSON.parse (inline-resource "public/contracts/Sablier.json")))

(rf/reg-fx
 :init-w3
 (fn [{:keys [on-success on-failure]}]
   (if js/window.ethereum
     (.then (js/ethereum.enable)
            #(rf/dispatch on-success)
            #(rf/dispatch (conj on-failure :user-denied-perm)))
     (rf/dispatch (conj on-failure :no-api)))))
