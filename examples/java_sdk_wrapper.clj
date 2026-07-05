(ns examples.java-sdk-wrapper
  (:require [oci-sdk-clj.auth :as auth]
            [oci-sdk-clj.java :as oci-java]))

(def provider
  (auth/config-file-authentication-details-provider "DEFAULT"))

(def identity
  (oci-java/client provider :identity {:region :uk-london-1}))

(defn list-users
  [compartment-ocid]
  (let [request (oci-java/request "com.oracle.bmc.identity.requests.ListUsersRequest"
                                  {:compartment-id compartment-ocid})]
    (oci-java/call identity :list-users request)))

(def compute
  (oci-java/client provider :compute {:region :uk-london-1}))

(defn list-shapes
  [compartment-ocid]
  (let [request (oci-java/request "com.oracle.bmc.core.requests.ListShapesRequest"
                                  {:compartment-id compartment-ocid})]
    (oci-java/call compute :list-shapes request)))
