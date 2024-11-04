(ns transcribble.storage
  (:require [clojure.edn :as edn]
            [transcribble.util :as util :refer [log]]))

(def storage-id "transcribbleStorage")

(defn mk-key [k]
  (str storage-id "_" k))

(defn set-item! [k v]
  (let [now (-> (js/Date.) (.getTime))
        k' (mk-key k)
        v {:value v, :timestamp now}]
    (try
      (js/localStorage.setItem k' (pr-str v))
      (log (str "storage/set-item! => set '" k "'") v)
      (catch js/Error e
        (log (str "storage/set-item! => error setting '" k "'") v e)))))

(defn get-item-with-metadata [k]
  (let [k' (mk-key k)]
    (if-let [v (js/localStorage.getItem k')]
      (edn/read-string v)
      (log (str "storage/get-item => no value for key '" k "'")))))

(defn get-item [k]
  (:value (get-item-with-metadata k)))

(comment

  (set-item! "foo" {:a 1, :b [2 3]})

  (get-item "foo")
  ;; => {:a 1, :b [2 3]}

  (get-item-with-metadata "foo")
  ;; => {:value {:a 1, :b [2 3]}, :timestamp 1730705547193}
  ;; => :yes

  (get-item "bar")
  ;; => nil
  ;; => nil

  )
