(ns transcribble.fe
  (:require [clojure.string :as str]
            [transcribble.dom :as dom]
            [transcribble.formats :as formats]
            [transcribble.listeners :as listeners]
            [transcribble.storage :as storage]
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

(defn select-file [ev]
  (let [el (.-target ev)
        selected-file (first (.-files el))]
    (log "Handling event" {:event ev, :file selected-file})
    (storage/set-item! :last-file (.-name selected-file))))

(defn add-file-input-listeners! [listeners]
  (->> [["change" select-file]]
       (map (partial apply listeners/add-event-listener! "input#file-picker"))
       (concat listeners)))

(defn load-ui!
  ([]
   (load-ui! []))
  ([listeners]
   (update-supported-formats!)
   (->> listeners
        (add-file-input-listeners!))))

(comment

  (def ls [])

  (def ls (load-ui! ls))

  ls

  (do
    (def ls
      (->> ls
           (listeners/remove-event-listener! "input#file-picker" "change")))
    ls)
  ;; => ()

  (storage/get-all)
  ;; => ()
  ;; => ({:key :last-file, :value "Upstream-Johann_Hari.mp3", :timestamp 1730790689960, :index 0})
  ;; => ()
  ;; => ({:key "_", :value nil, :timestamp 1730784759185, :index 0} {:key :last-file, :value "CleanShot 2022-11-17 at 13.08.45.mp4", :timestamp 1730790293529, :index 1})

  (storage/remove-all!)
  ;; => nil
  ;; => nil


  )
