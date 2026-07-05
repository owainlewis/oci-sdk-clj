# oci-sdk-clj

A lightweight and Clojure friendly library for working with Oracle Cloud Infrastructure (OCI).

This library uses the OCI Java SDK for request signing and can wrap current OCI Java SDK clients.

By default, the dependency set is intentionally lean. Add the OCI Java SDK service modules you need, or use the full shaded SDK when you want every client available.

## Usage

The most basic usage allows you to dispatch API request manually. You can use any clojure-http compatable
request.

```clj
(ns oci-sdk-clj.example
  (:require [oci-sdk-clj.auth :as auth]
            [oci-sdk-clj.core :as oci]))

(def provider (auth/config-file-authentication-details-provider "DEFAULT"))

;; Get a  list of available OCI compute shapes
(oci/run provider :compute :shapes :list {:query-params {:compartmentId compartment-ocid}}))
```

For new code, prefer the Java SDK wrapper path. It uses typed OCI Java SDK clients and requests while keeping the call site small:

```clj
(ns oci-sdk-clj.example
  (:require [oci-sdk-clj.auth :as auth]
            [oci-sdk-clj.java :as oci-java]))

(def provider (auth/config-file-authentication-details-provider "DEFAULT"))
(def identity (oci-java/client provider :identity {:region :uk-london-1}))

(defn list-users [compartment-ocid]
  (let [request (oci-java/request "com.oracle.bmc.identity.requests.ListUsersRequest"
                                  {:compartment-id compartment-ocid})]
    (oci-java/call identity :list-users request)))
```

For the example above, add the Identity service module and an HTTP client:

```clj
{:deps {com.owainlewis/oci-sdk-clj {:mvn/version "0.1.0"}
        com.oracle.oci.sdk/oci-java-sdk-common-httpclient-jersey {:mvn/version "3.91.0"}
        com.oracle.oci.sdk/oci-java-sdk-identity {:mvn/version "3.91.0"}}}
```

For full Java SDK client availability, add:

```clj
{:deps {com.oracle.oci.sdk/oci-java-sdk-shaded-full {:mvn/version "3.91.0"}}}
```

See `docs/modules.md` for the module strategy.

You can also construct HTTP request manually. For example:

```clj
(ns oci-sdk-clj.example
  (:require [oci-sdk-clj.auth :as auth]
            [oci-sdk-clj.core :as oci]))

(def provider (auth/config-file-authentication-details-provider "DEFAULT"))

(defn bare-metal-shapes
  "Return a list of all available bare metal compute shapes"
  [compartment-ocid]
  (let [all-shapes (oci/get provider "https://iaas.uk-london-1.oraclecloud.com/20160918/shapes/"
                     {:query-params {:compartmentId compartment-ocid}})]
    (filter (fn [shape]
              (clojure.string/starts-with? shape "BM"))
            (mapv :shape all-shapes)))))

["BM.Standard2.52"
 "BM.Standard.E3.128"
 "BM.Standard.E2.64"
 "BM.Standard1.36"
 "BM.Standard2.52"
 "BM.Standard.E3.128"
 "BM.Standard.E2.64"
 "BM.Standard1.36"
 "BM.Standard2.52"
 "BM.Standard.E3.128"
 "BM.Standard.E2.64"
 "BM.Standard1.36"]
```



## Running tests

```
clojure -M:test
clojure -M:fmt check
clojure -M:oci/full:inventory
clojure -T:build jar
```

The default tests do not require OCI credentials. Live OCI calls require a configured OCI profile and tenancy permissions.
## License

Copyright © 2020 Owain Lewis <owain@owainlewis.com>

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
