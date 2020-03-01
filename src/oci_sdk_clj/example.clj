(ns oci-sdk-clj.example
  (:require [oci-sdk-clj.core :as oci]))

(defn sample-request
  [compartment]
  {:request-method :get
   :headers {}
   :url
     (str "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances?compartmentId="
       compartment)})

(defn example [compartment]
  (let [auth (oci/config-file-authentication-provider)]
    (->> (oci/request auth (sample-request compartment))
         :body
         (mapv :shape))))