(ns wally.macros
  (:require [clojure.java.io :as io]))

;; https://clojureverse.org/t/best-practices-for-importing-raw-text-files-into-clojurescript-projects/2569
(defmacro inline-resource [resource-path]
  (slurp (clojure.java.io/resource resource-path)))
