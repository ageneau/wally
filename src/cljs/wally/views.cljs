(ns wally.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [wally.subs :as subs]
   [cljs-web3.core :as web3]))


;; home

(defn home-title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :label "Stream DAI tokens over time"
     :level :level1]))

(defn link-to-about-page []
  [re-com/hyperlink-href
   :label "go to About Page"
   :href "#/about"])

(def networks [{:id :main :label "Main Ethereum Network"}
               {:id :ropsten :label "Ropsten Test Network"}
               {:id :rinkeby :label "Rinkeby Test Network"}])


(def valid-dest? web3/address?)
(def valid-amount? pos?)
(def valid-duration? pos?)

(defn valid-form? [form]
  (and (valid-duration? (:duration form))
       (valid-amount? (:amount form))
       (valid-dest? (:dest form))))


(defn recipient-input []
  (let [dest @(re-frame/subscribe [::subs/dest])
        status (when (valid-dest? dest) :success)]
    [re-com/input-text
     :model dest
     :status status
     :status-icon? true
     :status-tooltip "valid address"
     :width "400px"
     :placeholder "recipient address"
     :on-change #(re-frame/dispatch [:dest-changed %])]))

(defn amount-input []
  (let [amount @(re-frame/subscribe [::subs/amount])
        status (when (valid-amount? amount) :success)]
    [re-com/input-text
     :model amount
     :status status
     :status-icon? true
     :status-tooltip "valid amount"
     :width "300px"
     :placeholder "Amount to send"
     :on-change #(re-frame/dispatch [:amount-changed %])]))

(defn duration-input []
  (let [duration @(re-frame/subscribe [::subs/duration])
        status (when (valid-duration? duration) :success)]
    [re-com/input-text
     :model duration
     :status status
     :status-icon? true
     :status-tooltip "valid duration"
     :width "300px"
     :placeholder "Stream duration in minutes"
     :on-change #(re-frame/dispatch [:duration-changed %])]))

(defn send-button []
  (let [form @(re-frame/subscribe [::subs/form])
        valid? (valid-form? form)]
    [re-com/button
     :label "Stream funds"
     :disabled? (not valid?)
     :style {:color            "white"
             :background-color "#4d90fe"}
     :on-click #(re-frame/dispatch [:submit-clicked])]))

(defn account-label []
  (let [accounts @(re-frame/subscribe [::subs/accounts])
        account (first accounts)
        balance @(re-frame/subscribe [::subs/balance account])
        token-balance @(re-frame/subscribe [::subs/token-balance account])]
    [re-com/v-box
     :children [(re-com/label :label (str "Account: " account))
                (re-com/label :label (str "Balance: " (web3/from-wei balance :ether) " ETH"))]]))

(defn home-panel []
  [re-com/v-box
   :gap "1em"
   :style {:margin "30px"}
   :children [[home-title]
              [re-com/button
               :label    [:span "Connect to MetaMask " #_[:i {:style {:background-image "url('https://i.ibb.co/BsTBKsn/fox.png')"
                                                                    :background-size "contain"
                                                                    :display "inline-block"
                                                                    :width "36px"
                                                                    :height "36px"}}]]
               :on-click #(re-frame/dispatch [:login/metamask])
               :style    {:color            "white"
                          :background-color "#4d90fe"
                          ;; :font-size        "22px"
                          ;; :font-weight      "300"
                          ;; :border           "none"
                          ;; :border-radius    "0px"
                          ;; :padding          "20px 26px"
                          }]
              #_[network-selector]
              [account-label]
              [re-com/h-box
               :gap "1em"
               :children [[recipient-input]
                          [amount-input]]]
              [duration-input]
              [send-button]
              #_[link-to-about-page]]])


;; about

(defn about-title []
  [re-com/title
   :label "This is the About Page."
   :level :level1])

(defn link-to-home-page []
  [re-com/hyperlink-href
   :label "go to Home Page"
   :href "#/"])

(defn about-panel []
  [re-com/v-box
   :gap "1em"
   :children [[about-title]
              [link-to-home-page]]])


;; main

(defn- panels [panel-name]
  (case panel-name
    :home-panel [home-panel]
    :about-panel [about-panel]
    [:div]))

(defn show-panel [panel-name]
  [panels panel-name])

(defn main-panel []
  (let [active-panel (re-frame/subscribe [::subs/active-panel])]
    [re-com/v-box
     :height "100%"
     :children [[panels @active-panel]]]))
