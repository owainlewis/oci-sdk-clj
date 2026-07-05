(require '[oci-sdk-clj.java :as oci-java])

(let [clients (oci-java/client-inventory)]
  (println "OCI Java SDK sync clients exposed:" (count clients))
  (doseq [client (take 25 clients)]
    (println client))
  (when (> (count clients) 25)
    (println "...")))
