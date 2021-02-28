(ns oci-sdk-clj.example
  (:require [oci-sdk-clj.auth :as auth]
            [oci-sdk-clj.core :as oci]))

(def compartment (System/getenv "COMPARTMENT_OCID"))

(def provider (auth/config-file-authentication-details-provider "DEFAULT"))

(defn list-users-request [compartment-ocid]
  (let [endpoint "https://identity.uk-london-1.oraclecloud.com/20160918/users/"]
    (oci/get provider endpoint
             {:query-params {:compartmentId compartment-ocid}})))

(defn list-shapes-request [compartment-ocid]
  (let [provider (auth/config-file-authentication-details-provider "DEFAULT")
        endpoint "https://iaas.uk-london-1.oraclecloud.com/20160918/shapes/"]
    (oci/get provider endpoint
             {:query-params {:compartmentId compartment-ocid}
                             :oci-debug true})))

(defn bare-metal-shapes
  "Return a list of all available Bare Metal compute shapes"
  [compartment-ocid]
  (let [all-shapes (oci/get provider "https://iaas.uk-london-1.oraclecloud.com/20160918/shapes/"
                            {:query-params {:compartmentId compartment-ocid}
                             :oci-debug false})]
    (filter (fn [shape]
              (clojure.string/starts-with? shape "BM"))
            (map :shape all-shapes))))

(defn dsl-example
  [provider compartment-ocid]
  (oci/run provider :compute :shapes :list {:query-params {:compartmentId compartment-ocid}}))

(defn create-instance-example
  "Create a compute instance"
  [availability-domain compartment subnet]
  (let [launch-instance-details {:availabilityDomain availability-domain
                                 :compartmentId compartment
                                 :subnetId subnet
                                 :shape "VM.Standard.E3.Flex"}]
    (oci/run provider :compute :instances :create {:body launch-instance-details})))
