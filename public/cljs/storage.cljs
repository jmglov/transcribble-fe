(ns transcribble.storage
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [transcribble.util :as util]))

(def storage-id "transcribbleStorage")
(def REMOVE-OLDEST-MAX-ATTEMPTS 10)

(defn log [f k msg & vs]
  (apply util/log
         (str "storage/" f " => " msg " for key " k)
         vs))

(defn mk-key [k]
  (let [k (name k)]
    (if (str/starts-with? k storage-id)
      k
      (str storage-id "_" k))))

(defn get-item-with-metadata [k]
  (let [k' (mk-key k)]
    (if-let [v (js/localStorage.getItem k')]
      (let [v' (edn/read-string v)]
        (log "get-item-with-metadata" k "got item" v')
        v')
      (log "get-item-with-metadata" k "no item"))))

(defn get-item [k]
  (:value (get-item-with-metadata k)))

(defn get-all []
  (->> (range js/localStorage.length)
       (map (fn [i]
              (let [k' (js/localStorage.key i)]
                (-> (get-item-with-metadata k')
                    (assoc :index i)
                    (update :timestamp #(or % 0))))))
       (sort-by :timestamp)))

(def get-first (comp first get-all))

(defn remove-item! [k]
  (let [k' (mk-key k)]
    (js/localStorage.removeItem k')
    (log "remove-item!" k "removed item")))

(defn set-item! [k v]
  (let [ts (util/now)
        k' (mk-key k)
        v {:key k, :value v, :timestamp ts}]
    (try
      (js/localStorage.setItem k' (pr-str v))
      (log "set-item!" k "set item" v)
      v
      (catch js/Error e
        (log "set-item!" k "error setting item" v e)))))

(defn remove-oldest!
  ([]
   (remove-oldest! 0))
  ([attempt-num]
   (when (>= attempt-num REMOVE-OLDEST-MAX-ATTEMPTS)
     (let [msg (str "storage/remove-oldest! => storage still full after "
                    attempt-num " attempts")]
       (util/log msg)
       (throw (ex-info msg {:transcribble/error ::full
                            ::attempts attempt-num}))))
   (let [num-to-remove (min 3 js/localStorage.length)]
     (util/log (str "storage/remove-oldest! => removing first "
                    num-to-remove " items"))
     (doseq [_i (range num-to-remove)
             :let [k (js/localStorage.key 0)]
             :when k]
       (remove-item! k)))
   (let [k (str "_test_" (util/now))
         v (set-item! k "_")]
     (if v
       (do
         (remove-item! k)
         (util/log (str "storage/remove-oldest! => storage cleared on "
                        "attempt " (inc attempt-num))))
       (do
         (util/log (str "storage/remove-oldest! => storage still full "
                        "after attempt " (inc attempt-num)))
         (recur (inc attempt-num)))))))

(defn remove-all! []
  (doseq [item (get-all)]
    (remove-item! (:key item))))
