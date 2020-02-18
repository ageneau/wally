(ns wally.wallet
  (:require
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as eth])
  (:require-macros [wally.macros :refer [inline-resource]]))


(def ^:const contract (js/JSON.parse (inline-resource "public/contracts/Sablier.json")))

(defn init-w3 [cb]
  (if js/window.ethereum
    (.then (js/ethereum.enable)
           #(js/console.log "OK")
           #(js/console.log "ERR"))
    ))
