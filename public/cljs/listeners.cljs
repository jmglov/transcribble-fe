(ns transcribble.listeners
  (:require [clojure.string :as str]
            [transcribble.dom :as dom]
            [transcribble.util :as util :refer [log]]))

(defn translate-mod
  "Support using 'Mod' as a modifier key to map to `⌘` on Apple systems and
   `Control` on other systems. This matches the expected behavior of copy, paste,
   bold, etc."
  [key->listener]
  (->> key->listener
       (map (fn [[k v]]
              [(if (str/starts-with? k "Mod+")
                 (if (util/macos?)
                   (str/replace k "Mod+" "Meta+")
                   (str/replace k "Mod+" "Control+"))
                 k)
               v]))
       (into {})))

(defn matching-listener? [el ev-type listener]
  (and (= el (:element listener)) (= ev-type (:event-type listener))))

(defn add-event-listener!
  "Add an event listener to the element designated by `sel` (which can be
   either a CSS selector relative either to `js/document` or `parent` if
   specified, or a DOM element) of type `event-type` with handler function
   `handler`. Returns a map with keys `:element`, `:event-type`, `:timestamp`,
   and `:handler` in order to support removing listeners (useful for
   interactive development).

   Example:

     (add-event-listener! \"button#save\" \"click\" save-file!)
     ;; => {:element #object[HTMLButtonElement [object HTMLButtonElement]]
            :event-type \"change\"
            :timestamp 1731223730971
            :handler #object[Function]}"
  ([sel event-type handler]
   (add-event-listener! js/document sel event-type handler))
  ([parent sel event-type handler]
   (let [el (dom/get-el parent sel)
         listener {:element el
                   :event-type event-type
                   :timestamp (util/now)
                   :handler handler}]
     (.addEventListener el event-type handler)
     (log "Added event listener" listener)
     listener)))

(defn add-key-listeners!
  "Add a \"keydown\" event listener to the window to handle the keys
   specified in the `key->listener` map, which is a map of `KeyboardEvent.key`
   (see https://developer.mozilla.org/en-US/docs/Web/API/UI_Events/Keyboard_event_key_values)
   to handler function. You may use the special modifier key \"Mod\", which maps
   to `⌘` on Apple systems and `Control` on other systems. Returns a listener
   map as returned by `add-event-listener!`.

   Example:

     (add-key-listeners! {\"F1\" skip-backwards!
                          \"Alt+w\" copy-selection!
                          \"Mod+v\" paste!})
     ;; => {:element #object[Window [object Window]]
            :event-type \"keydown\"
            :timestamp 1731223730971
            :handler #object[Function]}"
  [key->listener]
  (let [key->listener (translate-mod key->listener)
        f (fn [ev]
            (when-let [listener (key->listener (.-key ev))]
              (log "Handling keydown event" ev)
              (.preventDefault ev)
              (listener)))]
    (add-event-listener! js/window "keydown" f)))

(defn remove-event-listener!
  "Remove an event listener of type `event-type` from the element designated by
   `sel` (which can be either a CSS selector relative either to `js/document` or
   `parent` if specified, or a DOM element) of type `event-type` from
   `listeners`, which is a list of listener maps as returned by
   `add-event-listener!`. Returns a list of the remaing listeners.

   Example:

     (remove-event-listener! js/window \"keydown\"
                             [{:element #object[HTMLButtonElement [object HTMLButtonElement]]
                               :event-type \"change\"
                               :timestamp 1731223730971
                               :handler #object[Function]}
                              {:element #object[Window [object Window]]
                               :event-type \"keydown\"
                               :timestamp 1731223730971
                               :handler #object[Function]}])
     ;; => [{:element #object[HTMLButtonElement [object HTMLButtonElement]]
             :event-type \"change\"
             :timestamp 1731223730971
             :handler #object[Function]}]"
  ([sel event-type listeners]
   (remove-event-listener! js/document sel event-type listeners))
  ([parent sel event-type listeners]
   (let [el (dom/get-el parent sel)
         matches? (partial matching-listener? el event-type)]
     (doseq [{:keys [handler] :as listener} (filter matches? listeners)]
       (.removeEventListener el event-type handler)
       (log "Removed event listener" listener))
     (remove matches? listeners))))
