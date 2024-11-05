(ns transcribble.listeners
  (:require [transcribble.dom :as dom]
            [transcribble.util :as util :refer [log]]))

(defn matching-listener? [el ev-type listener]
  (and (= el (:element listener)) (= ev-type (:event-type listener))))

(defn add-event-listener! [sel event-type handler]
  (let [el (dom/get-el sel)
        listener {:element el
                  :event-type event-type
                  :timestamp (util/now)
                  :handler handler}]
    (.addEventListener el event-type handler)
    (log "Added event listener" listener)))

(defn remove-event-listener! [sel ev-type listeners]
  (let [el (dom/get-el sel)
        matches? (partial matching-listener? el ev-type)]
    (doseq [{:keys [handler] :as listener} (filter matches? listeners)]
      (.removeEventListener el ev-type handler)
      (log "Removed event listener" listener))
    (remove matches? listeners)))
