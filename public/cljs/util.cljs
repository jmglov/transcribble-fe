(ns transcribble.util)

(defn log
  ([msg]
   (js/console.log msg)
   nil)
  ([msg obj & objs]
   (apply js/console.log
          (->> [msg obj]
               (concat objs)
               (map clj->js)))
   obj))

(comment

  (log "foo")

  (log "some stuff"
       {:a "b"}
       ["c" "d"]
       #{:e :f}
       (js/document.createElement "audio"))

  )
