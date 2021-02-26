(ns oci-sdk-clj.auth
  (:require [clojure.string :as s])
  (:import [java.net URI]
           [com.oracle.bmc.auth ConfigFileAuthenticationDetailsProvider]
           [com.oracle.bmc.http.signing DefaultRequestSigner]))

(defn- derive-region
  "Get the OCI region name as a keyword e.g :us-ashburn-1"
  [region]
  (s/lower-case (s/replace (.toString region) "_" "-")))

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
   :region (keyword (derive-region (.getRegion provider)))
   :pass-phrase (.getPassPhrase provider)})
