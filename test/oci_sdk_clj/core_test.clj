(ns oci-sdk-clj.core-test
  (:require [clojure.test :refer :all]
            [oci-sdk-clj.auth :as auth]
            [oci-sdk-clj.core :as oci]))

(def provider (auth/config-file-authentication-details-provider "DEFAULT"))

(def compartment-ocid (System/getenv "COMPARTMENT_OCID"))

(defn get-user-request [provider user-ocid]
  (let [url (str "https://identity.uk-london-1.oraclecloud.com/20160918/users/" user-ocid)]
    (oci/get provider url nil)))

(deftest valid-get-user-request-test
  (testing "Should return user details"
    (let [user (-> provider auth/auth->map :user-id)
          response (get-user-request provider user)]
      (println response)
      (is (= 200 (:status response))))))

;; (defn list-users-request [auth]
;;   (let [url (str
;;              "https://identity.uk-london-1.oraclecloud.com/20160918/users?compartmentId="
;;              "")]
;;     (get auth url {})))

;; (deftest valid-request-list-instances
;;   (testing "Should list compute instances in a compartment"
;;     (let [response (list-users-request auth)]
;;       (println response))))
