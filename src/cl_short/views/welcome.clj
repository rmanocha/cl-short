(ns cl-short.views.welcome
  (:require [cl-short.views.common :as common]
            [noir.content.getting-started]
            [redis.core :as redis]
            [cl-short.forms :as forms]
            [cl-short.models.short_url :as surl])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]
        [noir.response :only [redirect]]
        hiccup.form-helpers))

(defpage "/short" {:as long-url}
         (common/layout
           (form-to [:post "/short"]
                    (forms/long-url-fields long-url)
                    (submit-button "Shorten URL"))))

(defpage "/:url-key" {:keys [url-key]}
  (redis/with-server surl/redis-config
    (do
      (if-let [long-url (redis/get (str surl/BASE_IRI url-key))]
        (redirect long-url)))))

(defpage [:post "/short"] {:as long-url}
  (common/layout
    (let [short-url (surl/get-short-url long-url)]
      [:p "Short URL is: "
         [:a {:href short-url} short-url]])))
