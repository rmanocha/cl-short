(ns cl-short.views.welcome
  (:require [cl-short.views.common :as common]
            [noir.content.getting-started]
            [redis.core :as redis]
            [cl-short.forms :as forms])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]
        hiccup.form-helpers))

(defpage "/welcome" []
         (common/layout
           [:p "Welcome to cl-short"]))

(defpage "/short" {:as long-url}
         (common/layout
           (form-to [:post "/short"]
                    (forms/long-url-fields long-url)
                    (submit-button "Shorten URL"))))

(defpage [:post "/short"] {:as long-url}
  (common/layout
    [:p "Url Shortened"]))
