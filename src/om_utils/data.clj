(ns om-utils.data)

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
