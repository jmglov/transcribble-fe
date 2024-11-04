(ns transcribble.fe
  (:require [clojure.string :as str]
            [transcribble.dom :as dom]
            [transcribble.formats :as formats]
            [transcribble.util :as util :refer [log]]))

(defn formats->str [formats]
  (str "Your browser supports "
       (str/join " / " (:audio formats))
       " and "
       (str/join " / " (:video formats))
       " video files."))

(defn update-supported-formats! []
  (let [formats (formats/supported-formats)]
    (log "Supported formats:" formats)
    (dom/set-html! "#formats"
                   (formats->str formats))))

(defn load-ui! []
  (update-supported-formats!))

(comment

  (load-ui!)

  (dom/get-el "input")

  )
