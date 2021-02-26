# oci-sdk-clj

A lightweight and Clojure friendly library for working with Oracle Cloud Infrastructure (OCI).

This library uses the OCI Java SDK for request signing.

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
COMPARTMENT_ID="ocid..." lein test
```
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
