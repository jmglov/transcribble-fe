(ns transcribble.dom)

(defn get-el
  ([sel]
   (get-el js/document sel))
  ([parent sel]
   (.querySelector parent sel)))

(defn set-html!
  ([sel-or-el html]
   (set-html! js/document sel-or-el html))
  ([parent sel-or-el html]
   (let [el (if (string? sel-or-el)
              (get-el parent sel-or-el)
              sel-or-el)]
     (set! (.-innerHTML el) html))))

(comment

  (set-html! "#formats" "some text")

  )
