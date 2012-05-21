(ns cl-short.models.short_url
  (:require [redis.core :as redis])) 

; This came from http://thecomputersarewinning.com/post/clojure-heroku-noir-mongo/
(defn split-redis-url [url]
  "Parses redistogo url from heroku, eg. redis://username:password@my.host:6789"
  (let [matcher (re-matcher #"^.*://(.*?):(.*?)@(.*?):(\d+)/$" url)] ;; Setup the regex.
    (when (.find matcher) ;; Check if it matches.
      (let [config-map (zipmap [:match :user :password :host :port] (re-groups matcher))]
        (update-in config-map [:port] #(Integer/parseInt %))))))

(def redis-config (split-redis-url (get (System/getenv) "REDISTOGO_URL")))

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

