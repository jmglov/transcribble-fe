(ns transcribble.formats)

(def potential-formats {:audio ["mp3" "ogg" "webm" "wav"]
                        :video ["mp4" "ogg" "webm"]})

(defn supported-formats
  ([]
   (supported-formats potential-formats))
  ([formats]
   (->> formats
        (map (fn [[media-type fmts]]
               [media-type
                (let [el (js/document.createElement (name media-type))]
                  (->> fmts
                       (filter #(#{"maybe" "probably"}
                                 (.canPlayType el
                                               (str (name media-type) "/" %))))))]))
        (into {}))))

(comment

  (supported-formats)
  ;; => {:audio ("mp3" "ogg" "webm" "wav"), :video ("mp4" "ogg" "webm")}

  (let [audio (js/document.createElement "audio")]
    (->> potential-formats
         :audio
         (filter #(#{"maybe" "probably"}
                   (.canPlayType audio (str "audio/" %))))))
  ;; => ("mp3" "ogg" "webm" "wav")
  ;; => ("mp3")


  )
