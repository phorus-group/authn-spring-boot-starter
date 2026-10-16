package group.phorus.authn.bdd.app.controllers

import group.phorus.authn.core.context.ApiKeyContext
import group.phorus.authn.core.context.AuthenticationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api-key-protected")
class ApiKeyProtectedController {

    @GetMapping("/identity")
    suspend fun identity(): Map<String, Any?> {
        val apiKey = ApiKeyContext.context.get()
        return mapOf(
            "keyId" to apiKey.keyId,
            "metadata" to apiKey.metadata,
        )
    }

    @GetMapping("/dual")
    suspend fun dual(): Map<String, Any?> {
        val apiKey = ApiKeyContext.context.get()
        val auth = AuthenticationContext.context.get()
        return mapOf(
            "keyId" to apiKey.keyId,
            "userId" to auth.userId.toString(),
            "scope" to auth.scope,
        )
    }
}
