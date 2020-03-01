(ns oci-sdk-clj.signing
  (:import [java.net URI]
           [com.oracle.bmc.auth ConfigFileAuthenticationDetailsProvider]
           [com.oracle.bmc.http.signing DefaultRequestSigner]))

;; Read config from the profile DEFAULT in the file "~/.oci/config".
;; You can switch to different profile.
(defn config-file-authentication-provider
 ([] (config-file-authentication-provider "DEFAULT"))
 ([profile] (new ConfigFileAuthenticationDetailsProvider profile)))

(defn sign-request-params [auth-provider uri-string method headers body]
  (let [uri (URI/create uri-string)
        signer (DefaultRequestSigner/createRequestSigner auth-provider)]
    (.signRequest signer uri method headers body)))

;; Note you must merge the return value of the above into the request headers map

(defn sign-request [auth-provider request])
