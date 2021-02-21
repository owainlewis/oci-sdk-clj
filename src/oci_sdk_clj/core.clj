(ns oci-sdk-clj.core
  (:import [java.net URI URLEncoder])
  (:require [clj-http.client :as http])
  (:import [com.oracle.bmc.http.signing DefaultRequestSigner])
  (:refer-clojure :exclude [get]))

(defn- sign-request-params
  [auth-provider uri-string method headers body]
  (let [uri (URI/create uri-string)
        signer (DefaultRequestSigner/createRequestSigner auth-provider)]
    (.signRequest signer uri method headers body)))

(defn sign-request
  "Given an authentication provider and a Clojure map representing a clj-http HTTP request
   return the signed request map"
  [auth-provider {:keys [url method headers query-params body] :as req}]
  (let [signed-headers (sign-request-params auth-provider
                                            url
                                            (name method)
                                            (or headers {})
                                            body)
        modified (into {} signed-headers)]
    (-> req
        (assoc :headers modified))))

(defn request
  "Given an authenticaton provider and a raw Clojure map representing an HTTP request
   sign the request, dispatch, and return the payload as JSON"
  [auth-provider req]
  (-> (sign-request auth-provider req)
      http/request))

;; To remove the host header
(comment (update-in [:headers] dissoc "host"))

(defn params->query-string
  "Converts a map of query parameter options into a URL encoded query string that
   can be added to a URI"
  [m]
  (clojure.string/join "&"
    (for [[k v] m]
      (str (name k) "="
           (URLEncoder/encode v)))))

(defn build-request [method url query-params & other-params]
  (let [url-with-query (if (or (nil? query-params)
                               (empty? query-params))
                         url
                         (str url "?" (params->query-string query-params)))]
    (merge {:method method :url url-with-query :as :json :throw-exceptions false}
           (into {} other-params))))

(defn get
  "Like #'request, but sets the :method and :url as appropriate."
  [auth-provider url query-params & other-params]
  (let [req (build-request :get url query-params other-params)]
    (request auth-provider req)))
