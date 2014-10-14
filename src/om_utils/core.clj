(ns om-utils.core
  (:require [om-utils.data  :as data]
            [clojure.walk   :as walk]
            [clojure.string :as string]))

(defn split-by
  [pred s]
  (let [res (group-by pred s)]
    (list (get res true) (get res false))))

(defn make-friendly-display-name
  [component-name]
  (->> (-> component-name str (string/split #"-"))
       (map string/capitalize)
       (string/join " ")))

(defn generate-display-name-method
  [component-name]
  `(om.core/IDisplayName
    (~'display-name
     ~'[this]
     (or (:react-name ~'opts) ~(make-friendly-display-name component-name)))))

(defn body->valid-reify-expr
  [component-name body]
  (let [reify-lifecycle-methods (map (fn [expr]
                                       (let [impl-fn-name (first expr)]
                                         (get-in data/lookup-table [impl-fn-name :lifecycle-method])))
                                     body)
        body-with-auto-exprs    (map (fn [expr]
                                       (let [impl-fn-name (first expr)]
                                         (concat (list (first expr) (get-in data/lookup-table [impl-fn-name :arg-list]))
                                                 (rest expr))))
                                     body)]
    `(reify
       ~@(generate-display-name-method component-name)
       ~@(interleave reify-lifecycle-methods body-with-auto-exprs))))

(defn is-render-method?
  [expr]
  (or (= (first expr) 'render)
      (= (first expr) 'render-state)))

(defn autogen-dom-fns
  [fn-body]
  (let [[render-methods non-render-methods] (split-by is-render-method? fn-body)]
    (concat (walk/postwalk-replace data/dom-fn-replacement-map render-methods)
            non-render-methods)))

(defn process-body
  [component-name body]
  (let [body-with-dom-fns (autogen-dom-fns body)]
    (body->valid-reify-expr component-name body-with-dom-fns)))

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
