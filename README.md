## API docs:
Currently, this library only contains one macro, defcomponent, which is intended to reduce the verbosity in defining Om components.
####Example:
```clj
(defcomponent my-component
  "Docstring for component."
  [foo bar]
  (render
    (dom/div
      #js {:id "my-component"}
      (str (:something data)
           (:something-else opts)))))
```
macroexpands to:
```clj
(defn my-component
  "Docstring for component."
  [data owner {:keys [foo bar] :as opts}]
  (reify
    om.core/IDisplayName
    (display-name [this]
      (or (:react-name opts) "My Component"))
    
    om.core/IRender
    (render [this]
      (om.dom/div
        #js {:id "my-component"}
        (str (:something data)
             (:something-else opts))))))
``` 

## Installation
Add the following to your project.clj:
```clj
[om-utils "0.3.0"]
```
