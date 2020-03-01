(ns oci-sdk-clj.example
  (:require [oci-sdk-clj.core :as oci]))

(defn sample-request
  [compartment]
  {:request-method :get
   :headers {}
   :query-params {"compartmentId" compartment}
   :url
     (str "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances"
       compartment)})

(defn example [compartment]
  (let [auth (oci/config-file-authentication-provider)]
    (->> (oci/request auth (sample-request compartment))
         :body
         (mapv :shape))))