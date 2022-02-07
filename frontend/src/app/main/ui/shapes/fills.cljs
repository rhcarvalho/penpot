;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) UXBOX Labs SL

(ns app.main.ui.shapes.fills
  (:require
   [app.common.geom.shapes :as gsh]
   [app.common.data :as d]
   [app.config :as cfg]
   [app.main.ui.shapes.attrs :as attrs]
   [app.main.ui.shapes.embed :as embed]
   [app.main.ui.shapes.gradients :as grad]
   [app.util.object :as obj]
   [rumext.alpha :as mf]))

(mf/defc fills
  {::mf/wrap-props false}
  [props]

  (let [shape     (obj/get props "shape")
        render-id (obj/get props "render-id")]
    (let [{:keys [x y width height]} (:selrect shape)
          {:keys [metadata]} shape
          fill-id (str "fill-" render-id)
          has-image (or metadata (:fill-image shape))
          uri (if metadata
                (cfg/resolve-file-media metadata)
                (cfg/resolve-file-media (:fill-image shape)))
          embed (embed/use-data-uris [uri])
          transform (gsh/transform-matrix shape)
          shape-without-image (dissoc shape :fill-image)
          fill-attrs (-> (attrs/extract-fill-attrs shape-without-image 0)
                         (obj/set! "width" width)
                         (obj/set! "height" height))
          _ (println ":fill" (:fill shape))
          gradients (filter #(some? (:fill-color-gradient %)) (:fill shape))
          _ (println "gradients" gradients)
          ]

      [:*
       (for [[index gradient] (-> (d/enumerate gradients) reverse)]
         (case (:type (:fill-color-gradient gradient))
           :linear [:> grad/linear-gradient #js {:id (str (name :fill-color-gradient) "_" render-id "_" index)
                                            :gradient (:fill-color-gradient gradient)
                                            :shape shape}]
           :radial [:> grad/radial-gradient #js {:id (str (name :fill-color-gradient) "_" render-id "_" index)
                                            :gradient (:fill-color-gradient gradient)
                                            :shape shape}]))

       [:pattern {:id fill-id
                  :patternUnits "userSpaceOnUse"
                  :x x
                  :y y
                  :height height
                  :width width
                  :patternTransform transform
                  :data-loading (str (not (contains? embed uri)))}
        [:g
         (for [[index value] (-> (d/enumerate (:fill shape [])) reverse)]
           [:> :rect (-> (attrs/extract-fill-attrs value index)
                         (obj/set! "width" width)
                         (obj/set! "height" height))])
         [:> :rect fill-attrs]

         (when has-image
           [:image {:xlinkHref (get embed uri uri)
                    :width width
                    :height height}])]]])))