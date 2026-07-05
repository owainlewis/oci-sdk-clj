(ns oci-sdk-clj.java
  (:require [clojure.string :as str])
  (:import [com.oracle.bmc Region]
           [com.oracle.bmc.auth AuthenticationDetailsProvider]
           [java.io File]
           [java.lang.reflect Method Modifier]
           [java.util.jar JarFile]))

(def default-client-aliases
  {:identity "com.oracle.bmc.identity.IdentityClient"
   :compute "com.oracle.bmc.core.ComputeClient"
   :virtual-network "com.oracle.bmc.core.VirtualNetworkClient"
   :object-storage "com.oracle.bmc.objectstorage.ObjectStorageClient"})

(defn- keyword->lower-camel
  [k]
  (let [[head & tail] (str/split (name k) #"-")]
    (apply str head (map str/capitalize tail))))

(defn- compatible-arg?
  [^Class parameter-type arg]
  (cond
    (nil? arg) (not (.isPrimitive parameter-type))
    (.isPrimitive parameter-type) true
    :else (.isAssignableFrom parameter-type (class arg))))

(defn- compatible-method?
  [^Method method args]
  (let [parameter-types (.getParameterTypes method)]
    (and (= (count args) (alength parameter-types))
         (every? true?
                 (map compatible-arg? parameter-types args)))))

(defn class-name
  "Resolve a Java SDK client alias or fully qualified class name."
  [client-key]
  (cond
    (keyword? client-key) (or (default-client-aliases client-key)
                              (throw (ex-info "Unknown OCI Java client alias"
                                              {:client client-key
                                               :known-aliases (sort (keys default-client-aliases))})))
    (string? client-key) client-key
    (class? client-key) (.getName ^Class client-key)
    :else (throw (ex-info "Client key must be a keyword, class, or class name"
                          {:client client-key}))))

(defn client-class
  "Load a Java SDK client class from an alias or fully qualified class name."
  [client-key]
  (if (class? client-key)
    client-key
    (Class/forName (class-name client-key))))

(defn- static-method
  [^Class cls method-name args]
  (or (some (fn [^Method method]
              (when (and (= method-name (.getName method))
                         (Modifier/isStatic (.getModifiers method))
                         (compatible-method? method args))
                method))
            (.getMethods cls))
      (throw (ex-info "Static method not found"
                      {:class (.getName cls)
                       :method method-name
                       :arg-types (mapv #(some-> % class .getName) args)}))))

(defn- instance-method
  [obj method-name args]
  (let [cls (class obj)]
    (or (some (fn [^Method method]
                (when (and (= method-name (.getName method))
                           (compatible-method? method args))
                  method))
              (.getMethods cls))
        (throw (ex-info "Instance method not found"
                        {:class (.getName cls)
                         :method method-name
                         :arg-types (mapv #(some-> % class .getName) args)})))))

(defn- invoke-static
  [^Class cls method-name & args]
  (.invoke (static-method cls method-name args)
           nil
           (object-array args)))

(defn- invoke-instance
  [obj method-name & args]
  (.invoke (instance-method obj method-name args)
           obj
           (object-array args)))

(defn region-value
  "Resolve an OCI region from a keyword or string such as :uk-london-1."
  [region-id]
  (cond
    (instance? Region region-id) region-id
    (keyword? region-id) (Region/fromRegionId (name region-id))
    (string? region-id) (Region/fromRegionId region-id)
    (nil? region-id) nil
    :else (throw (ex-info "Region must be a keyword, string, or Region"
                          {:region region-id}))))

(defn client
  "Build an OCI Java SDK client.

   client-key can be a known alias such as :identity or a fully qualified
   Java SDK client class name. opts supports :region."
  ([provider client-key]
   (client provider client-key {}))
  ([^AuthenticationDetailsProvider provider client-key {:keys [region] :as _opts}]
   (let [builder (invoke-static (client-class client-key) "builder")]
     (when region
       (invoke-instance builder "region" (region-value region)))
     (invoke-instance builder "build" provider))))

(defn request
  "Build a Java SDK request object from a request class name and setter map.

   Example:
   (request \"com.oracle.bmc.identity.requests.ListUsersRequest\"
            {:compartment-id \"ocid1.compartment...\"})"
  [request-class setters]
  (let [builder (invoke-static (Class/forName request-class) "builder")]
    (doseq [[setter value] setters]
      (invoke-instance builder (keyword->lower-camel setter) value))
    (invoke-instance builder "build")))

(defn call
  "Invoke a Java SDK client operation with one request object."
  [client operation request]
  (invoke-instance client (keyword->lower-camel operation) request))

(defn- client-class-entry?
  [entry-name]
  (and (str/starts-with? entry-name "com/oracle/bmc/")
       (str/ends-with? entry-name "Client.class")
       (not (str/includes? entry-name "/model/"))
       (not (str/includes? entry-name "/internal/"))
       (not (str/includes? entry-name "$"))
       (not (str/ends-with? entry-name "AsyncClient.class"))
       (not (#{"com/oracle/bmc/http/client/HttpClient.class"
               "com/oracle/bmc/http/internal/BaseClient.class"
               "com/oracle/bmc/http/internal/BaseSyncClient.class"}
             entry-name))))

(defn- jar-client-classes
  [^File jar-file]
  (with-open [jar (JarFile. jar-file)]
    (->> (enumeration-seq (.entries jar))
         (map #(.getName %))
         (filter client-class-entry?)
         (map #(-> %
                   (str/replace "/" ".")
                   (str/replace #"\.class$" "")))
         sort
         vec)))

(defn client-inventory
  "Return OCI Java SDK sync client classes visible from the classpath."
  []
  (let [classpath (str/split (System/getProperty "java.class.path") (re-pattern File/pathSeparator))]
    (->> classpath
         (map #(File. %))
         (filter #(.isFile %))
         (filter #(str/ends-with? (.getName %) ".jar"))
         (mapcat jar-client-classes)
         distinct
         sort
         vec)))
