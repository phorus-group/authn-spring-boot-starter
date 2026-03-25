package group.phorus.authn.bdd.app.controllers

import group.phorus.authn.bdd.app.dtos.UserDTO
import group.phorus.authn.bdd.app.dtos.UserResponse
import group.phorus.authn.bdd.app.services.UserService
import group.phorus.authn.core.context.AuthContext
import group.phorus.authn.core.context.HTTPContext
import group.phorus.authn.core.dtos.AuthContextData
import group.phorus.mapper.mapping.extensions.mapTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/user")
class UserController(
    private val userService: UserService,
) {
    @GetMapping
    suspend fun findCurrent(@RequestHeader(HttpHeaders.AUTHORIZATION) context: AuthContextData): UserResponse =
        userService.findById(context.userId).mapTo<UserResponse>()!!

    @GetMapping("/withStaticContext")
    suspend fun findCurrentWithStaticContext(): UserResponse {
        val userId = AuthContext.context.get().userId
        return userService.findById(userId).mapTo<UserResponse>()!!
    }

    @GetMapping("/withDispatcher")
    suspend fun findCurrentWithDispatcher(): UserResponse {
        val userId = withContext(Dispatchers.IO) {
            AuthContext.context.get().userId
        }
        val contentType = withContext(Dispatchers.IO) {
            HTTPContext.context.get().contentType
        }
        val user = userService.findById(userId).mapTo<UserResponse>()!!

        if (user.id != userId || contentType != "application/json") throw RuntimeException("Error")
        return user
    }

    @PostMapping
    suspend fun create(
        @RequestBody
        userDTO: UserDTO,
    ): ResponseEntity<Void> = userService.create(userDTO)
        .let { ResponseEntity.created(URI.create("/user/$it")).build() }
}
