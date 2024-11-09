(ns transcribble.dom)

(defn get-el
  ([sel-or-el]
   (if (string? sel-or-el)
     (get-el js/document sel-or-el)
     sel-or-el))
  ([parent sel]
   (.querySelector parent sel)))

(defn add-class!
  ([sel-or-el cls]
   (-> (get-el sel-or-el) (.-classList) (.add cls)))
  ([parent sel cls]
   (add-class! (get-el parent sel) cls)))

(defn remove-class!
  ([sel-or-el cls]
   (-> (get-el sel-or-el) (.-classList) (.remove cls)))
  ([parent sel cls]
   (remove-class! (get-el parent sel) cls)))

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
