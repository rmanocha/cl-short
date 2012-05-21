(ns cl-short.views.welcome
  (:require [cl-short.views.common :as common]
            [noir.content.getting-started]
            [redis.core :as redis]
            [cl-short.forms :as forms])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]
        [noir.response :as rsp]
        hiccup.form-helpers))

; This came from http://thecomputersarewinning.com/post/clojure-heroku-noir-mongo/
(defn split-redis-url [url]
  "Parses redistogo url from heroku, eg. redis://username:password@my.host:6789"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (let [config-map (zipmap [:match :user :password :host :port] (re-groups matcher))]
        (update-in config-map [:port] #(Integer/parseInt %))))))

(def redis-config (split-redis-url (get (System/getenv) "REDISTOGO_URL")))

(defpage "/short" {:as long-url}
         (common/layout
           (form-to [:post "/short"]
                    (forms/long-url-fields long-url)
                    (submit-button "Shorten URL"))))

(defpage "/:url-key" {:keys [url-key]}
  (redis/with-server redis-config
    (do
      (if-let [long-url (redis/get (str "http://localhost:8080/" url-key))]
        (rsp/redirect long-url)))))

(defn get-short-url [{:keys [long-url]}]
  (redis/with-server redis-config
    (do
      (if-let [surl (redis/get long-url)]
        surl
        (let [next-val
              (if (nil? (redis/get "next-val"))
                (redis/set "next-val" 1)
                (redis/incr "next-val"))
              full-url (str "http://localhost:8080/" next-val)]
          (redis/set full-url long-url)
          (redis/set long-url full-url)
          full-url)))))

(defpage [:post "/short"] {:as long-url}
  (common/layout
    (let [short-url (get-short-url long-url)]
      [:p "Short URL is: "
         [:a {:href short-url} short-url]])))
