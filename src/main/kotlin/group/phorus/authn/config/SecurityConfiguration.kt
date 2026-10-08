package group.phorus.authn.config

import group.phorus.authn.core.config.AuthMode
import group.phorus.authn.core.config.TokenFormat
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * Root configuration properties for auth-commons, bound to `group.phorus.security.*`.
 *
 * Controls the authentication [mode], token format, key material, IdP integration,
 * and request-filtering behavior for any Spring service that depends on this library.
 *
 * ### Authentication modes
 *
 * | Mode | Description |
 * |------|-------------|
 * | [AuthMode.STANDALONE] | The service creates **and** validates its own tokens. No external Identity Provider is involved. |
 * | [AuthMode.IDP_BRIDGE] | An external IdP issues the initial token. The service validates it, extracts claims, and mints its own tokens (JWS / JWE / nested-JWE) for internal use. Useful when the IdP does not support JWE or when you need a custom token format. |
 * | [AuthMode.IDP_DELEGATED] | The service only validates tokens issued by an external IdP. No own token creation, refresh is handled by the IdP. |
 *
 * ### Token formats (applicable to [AuthMode.STANDALONE] and [AuthMode.IDP_BRIDGE])
 *
 * | Format | Description |
 * |--------|-------------|
 * | [TokenFormat.JWS] | Signed-only JWT ([RFC 7515](https://datatracker.ietf.org/doc/html/rfc7515)). Provides integrity and authenticity. |
 * | [TokenFormat.JWE] | Encrypted-only JWT ([RFC 7516](https://datatracker.ietf.org/doc/html/rfc7516)). Claims are placed directly in the encrypted payload without an inner signature. |
 * | [TokenFormat.NESTED_JWE] | Claims are first signed as a JWS, then the JWS is encrypted as the payload of a JWE with `cty: "JWT"` ([RFC 7516 §A.2](https://datatracker.ietf.org/doc/html/rfc7516#appendix-A.2), [RFC 7519 §5.2](https://datatracker.ietf.org/doc/html/rfc7519#section-5.2)). Provides both integrity **and** confidentiality. |
 *
 * @see AuthMode
 * @see TokenFormat
 * @see JwtConfiguration
 * @see IdpConfiguration
 */
@AutoConfiguration
@ConfigurationProperties(prefix = "group.phorus.security")
class SecurityConfiguration(
    /** Authentication mode. Defaults to [AuthMode.STANDALONE]. */
    var mode: AuthMode = AuthMode.STANDALONE,

    /**
     * Per-filter configuration for authentication strategies.
     *
     * Each filter runs independently as a separate [org.springframework.web.server.CoWebFilter].
     * A request must satisfy **all** active filters unless the request path is in that
     * filter's specific [Path] ignore list.
     *
     * @see TokenFilterConfiguration
     * @see ApiKeyFilterConfiguration
     */
    @NestedConfigurationProperty
    var filters: FiltersConfiguration = FiltersConfiguration(),

    /** JWT creation, parsing, signing, and encryption settings. */
    @NestedConfigurationProperty
    var jwt: JwtConfiguration = JwtConfiguration(),

    /** External Identity Provider settings. Required when [mode] is [AuthMode.IDP_BRIDGE] or [AuthMode.IDP_DELEGATED]. */
    @NestedConfigurationProperty
    var idp: IdpConfiguration = IdpConfiguration(),

    /** SCrypt password encoder tuning parameters. */
    @NestedConfigurationProperty
    var passwordEncoder: PasswordEncoderConfiguration = PasswordEncoderConfiguration(),
)

// AuthMode and TokenFormat are defined in authn-core (group.phorus.authn.core.config)
// and are available here via the api(authn-core) dependency.

/**
 * A path pattern that configures which requests a filter should include or exclude.
 *
 * ### Path matching
 *
 * All paths use Spring [PathPattern][org.springframework.web.util.pattern.PathPattern] syntax.
 * For details of the path pattern syntax see [PathPattern][org.springframework.web.util.pattern.PathPattern].
 *
 * ### HTTP method filtering
 *
 * When [method] is set, only requests matching both the path pattern and the HTTP method are
 * affected. When [method] is `null`, all HTTP methods are matched.
 *
 * @property path Spring PathPattern string.
 * @property method Optional HTTP method constraint (e.g. `POST`). When `null`, all methods are matched.
 */
class Path(
    var path: String,
    var method: String? = null,
)

/**
 * Container for per-filter configuration. Each property corresponds to an authentication
 * strategy that runs as an independent [org.springframework.web.server.CoWebFilter].
 *
 * ### Execution order
 *
 * | Order | Filter | Context populated |
 * |-------|--------|-------------------|
 * | 1 | [group.phorus.authn.filters.ApiKeyFilter] | [group.phorus.authn.core.context.ApiKeyContext] |
 * | 2 | [group.phorus.authn.filters.AuthFilter] | [group.phorus.authn.core.context.AuthContext] |
 *
 * ### Composability
 *
 * When multiple filters are enabled, a request must satisfy **all** of them unless the
 * request path appears in that filter's specific [ignoredPaths][TokenFilterConfiguration.ignoredPaths].
 *
 * @see TokenFilterConfiguration
 * @see ApiKeyFilterConfiguration
 */
class FiltersConfiguration(
    /** JWT / Bearer token authentication filter. */
    @NestedConfigurationProperty
    var token: TokenFilterConfiguration = TokenFilterConfiguration(),

    /** API key authentication filter. */
    @NestedConfigurationProperty
    var apiKey: ApiKeyFilterConfiguration = ApiKeyFilterConfiguration(),
)

/**
 * Configuration for the JWT / Bearer token authentication filter.
 *
 * This filter extracts the `Authorization: Bearer <token>` header, validates the token
 * using [group.phorus.authn.core.services.Authenticator] or
 * [group.phorus.authn.services.impl.IdpAuthenticator], and populates
 * [group.phorus.authn.core.context.AuthContext].
 *
 * ### Path filtering modes
 *
 * Only one of [ignoredPaths] or [protectedPaths] may be set (non-empty) at a time:
 * - **[ignoredPaths]**: all paths require authentication **except** the listed ones.
 * - **[protectedPaths]**: **only** the listed paths require authentication; everything else is skipped.
 *
 * If both are non-empty, the application fails at startup with an [IllegalArgumentException].
 *
 * ### Path matching
 *
 * All paths use Spring [PathPattern][org.springframework.web.util.pattern.PathPattern] syntax.
 * For details of the path pattern syntax see [PathPattern][org.springframework.web.util.pattern.PathPattern].
 *
 * @property enabled Whether the token filter is active. Defaults to `false`.
 * @property refreshTokenPath Path where refresh tokens are accepted, in Spring PathPattern syntax. All other paths reject them.
 * @property ignoredPaths Paths that bypass token authentication. Uses Spring PathPattern syntax. Mutually exclusive with [protectedPaths].
 * @property protectedPaths Paths that require token authentication; all others are skipped. Uses Spring PathPattern syntax. Mutually exclusive with [ignoredPaths].
 */
class TokenFilterConfiguration(
    var enabled: Boolean = false,
    var refreshTokenPath: String? = null,
    var ignoredPaths: List<Path> = emptyList(),
    var protectedPaths: List<Path> = emptyList(),
)

/**
 * Configuration for the API key authentication filter.
 *
 * This filter extracts the API key from the configured [header] and validates it using
 * the following chain:
 *
 * 1. **Static keys**: if [keys] is non-empty, the header value is compared against the map values.
 *    On match, the map key becomes the key id.
 * 2. **Custom validator**: if an [ApiKeyValidator][group.phorus.authn.services.ApiKeyValidator]
 *    bean exists and no static key matched, the validator is called as a fallback.
 * 3. If neither matches, the request is rejected with a 401. If neither is configured, an
 *    [IllegalStateException] is thrown at request time.
 *
 * Both static keys and a custom validator can be used together. On successful validation,
 * [group.phorus.authn.core.context.ApiKeyContext] is populated with the resolved key
 * identity and any metadata from the validator.
 *
 * ### Path filtering modes
 *
 * Only one of [ignoredPaths] or [protectedPaths] may be set (non-empty) at a time:
 * - **[ignoredPaths]**: all paths require API key authentication **except** the listed ones.
 * - **[protectedPaths]**: **only** the listed paths require API key authentication; everything else is skipped.
 *
 * If both are non-empty, the application fails at startup with an [IllegalArgumentException].
 *
 * ### Path matching
 *
 * All paths use Spring [PathPattern][org.springframework.web.util.pattern.PathPattern] syntax.
 * For details of the path pattern syntax see [PathPattern][org.springframework.web.util.pattern.PathPattern].
 *
 * @property enabled Whether the API key filter is active. Defaults to `false`.
 * @property header HTTP header name to read the API key from. Defaults to `X-API-KEY`.
 * @property keys Static named API keys. Map key = key identifier, map value = the secret key.
 * @property ignoredPaths Paths that bypass API key authentication. Uses Spring PathPattern syntax. Mutually exclusive with [protectedPaths].
 * @property protectedPaths Paths that require API key authentication; all others are skipped. Uses Spring PathPattern syntax. Mutually exclusive with [ignoredPaths].
 */
class ApiKeyFilterConfiguration(
    var enabled: Boolean = false,
    var header: String = "X-API-KEY",
    var keys: Map<String, String> = emptyMap(),
    var ignoredPaths: List<Path> = emptyList(),
    var protectedPaths: List<Path> = emptyList(),
)

/**
 * JWT-level configuration: issuer, token format, signing keys, encryption keys, and expiration.
 *
 * @see JwtSigningConfiguration
 * @see JwtEncryptionConfiguration
 * @see JwtExpirationConfiguration
 */
class JwtConfiguration(
    /**
     * The `iss` (issuer) claim written into every token created by this library. An incoming token's
     * `iss` is checked against it only when [requireIssuer] is `true`.
     */
    var issuer: String? = null,

    /**
     * Token serialization format. Defaults to [TokenFormat.JWS]. Use [TokenFormat.JWE] when claims
     * must be confidential, or [TokenFormat.NESTED_JWE] when both integrity and confidentiality are required.
     */
    var tokenFormat: TokenFormat = TokenFormat.JWS,

    /** Signing key material. Required when [tokenFormat] is [TokenFormat.JWS] or [TokenFormat.NESTED_JWE]. */
    @NestedConfigurationProperty
    var signing: JwtSigningConfiguration = JwtSigningConfiguration(),

    /** Encryption key material. Required when [tokenFormat] is [TokenFormat.JWE] or [TokenFormat.NESTED_JWE]. */
    @NestedConfigurationProperty
    var encryption: JwtEncryptionConfiguration = JwtEncryptionConfiguration(),

    /** Access-token and refresh-token lifetimes. */
    @NestedConfigurationProperty
    var expiration: JwtExpirationConfiguration = JwtExpirationConfiguration(),

    /** Claim names for the tokens this library issues and validates. */
    @NestedConfigurationProperty
    var claims: JwtClaimsMapping = JwtClaimsMapping(),

    /**
     * The `aud` (audience) claim written into every token created by this library, naming the service
     * the token is for, per [RFC 7519 §4.1.3](https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.3).
     */
    var audience: String? = null,

    /**
     * When `true`, an incoming token is accepted only if its `aud` claim holds [audience].
     * Needs [audience] to be set.
     *
     * An absent or different `aud` is refused, so a running service sets [audience], waits out the
     * longest lifetime in [expiration], and turns this on after that.
     */
    var requireAudience: Boolean = false,

    /**
     * When `true`, an incoming token is accepted only if its `iss` claim holds [issuer].
     * Needs [issuer] to be set.
     *
     * An absent or different `iss` is refused, so a running service sets [issuer], waits out
     * the longest lifetime in [expiration], and turns this on after that.
     */
    var requireIssuer: Boolean = false,

    /**
     * How far an `exp` or `nbf` claim may be off before it is held against the token, which covers
     * clock drift between the issuing and validating services per
     * [RFC 7519 §4.1.4](https://datatracker.ietf.org/doc/html/rfc7519#section-4.1.4).
     */
    var clockSkewSeconds: Long = 0,
)

/**
 * Claim names for the tokens this library issues and validates.
 *
 * Each default is the claim name its RFC assigns, so a token this library both writes and reads needs
 * no configuration here.
 *
 * A name has to be a flat claim name. The issuing side writes it as a top-level claim, so
 * `realm_access.roles` would become one key containing a dot:
 *
 * ```json
 * { "realm_access.roles": ["ADMIN"] }
 * ```
 *
 * while the reading side treats a dot as a path into a nested object:
 *
 * ```json
 * { "realm_access": { "roles": ["ADMIN"] } }
 * ```
 *
 * Dot notation belongs to [IdpClaimsMapping], where the token comes from somewhere else. A dotted name
 * here is refused at startup.
 *
 * @property scope The claim that holds delegated application authority. Defaults to `"scope"`.
 * @property roles The claim that holds subject entitlement. Defaults to `"roles"`.
 */
class JwtClaimsMapping(
    var scope: String = "scope",
    var roles: String = "roles",
)

/**
 * Signing key configuration for JWS and nested-JWE token formats.
 *
 * Uses asymmetric key pairs: the private key signs tokens, the public key verifies them.
 *
 * @property algorithm JCA key-factory algorithm name (e.g. `"EC"`, `"RSA"`).
 * @property signatureAlgorithm JJWT signature algorithm identifier (e.g. `"ES384"`, `"RS256"`).
 *     When `null`, JJWT selects the strongest algorithm supported by the key.
 * @property encodedPrivateKey Base64-encoded PKCS#8 private key used for **signing**.
 * @property encodedPublicKey Base64-encoded X.509 public key used for **verification**.
 */
class JwtSigningConfiguration(
    var algorithm: String = "EC",
    var signatureAlgorithm: String? = null,
    var encodedPrivateKey: String? = null,
    var encodedPublicKey: String? = null,
)

/**
 * Token lifetime configuration.
 *
 * @property tokenMinutes Access-token lifetime in minutes. Defaults to `10`.
 * @property refreshTokenMinutes Refresh-token lifetime in minutes. Defaults to `1440` (24 h).
 */
class JwtExpirationConfiguration(
    var tokenMinutes: Long = 10,
    var refreshTokenMinutes: Long = 1440,
)

/**
 * Encryption key configuration for JWE and nested-JWE token formats.
 *
 * Uses asymmetric key pairs: the public key encrypts tokens, the private key decrypts them.
 *
 * @property algorithm JCA key-factory algorithm name (e.g. `"EC"`, `"RSA"`).
 * @property keyAlgorithm JJWT key-management algorithm identifier (e.g. `"ECDH-ES+A256KW"`, `"RSA-OAEP-256"`).
 * @property aeadAlgorithm JJWT content-encryption (AEAD) algorithm identifier (e.g. `"A256CBC-HS512"`, `"A192CBC-HS384"`).
 * @property encodedPublicKey Base64-encoded X.509 public key used for **encryption**.
 * @property encodedPrivateKey Base64-encoded PKCS#8 private key used for **decryption**.
 */
class JwtEncryptionConfiguration(
    var algorithm: String = "EC",
    var keyAlgorithm: String = "ECDH-ES+A256KW",
    var aeadAlgorithm: String = "A192CBC-HS384",
    var encodedPublicKey: String? = null,
    var encodedPrivateKey: String? = null,
)

/**
 * External Identity Provider (IdP) configuration.
 *
 * Required when [SecurityConfiguration.mode] is [AuthMode.IDP_BRIDGE] or [AuthMode.IDP_DELEGATED].
 *
 * @property issuerUri The IdP's issuer identifier (e.g. `https://idp.example.com`).
 *     Used to validate the `iss` claim of incoming IdP tokens.
 * @property jwkSetUri URL of the IdP's JWKS endpoint (e.g. `https://idp.example.com/.well-known/jwks.json`).
 *     Public keys are fetched from here to verify IdP token signatures.
 * @property jwksCacheTtlMinutes How long fetched JWKS keys are cached before a refresh. Defaults to `60`.
 * @property claims Mapping from IdP claim names to the internal representation.
 * @property encryption Optional encryption configuration for decrypting IdP JWE or nested JWE tokens.
 *     Required when the IdP sends encrypted tokens (e.g. OIDC Message-Level Encryption).
 */
class IdpConfiguration(
    var issuerUri: String? = null,
    var jwkSetUri: String? = null,
    var jwksCacheTtlMinutes: Long = 60,

    @NestedConfigurationProperty
    var claims: IdpClaimsMapping = IdpClaimsMapping(),

    @NestedConfigurationProperty
    var encryption: IdpEncryptionConfiguration = IdpEncryptionConfiguration(),
)

/**
 * Encryption configuration for decrypting JWE tokens from an IdP.
 *
 * Some IdPs support Message-Level Encryption (MLE), where tokens are encrypted with your
 * public key. You decrypt them with the corresponding private key configured here.
 *
 * For nested JWE tokens (JWE wrapping a JWS), the library decrypts the outer JWE first,
 * then verifies the inner JWS signature using the IdP's JWKS public keys.
 *
 * @property algorithm Key algorithm (e.g. `"RSA"`, `"EC"`). Must match the key type.
 * @property encodedPrivateKey Base64-encoded PKCS#8 private key for decryption.
 */
class IdpEncryptionConfiguration(
    var algorithm: String = "RSA",
    var encodedPrivateKey: String? = null,
)

/**
 * Maps IdP token claim names to the internal claim names expected by auth-commons.
 *
 * Different IdPs name these claims differently, so Keycloak serves roles under `realm_access.roles`,
 * Auth0 under `permissions` and Azure AD scope under `scp`. This mapping normalizes them.
 *
 * A value containing `.` is read as a path into a nested object, which is what Keycloak's
 * `realm_access.roles` requires.
 *
 * `roles` and `scope` are two independent claims, each read into its own field. Per
 * [RFC 6749 §3.3](https://datatracker.ietf.org/doc/html/rfc6749#section-3.3) `scope` is what the
 * calling application was granted consent to do, while `roles`, registered by
 * [RFC 9068 §7.2](https://datatracker.ietf.org/doc/html/rfc9068#section-7.2), is a property of the
 * subject regardless of which application calls.
 *
 * @property subject The claim that contains the user identifier. Defaults to `"sub"`.
 * @property scope The claim that holds delegated application authority. Defaults to `"scope"`.
 * @property roles The claim that holds subject entitlement. Defaults to `"roles"`.
 */
class IdpClaimsMapping(
    var subject: String = "sub",
    var scope: String = "scope",
    var roles: String = "roles",
)

/**
 * SCrypt password encoder tuning parameters.
 *
 * SCrypt is a memory-hard key derivation function designed to make brute-force attacks expensive.
 * Memory usage per hash: ~128 * [cpuCost] * [memoryCost] * [parallelization] bytes.
 *
 * The defaults provide a good balance between security and performance (~8 MB, ~50-100 ms per hash).
 *
 * | Preset | cpuCost | Memory | Approx. time |
 * |--------|---------|--------|---------------|
 * | **Default** | 8192 | ~8 MB | ~50-100 ms |
 * | High security | 16384 | ~16 MB | ~100-200 ms |
 * | Maximum | 65536 | ~64 MB | ~1-2 s |
 *
 * @property cpuCost CPU/memory cost parameter (N). Must be a power of 2. Higher = slower and more memory.
 * @property memoryCost Block size parameter (r). Higher values increase memory usage per block.
 * @property parallelization Parallelization parameter (p). Higher values multiply memory usage.
 * @property keyLength Length of the derived key in bytes. 32 bytes = 256 bits.
 * @property saltLength Length of the random salt in bytes. 16 bytes = 128 bits.
 */
class PasswordEncoderConfiguration(
    var cpuCost: Int = 8192,
    var memoryCost: Int = 8,
    var parallelization: Int = 1,
    var keyLength: Int = 32,
    var saltLength: Int = 16,
)
