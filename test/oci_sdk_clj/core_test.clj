(ns oci-sdk-clj.core-test
  (:require [clojure.test :refer :all]
            [oci-sdk-clj.auth :as auth]
            [oci-sdk-clj.core :as oci]))

(def provider (auth/config-file-authentication-details-provider "DEFAULT"))

(def compartment-ocid (System/getenv "COMPARTMENT_OCID"))

(defn get-user-request [provider user-ocid]
  (let [url (str "https://identity.uk-london-1.oraclecloud.com/20160918/users/" user-ocid)]
    (oci/get provider url {:oci-debug true})))

(deftest valid-get-user-request-test
  (testing "Should return user details"
    (let [user (-> provider auth/auth->map :user-id)
          response (get-user-request provider user)]
      (is (= 200 (:status response))))))

(defn get-shapes-request [provider compartment-ocid]
  (let [url (str "https://iaas.uk-london-1.oraclecloud.com/20160918/shapes/")]
    (oci/get provider url
             {:oci-debug true
              :query-params {:compartmentId compartment-ocid}})))

(deftest valid-get-shapes-request-test
  (testing "Should return a list of compute shapes"
    (let [response (get-shapes-request provider compartment-ocid)]
      (is (= 200 (:status response))))))
