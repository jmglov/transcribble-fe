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

(defn now []
  (-> (js/Date.) (.getTime)))
