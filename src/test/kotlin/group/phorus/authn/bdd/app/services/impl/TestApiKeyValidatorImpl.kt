package group.phorus.authn.bdd.app.services.impl

import group.phorus.authn.services.ApiKeyValidationResult
import group.phorus.authn.services.ApiKeyValidator
import group.phorus.exception.core.Unauthorized
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Service

@Service
class TestApiKeyValidatorImpl : ApiKeyValidator {

    private val validKeys = mapOf(
        "dynamic-key-123" to ApiKeyValidationResult(
            keyId = "dynamic-partner",
            metadata = mapOf(
                "partnerId" to "partner-dynamic",
                "tier" to "premium"
            )
        ),
        "webhook-key-456" to ApiKeyValidationResult(
            keyId = "webhook-service",
            metadata = mapOf(
                "service" to "webhooks",
                "environment" to "test"
            )
        )
    )

    override fun validate(apiKey: String, request: ServerHttpRequest?): ApiKeyValidationResult {
        return validKeys[apiKey] ?: throw Unauthorized("Invalid API key")
    }
}
