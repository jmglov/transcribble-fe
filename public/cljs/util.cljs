(ns transcribble.util
  (:require [clojure.string :as str]))

(defn log
  "Converts `msg` to JavaScript, prints it to the JS console, and returns it. If
   one or more objects are passed following `msg`, converts `msg` and all the
   objects to JavaScript, prints them to the console, and returns the first
   object.

   Examples:

     (log \"This is a string\")
     ;; => \"This is a string\"

     (log \"This is a string\"
          #{:and :a :set}
          [:a :vector]
          {:and \"a\", :map \"too\"})
  ;; => #{:and :a :set}"
  ([msg]
   (js/console.log (clj->js msg))
   msg)
  ([msg obj & objs]
   (apply js/console.log
          (->> (concat [msg obj] objs)
               (map clj->js)))
   obj))

(defn platform
  "Returns the platform as a keyword. Note that this relies on a deprecated
   feature and should not be used for anything other than selecting the
   appropriate modifier key for a platform; see:
   https://developer.mozilla.org/en-US/docs/Web/API/Navigator/platform

   Example:

     (platform)
     ;; => :linux"
  []
  (let [platform (.-platform js/window.navigator)]
    (if (str/starts-with? platform "Mac")
      :macos
      (-> platform (str/split " ") first str/lower-case keyword))))

(defn macos?
  "Returns true if on an Apple platform. See the documentation for `platform` on
   deprecation."
  []
  (#{:macos :iphone :ipod :ipad} (platform)))

(defn now
  "Returns the number of milliseconds since midnight at the beginning of January
   1, 1970, UTC (the Unix Epoch)."
  []
  (-> (js/Date.) (.getTime)))

(defn time->ts
  "Given a playback position in a media clip in seconds, returns a timestamp
   string in the form \"[H:MM:SS]\". The hour portion is ommitted if the position
   is less than an hour. Minutes and seconds are zero padded, whereas hours are
   not.

   Examples:

     (time->ts 12.34)
     ;; => \"[00:12]\"

     (time->ts 67.89)
     ;; => \"[01:07]\"

     (time->ts 4567.89)
     ;; => \"[1:16:07]\"

     (time->ts 101112.14)
     ;; => \"[28:05:12]\""
  [time]
  (let [hours (-> time (/ 3600) int)
        mins (-> time (mod 3600) (/ 60) int)
        secs (-> time (mod 60) int)
        hours-str (if (zero? hours) "" (str hours ":"))
        mins-str (cond
                   (zero? mins) "00:"
                   (< mins 10) (str "0" mins ":")
                   :else (str mins ":"))
        secs-str (cond
                   (zero? secs) "00"
                   (< secs 10) (str "0" secs)
                   :else (str secs))]
    (str "[" hours-str mins-str secs-str "]")))
