(ns oci-sdk-clj.core
  (:import [com.oracle.bmc.auth ConfigFileAuthenticationDetailsProvider]
           [com.oracle.bmc.http.signing DefaultRequestSigner]))

(defn authentication-provider
 ([] (authentication-provider "DEFAULT"))
 ([profile] (new ConfigFileAuthenticationDetailsProvider profile)))
