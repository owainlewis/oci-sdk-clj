# oci-sdk-clj

A lightweight and Clojure friendly library for working with Oracle Cloud Infrastruture.

## Usage

You can dispatch any clj-http compatible HTTP request.

```clj
(ns oci-sdk-clj.example
  (:require [oci-sdk-clj.core :as oci]))

(defn sample-request
  [compartment]
  {:request-method :get
   :headers {}
   :query-params {"compartmentId" compartment}
   :url
	 (str "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances"
	   compartment)})

(defn example [compartment]
  (let [auth (oci/config-file-authentication-provider)]
	(->> (oci/request auth (sample-request compartment))
		 :body
		 (mapv :shape))))
```

Get requests

```clj
(ns oci-sdk-clj.example
  (require [oci-sdk-clj.core :as oci]))

(def auth (oci/config-file-authentication-provider))

(oci/get auth
  "https://iaas.us-ashburn-1.oraclecloud.com/20160918/instances?compartmentId=X"
  {:as :json})
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
