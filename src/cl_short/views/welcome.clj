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

(defn get-short-url [{:keys [long-url]}]
  (redis/with-server {:host "127.0.0.1" :port 6379 :db 0}
    (do
      (if (nil? (redis/get long-url))
        (let [next-val
              (if (nil? (redis/get "next-val"))
                (redis/set "next-val" 1)
                (redis/incr "next-val"))
              full-url (str "http://localhost:8080/" next-val)]
          (redis/set full-url long-url)
          (redis/set long-url full-url)
          full-url))
        (redis/get long-url))))

(defpage [:post "/short"] {:as long-url}
  (common/layout
    (let [short-url (get-short-url long-url)]
      [:p "Short URL is: "
         [:a {:href short-url} short-url]])))
