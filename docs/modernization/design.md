# OCI SDK Clojure Modernization Design

Status: In Review

## Summary

Modernize `oci-sdk-clj` as a Clojure wrapper over Oracle's current OCI Java SDK. Keep the existing signed raw request path for compatibility, but make the Java SDK wrapper the main path for current service coverage.

Opinion [high]: Wrapping the official OCI Java SDK is the only honest path found for broad current OCI endpoint and method coverage in this repo.
This changes if: Oracle publishes a current, complete, public OpenAPI or Swagger catalog for all OCI services that can be used to generate and validate a pure Clojure SDK.

## Context and Scope

The current project is a small Leiningen library. It depends on `com.oracle.oci.sdk/oci-java-sdk-common` `1.32.1`, signs raw `clj-http` requests, and contains a partial hard-coded service table. It cannot prove full OCI coverage.

Oracle's Java SDK docs describe the SDK as the tool for managing OCI resources, list many supported services, and link to GitHub and Maven downloads. The current Java SDK API reference is version `3.91.0`. Oracle also documents Java SDK 3.x changes, including the explicit HTTP client choice for unshaded modular use.

## Goals

- Use current Oracle-maintained SDK code for service coverage.
- Keep raw signed HTTP requests working while the wrapper grows.
- Provide a small Clojure API for building Java SDK clients, building typed requests, and invoking operations.
- Add tests that run without OCI credentials.
- Add a coverage inventory check that states what Java SDK client classes are exposed.

## Non-Goals

- Claim complete Clojure idiomatic wrappers for every service in the first PR.
- Generate a pure Clojure SDK in the first PR.
- Publish to Clojars, change the license, or merge to `master`.
- Run live OCI calls in default tests.

## Constraints

- The repo is EPL-2.0 with GPL secondary terms. The OCI Java SDK is dual licensed under UPL 1.0 and Apache 2.0, so this design does not require changing the repo license.
- Default verification must run without OCI credentials.
- The first PR must avoid unsupported claims about full coverage.
- The raw request path currently uses `DefaultRequestSigner`; this stays as a compatibility path.

## Proposed Design

Add `oci-sdk-clj.java` as a thin bridge over the OCI Java SDK:

- `client` builds a Java SDK client from an authentication provider, an alias such as `:identity`, or a fully qualified Java client class name.
- `request` builds a typed Java SDK request object from a request class name and a Clojure map of builder setters.
- `call` invokes one SDK operation on a client with a typed request object.
- `client-inventory` scans the runtime classpath for exposed sync Java SDK client classes.

Use `com.oracle.oci.sdk/oci-java-sdk-shaded-full` `3.91.0` for the first PR. This pulls the current shaded service distribution onto the classpath and avoids manually listing dozens of service artifacts. A later PR can move to a BOM plus selected modules if library size is more important than broad client availability.

Keep `oci-sdk-clj.core` for signed raw HTTP requests. Fix compile issues, keep the existing `get`, `put`, `post`, `delete`, and `run` functions, and document that this path is legacy-compatible rather than full coverage.

## Public Clojure API Shape

```clj
(require '[oci-sdk-clj.auth :as auth])
(require '[oci-sdk-clj.java :as oci-java])

(def provider (auth/config-file-authentication-details-provider "DEFAULT"))
(def identity (oci-java/client provider :identity {:region :uk-london-1}))

(def request
  (oci-java/request "com.oracle.bmc.identity.requests.ListUsersRequest"
                    {:compartment-id "ocid1.compartment..."}))

(oci-java/call identity :list-users request)
```

For services without aliases, callers can pass a fully qualified Java client class name.

## Coverage Strategy

The Java SDK wrapper coverage is staged:

1. First PR: expose generic client, request, call, and inventory helpers. Prove `IdentityClient`, `ComputeClient`, and at least 100 sync client classes are on the classpath. Do not claim idiomatic Clojure wrappers for all services.
2. Later PRs: add aliases for commonly used clients and optional thin Clojure functions for high-demand operations.
3. Stop rule: any claim of full Clojure API coverage must be backed by generated inventory or tests that enumerate all intended client classes and operations.

## Testing Strategy

- Unit test raw endpoint construction and unsupported verb errors.
- Unit test Java request building without credentials.
- Unit test client inventory without credentials.
- Keep live OCI calls as examples or opt-in tests only, since they require OCI config and tenancy permissions.

## Compatibility Notes

- Existing raw request functions remain in `oci-sdk-clj.core`.
- The `run` helper remains limited by the old hard-coded service table.
- Consumers can migrate incrementally by creating Java SDK clients for new code while leaving existing raw calls in place.

## Migration Path

1. Existing users keep `oci-sdk-clj.core/get`, `post`, `put`, `delete`, and `run`.
2. New code should use `oci-sdk-clj.java/client`, `request`, and `call`.
3. Add aliases and typed convenience functions as demand appears.
4. Deprecate the hard-coded raw service table after enough Java SDK examples and wrappers exist.

## Alternatives Considered

### Pure Clojure Generator

Rejected for now. Oracle publishes REST API docs and a Java SDK, but this research did not find a complete current public OpenAPI or Swagger catalog for all OCI services. Without that, generated coverage would be hard to prove and easy to over-claim.

### Continue Raw Signed Requests

Rejected as the main architecture. Raw requests are useful for escape hatches, but the current service table is partial and endpoint/method coverage would need constant manual maintenance.

### Modular Java SDK Dependencies

Deferred. Oracle supports BOM plus service modules, but the first modernization PR benefits from the shaded full artifact because it exposes current clients with one dependency and makes inventory evidence simple.

## Source Links

- Oracle SDK for Java docs: https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk.htm
- Oracle Java SDK API reference, current version observed as `3.91.0`: https://docs.oracle.com/iaas/tools/java/latest/
- Oracle Java SDK 3.x changes: https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdk3.htm
- Oracle REST API concepts and signing requirement: https://docs.oracle.com/en-us/iaas/Content/API/Concepts/usingapi.htm
- Oracle Java SDK GitHub repo: https://github.com/oracle/oci-java-sdk
- Maven `oci-java-sdk` artifact: https://central.sonatype.com/artifact/com.oracle.oci.sdk/oci-java-sdk
- Maven `oci-java-sdk-bom` artifact: https://central.sonatype.com/artifact/com.oracle.oci.sdk/oci-java-sdk-bom
- Maven `oci-java-sdk-shaded-full` metadata: https://repo1.maven.org/maven2/com/oracle/oci/sdk/oci-java-sdk-shaded-full/maven-metadata.xml

## Open Questions

- Should the published library keep depending on the shaded full artifact, or should a later PR switch to BOM-managed optional service modules?
- Which service aliases should be promoted first beyond Identity, Compute, Virtual Network, and Object Storage?

## Decision

Proceed with the staged Java SDK wrapper. The first PR modernizes the dependency baseline, keeps raw signed requests, adds the generic wrapper, tests credential-free behavior, and adds inventory evidence. Full OCI coverage must remain a future claim until operation-level coverage evidence exists.
