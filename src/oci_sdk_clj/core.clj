(ns oci-sdk-clj.core
  (:import [java.net URI]
           [com.oracle.bmc.auth ConfigFileAuthenticationDetailsProvider]
           [com.oracle.bmc OCID]
           [com.oracle.bmc.http.signing DefaultRequestSigner])
  (:require [clj-http.client :as http])
  (:require [java-http-clj.core :as client])
  (:refer-clojure :exclude [get]))

(defn valid-ocid? [ocid] (OCID/isValid ocid))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Authentication providers (todo move to separate place)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn config-file-authentication-provider
 "Create a configuration file authentication profile with an optional
  profile"
 ([] (config-file-authentication-provider "DEFAULT"))
 ([profile] (new ConfigFileAuthenticationDetailsProvider profile)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Request signatures
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
    (-> req (assoc :headers (into {} signed-headers)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Requests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn request
  "Given an authenticaton provider and a raw Clojure map representing a clj-http HTTP request
   first sign the request and then dispatch it returning the payload as JSON"
  [auth-provider req]
  (let [signed-request (sign-request auth-provider req)
        modified (update-in signed-request [:headers] dissoc "content-length")]
    (core/send-request signed-request)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helper functions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get
  "Like #'request, but sets the :method and :url as appropriate."
  ([auth-provider url req]
    (request auth-provider (merge req {:method :get :url url})))
  ([url req]
    (get (config-file-authentication-provider) url req)))

(defn post
  "Like #'request, but sets the :method and :url as appropriate."
  ([auth-provider url req]
    (request auth-provider (merge req {:method :post :url url})))
  ([url req]
     (post (config-file-authentication-provider) url req)))

(defn put
  "Like #'request, but sets the :method and :url as appropriate."
  ([auth-provider url req]
    (request auth-provider (merge req {:method :put :url url})))
  ([url req]
    (put (config-file-authentication-provider) url req)))

(defn delete
  "Like #'request, but sets the :method and :url as appropriate."
  ([auth-provider url req]
  (request auth-provider (merge req {:method :delete :url url})))
  ([url req]
     (delete (config-file-authentication-provider) url req)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Examples
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn example-list-instances [compartment]
  (get "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances"
    {:query-params {"compartmentId" compartment} :as :json}))

(defn example-create-vcn [compartment]
  (post "https://iaas.us-ashburn-1.oraclecloud.com/20160918/vcns"
    {:compartmentId compartment
     :displayName "Clojure API Virtual Cloud Network",
     :cidrBlock "172.16.0.0/16"}))
