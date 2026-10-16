package group.phorus.authn.bdd.app.controllers

import group.phorus.authn.core.context.AuthenticationContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/protected")
class ProtectedController {

    @GetMapping("/me")
    suspend fun me(): Map<String, Any> {
        val auth = AuthenticationContext.context.get()
        return mapOf(
            "userId" to auth.userId.toString(),
            "roles" to auth.roles,
            "scope" to auth.scope,
        )
    }
}
