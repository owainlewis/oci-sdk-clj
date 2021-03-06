(defproject com.owainlewis/oci-sdk-clj "0.1.0-SNAPSHOT"
  :description "Clojure SDK for Oracle Cloud Infrastructure (OCI)"
  :url "https://github.com/owainlewis/oci-sdk-clj"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[lein-cljfmt "0.7.0"]]
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.oracle.oci.sdk/oci-java-sdk-common "1.32.1" :exclusions [net.minidev/json-smart]]
                 [org.slf4j/slf4j-simple "1.7.30"]
                 [cheshire "5.10.0"]
                 [clj-http "3.12.0"]]
  :repl-options {:init-ns oci-sdk-clj.core})
