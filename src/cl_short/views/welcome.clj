(ns cl-short.views.welcome
  (:require [cl-short.views.common :as common]
            [noir.content.getting-started]
            [redis.core :as redis]
            [cl-short.forms :as forms])
  (:use [noir.core :only [defpage]]
        [hiccup.core :only [html]]
        [noir.response :as resp]
        hiccup.form-helpers))

(def redis-url (get (System/getenv) "REDIS_URL"))
(def redis-port (Integer/parseInt (get (System/getenv) "REDIS_PORT")))
(def redis-pass (get (System/getenv) "REDIS_PASS"))

(defpage "/short" {:as long-url}
         (common/layout
           (form-to [:post "/short"]
                    (forms/long-url-fields long-url)
                    (submit-button "Shorten URL"))))

(defpage "/:url-key" {:keys [url-key]}
  (redis/with-server {:host redis-url :port redis-port :password redis-pass}
    (do
      (if-let [long-url (redis/get (str "http://localhost:8080/" url-key))]
        (resp/redirect long-url)))))

(defn get-short-url [{:keys [long-url]}]
  (redis/with-server {:host redis-url :port redis-port :password redis-pass}
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
