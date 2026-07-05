(ns oci-sdk-clj.test-runner
  (:require [clojure.test :as test]
            [oci-sdk-clj.core-test]
            [oci-sdk-clj.java-test]))

(defn -main
  [& _]
  (let [{:keys [fail error]} (test/run-tests 'oci-sdk-clj.core-test
                                             'oci-sdk-clj.java-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
