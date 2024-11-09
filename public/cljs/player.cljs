(ns transcribble.player
  (:require [clojure.string :as str]
            [transcribble.listeners :as listeners]
            [transcribble.util :refer [log] :as util]))

(def MIN-SPEED 0.5)
(def MAX-SPEED 2.0)

(defn video-format? [file]
  (str/starts-with? (.-type file) "video/"))

(defn current-time [{:keys [el] :as _player}]
  (.-currentTime el))

(defn duration [{:keys [el] :as _player}]
  (.-duration el))

(defn speed [{:keys [el] :as _player}]
  (.-playbackRate el))

(defn play-pause! [{:keys [el events state] :as player}]
  (let [{:keys [status]} @state]
    (if (= :playing status)
      (let [time (current-time player)]
        (.pause el)
        (log "Paused at time:" time (util/time->ts time))
        (when-let [f (events :pause)]
          (f))
        (update player :state swap! assoc :status :paused))
      (let [time (current-time player)]
        (.play el)
        (log "Playing from time:" time (util/time->ts time))
        (when-let [f (events :play)]
          (f))
        (update player :state swap! assoc :status :playing)))))

(defn set-speed! [{:keys [el] :as player} speed]
  (let [speed (-> speed (max MIN-SPEED) (min MAX-SPEED))]
    (set! (.-playbackRate el) speed)
    (log "Set speed:" speed)
    player))

(defn speed-up! [player]
  (set-speed! player (+ (speed player) 0.1)))

(defn speed-down! [player]
  (set-speed! player (- (speed player) 0.1)))

(defn set-time! [{:keys [el] :as player} time]
  (let [time (-> time
                 (min (duration player))
                 (max 0))]
    (set! (.-currentTime el) time)
    time))

(defn skip-backwards! [player]
  (let [time (set-time! player (- (current-time player) 1.0))]
    (log "Skipped backwards to time:" time (util/time->ts time))
    player))

(defn skip-forwards! [player]
  (let [time (set-time! player (+ (current-time player) 1.0))]
    (log "Skipped forwards to time:" time (util/time->ts time))
    player))

(defn add-click-listeners! [{:keys [listeners controls] :as player}]
  (let [listeners'
        (->> [[:play-pause play-pause!]
              [:skip-backwards skip-backwards!]
              [:skip-forwards skip-forwards!]]
             (map (fn [[k handler]]
                    (listeners/add-event-listener! (controls k) "click"
                                                   (partial handler player))))
             doall
             (concat listeners))]
    (assoc player :listeners listeners')))

(defn key-listeners [player]
  {"Escape" (partial play-pause! player)
   "F1" (partial skip-backwards! player)
   "F2" (partial skip-forwards! player)
   "F3" (partial speed-down! player)
   "F4" (partial speed-up! player)})

(defn mk-player [{:keys [file] :as player}]
  (let [file-type (if (video-format? file) "video" "audio")
        el (js/document.createElement file-type)
        player (merge player {:el el
                              :state (atom {:status :paused})
                              :listeners []})]
    (set! (.-className el) (str file-type "-player"))
    (set! (.-src el) (js/window.URL.createObjectURL file))
    (add-click-listeners! player)))
