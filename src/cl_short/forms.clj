(ns cl-short.forms
  (:use [noir.core :only [defpartial]]
        [hiccup.form-helpers :only [label text-field]]))

(defpartial long-url-fields [{:keys [long-url]}]
  (label "long-url" "Long URL: ")
  (text-field "long-url" long-url))

