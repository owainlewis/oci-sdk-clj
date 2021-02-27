(ns oci-sdk-clj.core
  (:import [java.net URI URLEncoder])
  (:require [clj-http.client :as http]
            [oci-sdk-clj.auth :as auth])
  (:import [com.oracle.bmc Realm Region])
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
               {:name "data-catalog" :description "Data Catalog"}
               {:name "data-flow" :description "Data Flow"}
               {:name "data-integration" :description "Data Integration"}
               {:name "data-safe" :description "Data Safe"}
               {:name "data-science" :description "Data Science"}
               {:name "database-management" :description "Database Management"}
               {:name "db" :description "Database Service"}
               {:name  "dns" :description "DNS"}
               {:name "dts" :description "Data Transfer Service"}
               {:name "email" :description "Email Delivery"}
               {:name "events" :description "Events"}
               {:name "fn" :description "Functions Service"}
               {:name "fs" :description "File Storage"}
               {:name "health-checks" :description "Health Checks"}
               {:name "iam" :description "Identity and Access Management Service"}
               {:name "instance-agent" :description "Compute Instance Agent Service"}
               {:name "integration" :description "Oracle Integration"}
               {:name "kms" :description "Key Management"}
               {:name "lb" :description "Load Balancing"}
               {:name "limits" :description "Service limits"}
               {:name "log-analytics" :description "LogAnalytics"}
               {:name "logging" :description "Logging Management"}
               {:name "logging-ingestion" :description "Logging Ingestion"}
               {:name "logging-search" :description "Logging Search"}
               {:name "management-agent" :description "Management Agent"}])
  ;; "management-dashboard   ManagementDashboard
  ;; "marketplace            Marketplace Service
  ;; "monitoring             Monitoring
  ;; "mysql                  MySQL Database Service
  ;; "network                Networking Service
  ;; "nosql                  NoSQL Database
  ;; "oce                    Oracle Content and Experience
  ;; "ocvs                   Oracle Cloud VMware Solution
  ;; "oda                    Digital Assistant Service Instance
  ;; "ons                    Notifications
  ;; "opsi                   Operations Insights
  ;; "optimizer              Cloud Advisor
  ;; "organizations          TenantManager
  ;; "os                     Object Storage Service
  ;; "os-management          OS Management
  ;; "raw-request            Makes a raw request against an OCI service
  ;; "resource-manager       Resource Manager
  ;; "rover                  RoverCloudService
  ;; "sch                    Service Connector Hub
  ;; "search                 Search Service
  ;; "secrets                Secrets
  ;; "session                Session commands for CLI
  ;; "setup                  Setup commands for CLI
  ;; "streaming              Streaming Service
  ;; "support                Support Management
  ;; "usage-api"              "Usage"
  ;; "vault" "Secrets Management"
  ;; "waas" "Web Application Acceleration and Security Services"
;; "work-requests" "Work Requests"

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
     (str "https://" (name endpoint) "." (name region) domain "/" version "/"))))

(defn regional-endpoint
  "Get the regional endpoint for a service name with the correct API version
   Example -> (regional-endpoint :compute :uk-london-1)"
  [service region]
  (let [service (find-map services :name (name service))
        {endpoint :endpoint version :version} service]
    (format-endpoint endpoint region version)))

(defn translate-verb-to-fn [verb]
  (case verb
    :get #'get
    :list #'get
    :create #'post
    :update #'put
    :delete #'delete))

(defn run
  [provider service resource verb req]
  (let [region (-> provider auth/auth->map :region)
        request-fn (translate-verb-to-fn verb)
        endpoint (regional-endpoint service region)
        url (str endpoint (name resource))]
    (apply request-fn [provider url req])))
