(ns oci-sdk-clj.core
  (:import [java.net URI]
           [com.oracle.bmc.auth ConfigFileAuthenticationDetailsProvider]
           [com.oracle.bmc OCID]
           [com.oracle.bmc.http.signing DefaultRequestSigner])
  (:require [clj-http.client :as http]))

(defn valid-ocid? [ocid] (OCID/isValid ocid))

(defn config-file-authentication-provider
 "Create a configuration file authentication profile with an optional
  profile"
 ([] (config-file-authentication-provider "DEFAULT"))
 ([profile] (new ConfigFileAuthenticationDetailsProvider profile)))

(defn sign-request-params
  [auth-provider uri-string method headers body]
  (let [uri (URI/create uri-string)
        signer (DefaultRequestSigner/createRequestSigner auth-provider)]
    (.signRequest signer uri method headers body)))

(defn sign-request
  "Given an authentication provider and a Clojure map representing a clj-http HTTP request
   return the signed request map"
  [auth-provider request]
  (let [{uri :url
         method :request-method
         headers :headers
         query :query-params
         body :body} request
         qs (http/generate-query-string query)]
  (let [uri-with-query (if (clojure.string/blank? qs)
                         str
                        (str uri "?" qs))
        signed-headers (sign-request-params
                         auth-provider uri-with-query (name method) headers body)]
    (assoc request :headers
      (into {} signed-headers)))))

;; TODO use an ordered map for query params!

(defn request
  "Given an authenticaton provider and a raw Clojure map representing a clj-http HTTP request
   first sign the request and then dispatch it returning the payload as JSON
   If no authentication provider is given, default to the config-file-authentication-provider
   with a DEFAULT profile."
  ([request]
    (request (config-file-authentication-provider)))
  ([auth-provider request]
  (let [signed-request (sign-request auth-provider request)]
    (http/request
      (merge signed-request {:as :json})))))

;; Fails because of incorrect params handling
(defn- list-instances
  [compartment]
  {:request-method :get
   :headers {}
   :query-params {"compartmentId" compartment}
   :url "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances"})
