(ns oci-sdk-clj.auth
  (:import [java.net URI]
           [com.oracle.bmc.auth ConfigFileAuthenticationDetailsProvider]
           [com.oracle.bmc.http.signing DefaultRequestSigner]))

(defn config-file-authentication-details-provider
  "Create a configuration file authentication profile with an optional profile"
  ([]
   (config-file-authentication-details-provider "DEFAULT"))
  ([profile]
   (new ConfigFileAuthenticationDetailsProvider profile)))

(defn auth->map
  [provider]
  {:user-id (.getUserId provider)
   :tenant-id (.getTenantId provider)
   :key-id (.getKeyId provider)
   :fingerprint (.getFingerprint provider)
   :region (.getRegion provider)
   :pass-phrase (.getPassPhrase provider)})
