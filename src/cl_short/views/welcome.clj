(ns cl-short.views.welcome
  (:require [cl-short.views.common :as common]
            [noir.content.getting-started]
            [redis.core :as redis])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to cl-short"]))

(defpage "/short" []
         (common/layout
           [:p "Welcome to my page"]))
