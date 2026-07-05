(ns oci-sdk-clj.java-test
  (:require [clojure.test :refer :all]
            [oci-sdk-clj.java :as oci-java])
  (:import [com.oracle.bmc.auth AuthenticationDetailsProvider]
           [java.io ByteArrayInputStream]
           [java.security KeyPairGenerator]
           [java.util Base64]))

(defn- private-key-pem
  []
  (let [generator (KeyPairGenerator/getInstance "RSA")
        _ (.initialize generator 2048)
        key-pair (.generateKeyPair generator)
        encoded (.encodeToString (Base64/getMimeEncoder 64 (.getBytes "\n"))
                                 (.. key-pair getPrivate getEncoded))]
    (str "-----BEGIN PRIVATE KEY-----\n"
         encoded
         "\n-----END PRIVATE KEY-----\n")))

(def fake-private-key
  (private-key-pem))

(def fake-provider
  (reify AuthenticationDetailsProvider
    (getFingerprint [_] "00:00:00")
    (getTenantId [_] "ocid1.tenancy.oc1..example")
    (getUserId [_] "ocid1.user.oc1..example")
    (getKeyId [_] "ocid1.tenancy.oc1..example/ocid1.user.oc1..example/00:00:00")
    (getPrivateKey [_] (ByteArrayInputStream. (.getBytes fake-private-key "UTF-8")))
    (getPassPhrase [_] nil)
    (getPassphraseCharacters [_] nil)))

(deftest client-alias-test
  (testing "known aliases resolve to OCI Java SDK client classes"
    (is (= "com.oracle.bmc.identity.IdentityClient"
           (oci-java/class-name :identity)))
    (is (= "com.oracle.bmc.core.ComputeClient"
           (oci-java/class-name :compute)))))

(deftest request-builder-test
  (testing "request maps build typed Java SDK request objects without credentials"
    (let [request (oci-java/request "com.oracle.bmc.identity.requests.ListUsersRequest"
                                    {:compartment-id "ocid1.compartment.oc1..example"})]
      (is (= "ocid1.compartment.oc1..example"
             (.getCompartmentId request))))))

(deftest client-builder-test
  (testing "client aliases build Java SDK client instances without making network calls"
    (let [client (oci-java/client fake-provider :identity {:region :uk-london-1})]
      (is (= "com.oracle.bmc.identity.IdentityClient"
             (.getName (class client)))))))

(deftest call-test
  (testing "operation keywords invoke lower camel Java methods"
    (let [client (StringBuilder.)]
      (is (identical? client (oci-java/call client :append "ok")))
      (is (= "ok" (str client))))))

(deftest client-inventory-test
  (testing "the shaded OCI Java SDK exposes sync clients on the classpath"
    (let [clients (set (oci-java/client-inventory))]
      (is (contains? clients "com.oracle.bmc.identity.IdentityClient"))
      (is (contains? clients "com.oracle.bmc.core.ComputeClient"))
      (is (>= (count clients) 100)))))
