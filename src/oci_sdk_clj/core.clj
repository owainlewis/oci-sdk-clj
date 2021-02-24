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
   sign the request, dispatch, and return the payload as JSON

   If you need the full payload you can set an oci-debug flag {:oci-debug true}"
  [auth-provider req]
  (if (true? (:oci-debug req))
    (-> (sign-request auth-provider req) http/request)
    (-> (sign-request auth-provider req) http/request :body)))

(defn- params->query-string
  "Converts a map of query parameter options into a URL encoded query string that
   can be added to a URI"
  [m]
  (clojure.string/join "&"
    (for [[k v] m]
      (str (name k) "="
           (URLEncoder/encode (or v ""))))))

(defn- build-request [method url req]
  (let [query-params (:query-params req)
        url-with-query (if (or (nil? query-params)
                               (empty? query-params))
                         url
                         (str url "?" (params->query-string query-params)))]
    (merge {:method method :url url-with-query :as :json :throw-exceptions false}
           (dissoc req :query-params))))

(defn define-method-fn
  "Like #'request, but sets the :method and :url as appropriate."
  [auth-provider url method req]
  (let [builder-req (build-request method url req)]
    (request auth-provider builder-req)))

(defn get
  "Like #'request, but sets the :method and :url as appropriate."
  ([auth-provider url] (get auth-provider url nil))
  ([auth-provider url req]
    (define-method-fn auth-provider url :get req)))

(defn put
  "Like #'request, but sets the :method and :url as appropriate."
  ([auth-provider url] (put auth-provider url nil))
  ([auth-provider url req]
   (define-method-fn auth-provider url :put req)))

(defn post
  "Like #'request, but sets the :method and :url as appropriate."
  ([auth-provider url] (post auth-provider url nil))
  ([auth-provider url req]
   (define-method-fn auth-provider url :post req)))

(defn delete
  "Like #'request, but sets the :method and :url as appropriate."
  ([auth-provider url] (delete auth-provider url nil))
  ([auth-provider url req]
   (define-method-fn auth-provider url :delete req)))

;; Can we write a macro generate OCI CLI DSL (oci/compute :instance :list) without writing any actual code?

(defn translate-resource [resource]
  (case resource
    :instance "https://iaas.uk-london-1.oraclecloud.com/20160918/"))

(defn translate-verb-to-fn [verb]
  (case verb
    :get #'get
    :list #'get
    :create #'post
    :delete #'delete
    #'get))

(defmacro ^:private defoci [method]
    `(defn ~method
       ~'[provider resource verb req]
       (let [~'a (translate-resource ~'resource)
             ~'endpoint (str ~'a "/" (name ~'verb))
             ~'intern-fn (translate-verb-to-fn ~'verb)]
         (apply ~'intern-fn [~'provider ~'endpoint ~'req]))))

(defoci compute)
