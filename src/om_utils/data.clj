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
