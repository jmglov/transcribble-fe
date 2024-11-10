(ns transcribble.dom)

(defn get-el
  "Returns the element resolved by the specified CSS selector. If `parent` is
   specified, the selector will be resolved against that element; otherwise
   js/document is used. If `sel` is not a string, it is assumed to be a DOM
   element and just returned."
  ([sel]
   (if (string? sel)
     (get-el js/document sel)
     sel))
  ([parent sel]
   (if (string? sel)
     (.querySelector parent sel)
     sel)))

(defn get-els
  "Returns all elements resolved by the specified CSS selector as a seq; see
  `get-el` for details on how the element is resolved. You may pass a parent
   element as the first argument."
  ([sel]
   (if (string? sel)
     (get-els js/document sel)
     sel))
  ([parent sel]
   (seq (.querySelectorAll parent sel))))

(defn add-class!
  "Adds a CSS class to the specified element or selector; see `get-el` for
   details on how the element is resolved. You may pass a parent element as the
   first argument. Returns the element."
  ([sel-or-el cls]
   (let [el (get-el sel-or-el)]
     (-> el (.-classList) (.add cls))
     el))
  ([parent sel cls]
   (add-class! (get-el parent sel) cls)))

(defn remove-class!
  "Removes a CSS class from the specified element or selector; see `get-el` for
   details on how the element is resolved. You may pass a parent element as the
   first argument. Returns the element."
  ([sel-or-el cls]
   (let [el (get-el sel-or-el)]
     (-> el (.-classList) (.remove cls))
     el))
  ([parent sel cls]
   (remove-class! (get-el parent sel) cls)))

(defn set-html!
  "Sets the inner HTML of the specified element or selector; see `get-el` for
   details on how the element is resolved. You may pass a parent element as the
   first argument. Returns the element."
  ([sel-or-el html]
   (set-html! js/document sel-or-el html))
  ([parent sel-or-el html]
   (let [el (if (string? sel-or-el)
              (get-el parent sel-or-el)
              sel-or-el)]
     (set! (.-innerHTML el) html)
     el)))

(defn set-style!
  "Sets the specified style to `v` on the specified element or selector; see
   `get-el` for details on how the element is resolved. You may pass a parent
   element as the first argument. Returns the element.

   Examples:

     (set-style! \"video\" :height \"100px\")
     ;; => #object[HTMLVideoElement [object HTMLVideoElement]]

     (set-style! (get-el \".topbar\") \"div.title\" :background \"#63b132\")
     ;; => #object[HTMLDivElement [object HTMLDivElement]]"
  ([sel-or-el style-name v]
   (set-style! js/document sel-or-el style-name v))
  ([parent sel-or-el style-name v]
   (let [el (get-el parent sel-or-el)]
     (aset (-> el (.-style)) (name style-name) v)
     el)))
