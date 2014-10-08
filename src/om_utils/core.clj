(ns om-utils.core
  (:require [clojure.walk :as walk]))

(def lookup-table
  {'did-mount          {:lifecycle-method 'om.core/IDidMount
                        :arg-list         '[this]}
   'did-update         {:lifecycle-method 'om.core/IDidUpdate
                        :arg-list         '[this prev-props prev-state]}
   'display-name       {:lifecycle-method 'om.core/IDisplayName
                        :arg-list         '[this]}
   'init-state         {:lifecycle-method 'om.core/IInitState
                        :arg-list         '[this]}
   'render             {:lifecycle-method 'om.core/IRender
                        :arg-list         '[this]}
   'render-state       {:lifecycle-method 'om.core/IRenderState
                        :arg-list         '[this state]}
   'should-update      {:lifecycle-method 'om.core/IShouldUpdate
                        :arg-list         '[this next-props next-state]}
   'will-mount         {:lifecycle-method 'om.core/IWillMount
                        :arg-list         '[this]}
   'will-receive-props {:lifecycle-method 'om.core/IWillReceiveProps
                        :arg-list         '[this next-state]}
   'will-unmount       {:lifecycle-method 'om.core/IWillUnmount
                        :arg-list         '[this]}
   'will-update        {:lifecycle-method 'om.core/IWillUpdate
                        :arg-list         '[this next-props next-state]}})

(defn make-friendly-display-name
  [component-name]
  (let [capitalize-first-letter (fn [s]
                                  (apply str (concat (seq (.toUpperCase (.substring s 0 1)))
                                                     (rest s))))]
    (apply str (interpose " " (map capitalize-first-letter (clojure.string/split (str component-name) #"-"))))))

(defn generate-display-name-method
  [component-name]
  `(om.core/IDisplayName
    (display-name
     [this]
     (or (:react-name opts) ~(make-friendly-display-name component-name)))))

(defn body->valid-reify-expr
  [component-name body]
  (let [reify-lifecycle-methods (map (fn [expr]
                                       (let [impl-fn-name (first expr)]
                                         (get-in lookup-table [impl-fn-name :lifecycle-method])))
                                     body)
        body-with-auto-exprs    (map (fn [expr]
                                       (let [impl-fn-name (first expr)]
                                         (concat (list (first expr) (get-in lookup-table [impl-fn-name :arg-list]))
                                                 (rest expr))))
                                     body)
        friendly-display-name   (make-friendly-display-name component-name)
        display-name-method     `(om.core/IDisplayName
                                  (~'display-name
                                   ~'[this]
                                   (or (:react-name ~'opts) ~friendly-display-name)))
        reify-body              (concat display-name-method (interleave reify-lifecycle-methods body-with-auto-exprs))]
    `(reify
       ~@reify-body)))

(def dom-fn-replacement-map
  (reduce (fn [acc dom-fn]
            (assoc acc dom-fn (symbol (str 'om.dom "/" dom-fn))))
          {}
          '[a
            abbr
            address
            area
            article
            aside
            audio
            b
            base
            bdi
            bdo
            big
            blockquote
            body
            br
            button
            canvas
            caption
            cite
            code
            col
            colgroup
            data
            datalist
            dd
            del
            dfn
            div
            dl
            dt
            em
            embed
            fieldset
            figcaption
            figure
            footer
            form
            h1
            h2
            h3
            h4
            h5
            h6
            head
            header
            hr
            html
            i
            iframe
            img
            ins
            kbd
            keygen
            label
            legend
            li
            link
            main
            map
            mark
            marquee
            menu
            menuitem
            meta
            meter
            nav
            noscript
            object
            ol
            optgroup
            output
            p
            param
            pre
            progress
            q
            rp
            rt
            ruby
            s
            samp
            script
            section
            select
            small
            source
            span
            strong
            style
            sub
            summary
            sup
            table
            tbody
            td
            tfoot
            th
            thead
            time
            title
            tr
            track
            u
            ul
            var
            video
            wbr
            
            ;; svg
            circle
            ellipse
            g
            line
            path
            polyline
            rect
            svg
            text
            defs
            linearGradient
            polygon
            radialGradient
            stop
            tspan]))

(defn autogen-dom-fns
  [fn-body]
  (let [render-methods (filter (fn [expr]
                                 (or (= (first expr) 'render)
                                     (= (first expr) 'render-state)))
                               fn-body)]
    (walk/postwalk-replace dom-fn-replacement-map render-methods)))

(defn process-body
  [component-name body]
  (body->valid-reify-expr component-name (autogen-dom-fns body)))

(defmacro defcomponent
  [component-name & body]
  (if (string? (first body))
    (let [[docstring & fn-body] body]
      `(defn ~component-name
         ~docstring
         [~'data ~'owner ~'opts]
         ~(process-body component-name fn-body)))
    `(defn ~component-name
       [~'data ~'owner ~'opts]
       ~(process-body component-name body))))
