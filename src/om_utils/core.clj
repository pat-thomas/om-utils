(ns om-utils.core
  (:require [om-utils.data :as data]
            [clojure.walk  :as walk]))

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
                                         (get-in data/lookup-table [impl-fn-name :lifecycle-method])))
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

(defn autogen-dom-fns
  [fn-body]
  (let [render-methods (filter (fn [expr]
                                 (or (= (first expr) 'render)
                                     (= (first expr) 'render-state)))
                               fn-body)]
    (walk/postwalk-replace data/dom-fn-replacement-map render-methods)))

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
