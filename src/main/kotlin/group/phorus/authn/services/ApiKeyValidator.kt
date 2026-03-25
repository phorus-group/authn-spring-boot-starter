package group.phorus.authn.services

import org.springframework.http.server.reactive.ServerHttpRequest

/**
 * Interface for custom API key validation.
 *
 * Implement this interface and register it as a Spring bean to provide dynamic API key
 * validation logic. The [group.phorus.authn.filters.ApiKeyFilter] tries static
 * [keys][group.phorus.authn.config.ApiKeyFilterConfiguration.keys] first, then
 * falls back to this validator if no static key matched. Both can be used together.
 *
 * ### Usage
 *
 * ```kotlin
 * @Service
 * class DatabaseApiKeyValidator(
 *     private val apiKeyRepository: ApiKeyRepository,
 * ) : ApiKeyValidator {
 *     override fun validate(apiKey: String, request: ServerHttpRequest?): ApiKeyValidationResult {
 *         val entity = apiKeyRepository.findByKey(apiKey)
 *             ?: throw Unauthorized("Invalid API key")
 *         if (entity.revoked) throw Unauthorized("API key has been revoked")
 *         return ApiKeyValidationResult(
 *             keyId = entity.name,
 *             metadata = mapOf("ownerId" to entity.ownerId.toString()),
 *         )
 *     }
 * }
 * ```
 *
 * @see ApiKeyValidationResult
 * @see group.phorus.authn.filters.ApiKeyFilter
 */
interface ApiKeyValidator {
    /**
     * Validates the provided API key and returns the key's identity and metadata on success,
     * or throws an [group.phorus.exception.core.Unauthorized] exception on failure.
     *
     * @param apiKey The raw API key value extracted from the HTTP header.
     * @param request The incoming HTTP request. Can be used to make validation decisions based on
     *     the request path, headers, or other request attributes (e.g. restrict a key to specific
     *     endpoints or HTTP methods). May be `null` when the validator is invoked outside of a filter
     *     context, for example via the [ApiKeyContextConverter][group.phorus.authn.config.ApiKeyContextConverter]
     *     for `@RequestHeader` parameter injection.
     * @return Validation result with key identity and metadata.
     * @throws group.phorus.exception.core.Unauthorized if the API key is invalid.
     */
    fun validate(apiKey: String, request: ServerHttpRequest?): ApiKeyValidationResult
}

/**
 * Result of a successful [ApiKeyValidator.validate] call.
 *
 * @property keyId Optional identifier for the API key (e.g. partner name, application ID, database key).
 *     Stored in [group.phorus.authn.core.dtos.ApiKeyContextData.keyId].
 * @property metadata Additional key-value pairs to attach to the request context.
 *     Stored in [group.phorus.authn.core.dtos.ApiKeyContextData.metadata].
 *
 * ### Security note
 *
 * When implementing a validator with database or cache lookups, use constant-time comparison
 * (e.g. `MessageDigest.isEqual()`) to compare stored API keys with the provided key. This prevents
 * timing attacks where an attacker could measure response times to progressively guess the key.
 */
data class ApiKeyValidationResult(
    val keyId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
)
