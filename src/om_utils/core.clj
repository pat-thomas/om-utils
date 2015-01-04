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

(defmacro defcomponent
  [component-name & body]
  (cond
    (and (string? (first body))
         (vector? (second body))) ;; treat (second body) as map destructuring for opts
    (let [[docstring opts-keys & fn-body] body]
      `(defn ~component-name
         ~docstring
         [~'data ~'owner {:keys ~opts-keys :as ~'opts}]
         ~(body->valid-reify-expr component-name fn-body)))

    (string? (first body))
    (let [[docstring & fn-body] body]
      `(defn ~component-name
         ~docstring
         [~'data ~'owner ~'opts]
         ~(body->valid-reify-expr component-name fn-body)))

    (vector? (first body)) ;; treat (second body) as map destructuring for opts)
    (let [[opts-keys & fn-body] body]
      `(defn ~component-name
         [~'data ~'owner {:keys ~opts-keys :as ~'opts}]
         ~(body->valid-reify-expr component-name fn-body)))

    :default
    `(defn ~component-name
       [~'data ~'owner ~'opts]
       ~(body->valid-reify-expr component-name body))))
