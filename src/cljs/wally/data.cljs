(ns wally.data)

(def ^:const app-data
  {:sablier {:contracts ;; https://docs.sablier.finance/
             {:mainnet "0xA4fc358455Febe425536fd1878bE67FfDBDEC59a"
              :goerli "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"
              :kovan "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"
              :rinkeby "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"
              :ropsten "0xc04Ad234E01327b24a831e3718DBFcbE245904CC"}}
   :networks {:chain-id {"1" :mainnet "3" :ropsten "4" :rinkeby}
              :rinkeby {:tokens {:weenus {:address "0xaFF4481D10270F50f203E0763e2597776068CBc5"
                                          :symbol "WEENUS"}
                                 :fau {:address "0xfab46e002bbf0b4509813474841e0716e6730136"
                                       :symbol "FAU"}
                                 :testdai {:address "0xc3dbf84Abb494ce5199D5d4D815b10EC29529ff8"
                                           :symbol "DAI"}}}
              :ropsten {:tokens {:weenus {:address "0x101848D5C5bBca18E6b4431eEdF6B95E9ADF82FA"
                                          :symbol "WEENUS"}
                                 :fau {:address "0xfab46e002bbf0b4509813474841e0716e6730136"
                                       :symbol "FAU"}}}
              :mainnet {:tokens {:fau {:address "0xfab46e002bbf0b4509813474841e0716e6730136"
                                       :symbol "FAU"}}}}})
