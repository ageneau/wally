(ns wally.views
  (:require
   [re-frame.core :as re-frame]
   [re-com.core :as re-com]
   [wally.subs :as subs]
   [cljs-web3.core :as web3]
   ))


;; home

(defn home-title []
  (let [name (re-frame/subscribe [::subs/name])]
    [re-com/title
     :label "Stream ETH tokens over time"
     :level :level1]))

(defn link-to-about-page []
  [re-com/hyperlink-href
   :label "go to About Page"
   :href "#/about"])

(def networks [{:id :main :label "Main Ethereum Network"}
               {:id :ropsten :label "Ropsten Test Network"}
               {:id :rinkeby :label "Rinkeby Test Network"}])


(defn valid-dest? [dest]
  (web3/address? dest))

(def valid-form? valid-dest?)

(defn network-selector []
  (let [network @(re-frame/subscribe [:network/network])]
    [re-com/single-dropdown
     :choices   networks
     :model network
     :width "300px"
     :on-change #(re-frame/dispatch [:network/changed %])]))

(defn address-input []
  (let [dest @(re-frame/subscribe [:dest/address])
        status (when (valid-dest? dest) :success)]
   [re-com/input-text
    :model dest
    :status status
    :status-icon? true
    :status-tooltip "valid address"
    :width "300px"
    :placeholder "destination address"
    :on-change #(re-frame/dispatch [:dest/changed %])]))

(defn send-button []
  (let [dest @(re-frame/subscribe [:dest/address])
        valid? (valid-form? dest)]
    [re-com/button
     :label "Send funds"
     :disabled? (not valid?)
     :on-click #(re-frame/dispatch [:view/submit-clicked])]))

(defn account-label []
  (let [accounts @(re-frame/subscribe [::subs/accounts])
        account (first accounts)]
    (re-com/label :label (str "Account: " account))))

(defn home-panel []
  [re-com/v-box
   :gap "1em"
   :children [[home-title]
              [re-com/button
               :label    [:span "Connect to MetaMask " [:i {:style {:background-image "url('https://i.ibb.co/BsTBKsn/fox.png')"
                                                                    :background-size "contain"
                                                                    :display "inline-block"
                                                                    :width "36px"
                                                                    :height "36px"}}]]
               :on-click #(re-frame/dispatch [:login/metamask])
               :style    {:color            "white"
                          :background-color "#4d90fe"
                          :font-size        "22px"
                          :font-weight      "300"
                          :border           "none"
                          :border-radius    "0px"
                          :padding          "20px 26px"}]
              [network-selector]
              [account-label]
              [address-input]
              [send-button]
              [link-to-about-page]]])


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
