(ns transcribble.fe
  (:require [clojure.string :as str]
            [transcribble.dom :as dom]
            [transcribble.formats :as formats]
            [transcribble.listeners :as listeners]
            [transcribble.player :as player]
            [transcribble.storage :as storage]
            [transcribble.util :as util :refer [log]]))

(defonce state (atom {}))

(def controls
  (->> [:play-pause
        :skip-backwards
        :skip-forwards
        :speed
        :player-time
        :reset]
       (map (fn [k] [k (dom/get-el (str ".button." (name k)))]))
       (into {})))

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

(defn set-video-size! []
  (when-let [el (dom/get-el "video")]
    (let [width (.-offsetWidth el)]
      (set! (.-height (.-style el)) (str (* width (/ 3 4)) "px")))))

(defn hide-file-input! []
  (dom/remove-class! ".topbar" "inputting")
  (dom/remove-class! ".input" "active")
  (dom/remove-class! ".file-input-outer" "ext-input-active")
  (dom/add-class! ".sbutton.time" "active")
  (dom/add-class! ".text-panel" "editing"))

(defn select-file! [ev]
  (let [el (.-target ev)
        file (first (.-files el))
        player (player/mk-player
                {:controls controls
                 :file file
                 :events {:play #(dom/add-class! ".button.play-pause" "playing")
                          :pause #(dom/remove-class! ".button.play-pause" "playing")}})
        key-listeners (player/key-listeners player)]
    (log "Handling event" {:event ev, :file file})
    (storage/set-item! :last-file (.-name file))
    (hide-file-input!)
    (.prepend (dom/get-el "div.textbox-container") (:el player))
    (set-video-size!)
    (swap! state #(-> %
                      (assoc :file file)
                      (assoc :player player)
                      (update :listeners concat
                              (:listeners player)
                              (listeners/add-key-listeners! key-listeners))))))

(defn add-file-input-listeners! [listeners]
  (->> [["change" select-file!]]
       (map (partial apply listeners/add-event-listener! "input#file-picker"))
       (concat listeners)))

(defn load-ui!
  ([]
   (load-ui! []))
  ([listeners]
   (update-supported-formats!)
   (->> listeners
        (add-file-input-listeners!)
        doall
        (swap! state assoc :listeners))))

(comment

  (load-ui!)

  (swap! state update :listeners
         (let [{:keys [player]} @state]
           (-> (player/add-click-listeners! controls)
               :listeners)))

  (->> (:listeners @state)
       (listeners/remove-event-listener! js/window "keydown")
       (swap! state assoc :listeners))

  (-> @state
      :listeners)

  (-> @state
      :player)
  ;; => nil
  ;; => nil
  ;; => 55.166667

  (-> @state
      :player
      player/speed)
  ;; => 0.5

  (-> @state
      :player
      (player/set-speed! 9.0))

  (do
    (doseq [{:keys [element event-type]} (:listeners @state)]
      (listeners/remove-event-listener! element event-type (:listeners @state)))
    (swap! state assoc :listeners []))

  (storage/get-all)
  ;; => ({:key :last-file, :value "CleanShot 2022-11-17 at 13.08.45.mp4", :timestamp 1731138466078, :index 0})

  )
