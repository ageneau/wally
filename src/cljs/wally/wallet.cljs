(ns wally.wallet
  (:require
   [cljs-web3.core :as web3]
   [cljs-web3.eth :as web3-eth]
   [cljs-web3.net :as web3-net]
   [re-frame.core :as rf]
   [wally.data :as data])
  (:require-macros [wally.macros :refer [inline-resource]]))

(def ^:const sablier-abi (js/JSON.parse (inline-resource "public/contracts/Sablier.json")))
(def ^:const erc20-abi (js/JSON.parse (inline-resource "public/contracts/erc20.json")))
(def w3 (js/Web3. js/window.ethereum))


(rf/reg-fx
 :init-w3
 (fn [{:keys [on-success on-failure]}]
   (if js/window.ethereum
     (.then (js/ethereum.enable)
            #(rf/dispatch on-success)
            #(rf/dispatch (conj on-failure :user-denied-perm)))
     (rf/dispatch (conj on-failure :no-api)))))

(defn current-network []
  (get-in data/app-data [:networks :chain-id (web3/version-network w3)]))

(defn sablier-contract []
  (web3-eth/contract-at w3 sablier-abi (get-in data/app-data [:sablier :contracts :rinkeby])))

(defn erc20-contract [address]
  (web3-eth/contract-at w3 erc20-abi address))

(defn approve-spender [ct-address spender amount cb]
  (.approve (erc20-contract ct-address)
            spender
            amount
            cb))

(defn sablier-create-stream [recipient deposit token-address start-time stop-time cb]
  (.createStream (sablier-contract)
                 recipient
                 deposit
                 token-address
                 start-time
                 stop-time
                 cb))

(defn epoch-time-seconds []
  (js/Math.floor (/ (.getTime (js/Date.)) 1000)))

(defn handle-contract-error [err on-success on-failure]
  (if err
    (rf/dispatch (conj on-failure {:code (.-code err)
                                   :message (.-message err)}))
    (rf/dispatch on-success)))

(rf/reg-fx
 :approve-spender
 (fn [{:keys [on-success on-failure token amount]}]
   (let [net (current-network)]
     (approve-spender (get-in data/app-data [:networks net :tokens token :address])
                      (get-in data/app-data [:sablier :contracts net])
                      amount
                      #(handle-contract-error % on-success on-failure)))))

(rf/reg-fx
 :create-stream
 (fn [{:keys [on-success on-failure recipient deposit token start-time stop-time]}]
   (let [net (current-network)]
     (sablier-create-stream
      recipient
      deposit
      (get-in data/app-data [:networks net :tokens token :address])
      start-time
      stop-time
      #(handle-contract-error % on-success on-failure)))))

;; (approve-spender (get-in data/app-data [:networks (current-network) :tokens :testdai :address]) (get-in data/app-data [:sablier :contracts :rinkeby]) 0 #(js/console.log "Spender approved"))
;; (approve-spender (get-in data/app-data [:networks (current-network) :tokens :testdai :address]) (get-in data/app-data [:sablier :contracts :rinkeby]) 5e18 #(js/console.log "Spender approved"))
;; (let [time (epoch-time-seconds)] (sablier-create-stream "0x21696aDc72C227f5c05C1d5c4AcCD7b8DB5ac9a9" 5e18 (get-in data/app-data [:networks :rinkeby :tokens :testdai :address]) (+ time 60) (+ time 160) #(js/console.log "DONE")))
