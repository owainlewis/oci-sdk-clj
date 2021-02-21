(ns oci-sdk-clj.example
  (:require [oci-sdk-clj.auth :as auth]
            [oci-sdk-clj.core :as oci]))

(def compartment (System/getenv "COMPARTMENT_OCID"))

(defn list-users-request [compartment]
  (let [provider (auth/config-file-authentication-details-provider "DEFAULT")
        endpoint "https://identity.uk-london-1.oraclecloud.com/20160918/users/"]
    (oci/get provider endpoint {:compartmentId compartment})))
