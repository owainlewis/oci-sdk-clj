(ns oci-sdk-clj.core
  (:import [java.net URI]
           [com.oracle.bmc.auth ConfigFileAuthenticationDetailsProvider]
           [com.oracle.bmc.http.signing DefaultRequestSigner])
  (:require [clj-http.client :as http]))

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
         body :body} request
        signed-headers (signing/sign-request-params
                         auth-provider uri (name method) headers body)]
    (assoc request :headers
      (into {} signed-headers))))

(defn request
  "Given an authenticaton provider and a raw Clojure map representing a clj-http HTTP request
   first sign the request and then dispatch it returning the payload as JSON"
  [auth-provider request]
  (let [signed-request (sign-request auth-provider request)]
    (http/request
      (merge signed-request {:as :json}))))
