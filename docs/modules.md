# OCI Java SDK Module Strategy

`oci-sdk-clj` uses Oracle's Java SDK for compatibility, but the default dependency set should stay lean.

## Default Dependency Set

The base project depends on `oci-java-sdk-common` only. That is enough for shared OCI types, auth providers, regions, and raw request signing.

It does not pull every OCI service client into downstream projects.

## Selected Service Modules

Applications can add only the OCI Java SDK modules they need. For example, a Compute-only application needs the HTTP client and Core service module:

```clj
{:deps {com.owainlewis/oci-sdk-clj {:mvn/version "0.1.0"}
        com.oracle.oci.sdk/oci-java-sdk-common-httpclient-jersey {:mvn/version "3.91.0"}
        com.oracle.oci.sdk/oci-java-sdk-core {:mvn/version "3.91.0"}}}
```

The repository has matching aliases for development:

```sh
clojure -M:oci/http-jersey:oci/core
```

## Full Coverage Mode

For discovery, tests, or users who value coverage more than dependency size, use the shaded full distribution:

```sh
clojure -M:oci/full:inventory
```

This exposes all Java SDK clients in one dependency, but it is large.

Opinion [high]: The library should publish a lean base artifact and document opt-in OCI Java SDK modules, rather than forcing the full shaded SDK on every consumer.
This changes if: most users want a batteries-included SDK and do not care about dependency size.

## Future Packaging Options

- Keep one lean artifact and document service-module dependencies.
- Add convenience modules later, such as `oci-sdk-clj-core`, `oci-sdk-clj-object-storage`, and `oci-sdk-clj-full`.
- Generate alias and dependency docs from the OCI Java SDK BOM so supported modules stay current.
- Add operation inventory on top of client inventory before making any full coverage claim.
