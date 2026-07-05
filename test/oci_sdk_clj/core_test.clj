(ns oci-sdk-clj.core-test
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [oci-sdk-clj.core :as oci]))

(deftest regional-endpoint-test
  (testing "builds the known compute endpoint from the existing raw request table"
    (is (= "https://iaas.uk-london-1.oraclecloud.com/20160918/"
           (oci/regional-endpoint :compute :uk-london-1)))))

(deftest unsupported-raw-verb-test
  (testing "fails clearly for unsupported raw request verbs"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Unsupported OCI raw request verb"
                          (oci/translate-verb-to-fn :patch)))))

(deftest raw-query-params-are-url-encoded-test
  (testing "raw request helpers encode query parameters before signing"
    (let [captured (atom nil)]
      (with-redefs [oci/request (fn [_ req] (reset! captured req))]
        (oci/get :provider
                 "https://iaas.uk-london-1.oraclecloud.com/20160918/shapes/"
                 {:query-params {:compartmentId "ocid value"}}))
      (is (str/includes? (:url @captured) "compartmentId=ocid+value")))))
