(ns oci-sdk-clj.core
  (:import [java.net URI]
           [com.oracle.bmc.auth ConfigFileAuthenticationDetailsProvider]
           [com.oracle.bmc OCID]
           [com.oracle.bmc.http.signing DefaultRequestSigner])
  (:require [clj-http.client :as http])
  (:refer-clojure :exclude [get]))

(defn valid-ocid? [ocid] (OCID/isValid ocid))

(defn config-file-authentication-provider
 "Create a configuration file authentication profile with an optional
  profile"
 ([] (config-file-authentication-provider "DEFAULT"))
 ([profile] (new ConfigFileAuthenticationDetailsProvider profile)))

(defn- sign-request-params
  [auth-provider uri-string method headers body]
  (let [uri (URI/create uri-string)
        signer (DefaultRequestSigner/createRequestSigner auth-provider)]
    (.signRequest signer uri method headers body)))

;; TODO edge case of url with query params + query params map
(defn- apply-query-params [url query-params]
  (let [qs (http/generate-query-string query-params)]
    (if (clojure.string/blank? qs)
      url
      (str url "?" qs))))

(defn sign-request
  "Given an authentication provider and a Clojure map representing a clj-http HTTP request
   return the signed request map"
  [auth-provider {:keys [url method headers query-params body] :as req}]
  (let [uri-with-query (apply-query-params url query-params)
        signed-headers (sign-request-params
                         auth-provider
                         uri-with-query
                         (name method)
                         (or headers {})
                         body)]
    (assoc req :headers
      (into {} signed-headers))))

;; TODO use an ordered map for query params!
(defn request
  "Given an authenticaton provider and a raw Clojure map representing a clj-http HTTP request
   first sign the request and then dispatch it returning the payload as JSON"
  [auth-provider req]
  (let [signed-request (sign-request auth-provider req)]
    (http/request signed-request)))

(defn get
  "Like #'request, but sets the :method and :url as appropriate."
  [auth-provider url req]
  (request auth-provider (merge req {:method :get :url url})))

(defn post
  "Like #'request, but sets the :method and :url as appropriate."
  [auth-provider url req]
  (request auth-provider (merge req {:method :post :url url})))

(defn put
  "Like #'request, but sets the :method and :url as appropriate."
  [auth-provider url req]
  (request auth-provider (merge req {:method :put :url url})))

(defn delete
  "Like #'request, but sets the :method and :url as appropriate."
  [auth-provider url req]
  (request auth-provider (merge req {:method :delete :url url})))

;; Fails because of incorrect params handling
(defn- list-instances
  [compartment]
  {:request-method :get
   :headers {}
   :query-params {"compartmentId" compartment}
   :url "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances"})