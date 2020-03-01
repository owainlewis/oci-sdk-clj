(ns oci-sdk-clj.core
  (:require [oci-sdk-clj.signing :as signing]
            [java-http-clj.core :as http]))

(defn sample-request [compartment]
  {:body nil
   :headers {}
   :method :get
   :uri (str "https://iaas.us-ashburn-1.oraclecloud.com"
             "/20160918/instances?compartmentId="
             compartment)})

(def auth-provider (signing/config-file-authentication-provider))

;; (defn sign-request-params [auth-provider uri-string method headers body]

(defn go [auth-provider request]
  (let [{uri :uri method :method headers :headers body :body} request
        signed-headers (signing/sign-request-params auth-provider uri (name method) headers body)]
    (assoc request :headers signed-headers)))

(defn req [compartment]
  (let [request (go auth-provider (sample-request compartment))]
    request))
