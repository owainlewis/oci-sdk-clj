(ns oci-sdk-clj.core
  (:import [java.net URI URLEncoder])
  (:require [clj-http.client :as http]
            [cheshire.core :as json]
            [oci-sdk-clj.auth :as auth])
  (:import [com.oracle.bmc.http.signing DefaultRequestSigner])
  (:refer-clojure :exclude [get]))

(defn- params->query-string
  "Converts a map of query parameter options into a URL encoded query string that
   can be added to a URI"
  [m]
  (clojure.string/join "&"
                       (for [[k v] m]
                         (str (name k) "="
                              (URLEncoder/encode (str (or v "")) "UTF-8")))))

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
        modified (dissoc (into {} signed-headers) "content-length")]
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

(defn json-body-transformer
  "Given an HTTP request, transform the body from a Clojure map to a JSON string"
  [req]
  (let [body (:body req)]
    (if (some? body)
      (assoc req :body (json/generate-string body))
      req)))

(defn- build-request
  "Construct an HTTP request for dispatching and signing"
  [method url req]
  (let [req' (json-body-transformer (or req {}))
        query-params (:query-params req)
        url-with-query (if (or (nil? query-params)
                               (empty? query-params))
                         url
                         (str url "?" (params->query-string query-params)))]
    (merge {:method method
            :url url-with-query
            :as :json
            :headers {"content-type" ["application/json"]}
            :throw-exceptions false}
           (dissoc req' :query-params))))

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

(def realms [{:realm "oc1" :domain "oraclecloud.com"}
             {:realm "oc2" :domain "oraclegovcloud.com"}
             {:realm "oc3" :domain "oraclegovcloud.com"}
             {:realm "oc4" :domain "oraclegovcloud.uk"}
             {:realm "oc8" :domain "oraclecloud8.com"}])

(def services [{:name "analytics" :description "Analytics" :endpoint "" :version ""}
               {:name "announce" :description "Announcements Service" :endpoint "" :version ""}
               {:name "api-gateway" :description "API Gateway" :endpoint "" :version ""}
               {:name "application-migration" :description "Application Migration" :endpoint "" :version ""}
               {:name "audit" :description "Audit" :endpoint "" :version ""}
               {:name "autoscaling" :description "Autoscaling" :endpoint "" :version ""}
               {:name "bds" :description "Big Data Service" :endpoint "" :version ""}
               {:name "blockchain" :description "Blockchain Platform Control Plane" :endpoint "" :version ""}
               {:name "budgets" :description "Budgets" :endpoint "" :version ""}
               {:name "bv" :description "Block Volume Service" :endpoint "" :version ""}
               {:name "ce" :description "Container Engine for Kubernetes" :endpoint "" :version ""}
               {:name "cloud-guard" :description "Cloud Guard" :endpoint "" :version ""}
               {:name "compute" :description "Compute Service" :endpoint "iaas" :version "20160918"}
               {:name "compute-management" :description "Compute Management Service" :endpoint "" :version ""}
               {:name "data-catalog" :description "Data Catalog" :endpoint "" :version ""}
               {:name "data-flow" :description "Data Flow" :endpoint "" :version ""}
               {:name "data-integration" :description "Data Integration" :endpoint "" :version ""}
               {:name "data-safe" :description "Data Safe" :endpoint "" :version ""}
               {:name "data-science" :description "Data Science" :endpoint "" :version ""}
               {:name "database-management" :description "Database Management" :endpoint "" :version ""}
               {:name "db" :description "Database Service" :endpoint "" :version ""}
               {:name  "dns" :description "DNS" :endpoint "" :version ""}
               {:name "dts" :description "Data Transfer Service" :endpoint "" :version ""}
               {:name "email" :description "Email Delivery" :endpoint "" :version ""}
               {:name "events" :description "Events" :endpoint "" :version ""}
               {:name "fn" :description "Functions Service" :endpoint "" :version ""}
               {:name "fs" :description "File Storage" :endpoint "" :version ""}
               {:name "health-checks" :description "Health Checks" :endpoint "" :version ""}
               {:name "iam" :description "Identity and Access Management Service" :endpoint "" :version ""}
               {:name "instance-agent" :description "Compute Instance Agent Service" :endpoint "" :version ""}
               {:name "integration" :description "Oracle Integration"}
               {:name "kms" :description "Key Management" :endpoint "" :version ""}
               {:name "lb" :description "Load Balancing" :endpoint "" :version ""}
               {:name "limits" :description "Service limits" :endpoint "" :version ""}
               {:name "log-analytics" :description "LogAnalytics" :endpoint "" :version ""}
               {:name "logging" :description "Logging Management" :endpoint "" :version ""}
               {:name "logging-ingestion" :description "Logging Ingestion" :endpoint "" :version ""}
               {:name "logging-search" :description "Logging Search" :endpoint "" :version ""}
               {:name "management-agent" :description "Management Agent" :endpoint "" :version ""}
               {:name "management-dashboard" :description "ManagementDashboard" :endpoint "" :version ""}
               {:name "marketplace" :description "Marketplace Service" :endpoint "" :version ""}
               {:name "monitoring" :description "Monitoring" :endpoint "" :version ""}
               {:name "mysql" :description "MySQL Database Service" :endpoint "" :version ""}
               {:name "network" :description "Networking Service" :endpoint "" :version ""}
               {:name "nosql" :description "NoSQL Database" :endpoint "" :version ""}
               {:name "oce" :description "Oracle Content and Experience" :endpoint "" :version ""}
               {:name "ocvs" :description "Oracle Cloud VMware Solution" :endpoint "" :version ""}
               {:name "oda" :description "Digital Assistant Service Instance" :endpoint "" :version ""}
               {:name "ons" :description "Notifications" :endpoint "" :version ""}
               {:name "opsi" :description "Operations Insights" :endpoint "" :version ""}
               {:name "optimizer" :description "Cloud Advisor" :endpoint "" :version ""}
               {:name "organizations" :description "TenantManager" :endpoint "" :version ""}
               {:name "os" :description "Object Storage Service" :endpoint "" :version ""}
               {:name "os-management" :description "OS Management" :endpoint "" :version ""}
               {:name "raw-request" :description "Makes a raw request against an OCI service" :endpoint "" :version ""}
               {:name "resource-manager" :description "Resource Manager" :endpoint "" :version ""}
               {:name "rover" :description "RoverCloudService" :endpoint "" :version ""}
               {:name "sch" :description "Service Connector Hub" :endpoint "" :version ""}
               {:name "search" :description "Search Service" :endpoint "" :version ""}
               {:name "secrets" :description "Secrets" :endpoint "" :version ""}
               {:name "session" :description "Session commands for CLI" :endpoint "" :version ""}
               {:name "setup" :description "Setup commands for CLI" :endpoint "" :version ""}
               {:name "streaming" :description "Streaming Service" :endpoint "" :version ""}
               {:name "support" :description "Support Management" :endpoint "" :version ""}
               {:name "usage-api" :description "Usage" :endpoint "" :version ""}
               {:name "vault" :description "Secrets Management" :endpoint "" :version ""}
               {:name "waas" :description "Web Application Acceleration and Security Services" :endpoint "" :version ""}
               {:name "work-requests" :description "Work Requests" :endpoint "" :version ""}])

(defn find-map [ms k v]
  (into {}
        (filter
         (fn [m]
           (= (clojure.core/get m k) v)) ms)))

(defn- format-endpoint
  ([endpoint region version]
   (format-endpoint endpoint region "oc1" version))
  ([endpoint region realm version]
   (let [domain (-> (filter
                     (fn [m]
                       (= (:realm m) realm))
                     realms) first :domain)]
     (str "https://" (name endpoint) "." (name region) "." domain "/" version "/"))))

(defn regional-endpoint
  "Get the regional endpoint for a service name with the correct API version
   Example -> (regional-endpoint :compute :uk-london-1)"
  [service region]
  (let [service (find-map services :name (name service))
        {endpoint :endpoint version :version} service]
    (when (some? service)
      (format-endpoint endpoint region version))))

(defn translate-verb-to-fn [verb]
  (case verb
    :get #'get
    :list #'get
    :create #'post
    :update #'put
    :delete #'delete
    (throw (ex-info "Unsupported OCI raw request verb" {:verb verb}))))

(defn run
  [provider service resource verb req]
  (let [region (-> provider auth/auth->map :region)
        request-fn (translate-verb-to-fn verb)
        endpoint (regional-endpoint service region)
        url (str endpoint (name resource))]
    (apply request-fn [provider url req])))
