package group.phorus.authn.bdd.steps

import group.phorus.authn.bdd.app.model.Device
import group.phorus.authn.bdd.app.repositories.DeviceRepository
import group.phorus.authn.bdd.app.repositories.UserRepository
import group.phorus.authn.core.dtos.AccessToken
import group.phorus.authn.core.services.Authenticator
import group.phorus.authn.core.services.TokenFactory
import group.phorus.test.commons.bdd.BaseScenarioScope
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

class ValidatorStepsDefinition(
    @Autowired private val baseScenarioScope: BaseScenarioScope,
    @Autowired private val tokenFactory: TokenFactory,
    @Autowired private val authenticator: Authenticator,
    @Autowired private val deviceRepository: DeviceRepository,
    @Autowired private val userRepository: UserRepository,
) {
    @Given("the caller has a token for that user with privileges {string} and claim {string} = {string}")
    fun `the caller has a token for that user with custom claim`(privileges: String, claimName: String, claimValue: String) {
        val userId = baseScenarioScope.objects["userId"] as String
        val privilegesList = privileges.split(" ").filter { it.isNotBlank() }

        val accessToken = runBlocking {
            tokenFactory.createAccessToken(
                UUID.fromString(userId),
                privilegesList,
                mapOf(claimName to claimValue),
            )
        }

        // Extract JTI and create device entry so DeviceValidator passes
        val authData = authenticator.authenticate(accessToken.token, enableValidators = false)
        val user = userRepository.findById(UUID.fromString(userId)).get()

        deviceRepository.save(Device(
            name = "validator-device-${UUID.randomUUID()}",
            user = user,
            accessTokenJTI = authData.jti,
            refreshTokenJTI = "not-used",
            disabled = false
        ))

        baseScenarioScope.objects["accessToken"] = accessToken.token
    }

    @Given("the caller has a token for that user with roles {string}")
    fun `the caller has a token for that user with roles`(roles: String) {
        val userId = baseScenarioScope.objects["userId"] as String
        val rolesList = roles.split(" ").filter { it.isNotBlank() }

        val accessToken = runBlocking {
            tokenFactory.createAccessToken(
                UUID.fromString(userId),
                mapOf("roles" to rolesList),
            )
        }

        val authData = authenticator.authenticate(accessToken.token, enableValidators = false)
        val user = userRepository.findById(UUID.fromString(userId)).get()

        deviceRepository.save(Device(
            name = "roles-device-${UUID.randomUUID()}",
            user = user,
            accessTokenJTI = authData.jti,
            refreshTokenJTI = "not-used",
            disabled = false
        ))

        baseScenarioScope.objects["accessToken"] = accessToken.token
        baseScenarioScope.objects["mintedAccessToken"] = accessToken
    }

    @Then("the minted token carries roles {string}")
    fun `the minted token carries roles`(roles: String) {
        val accessToken = baseScenarioScope.objects["mintedAccessToken"] as AccessToken
        val rolesList = roles.split(" ").filter { it.isNotBlank() }

        assertEquals(rolesList, accessToken.roles)
        assertTrue(accessToken.scope.isEmpty(), "Expected no scope, found ${accessToken.scope}")
    }
}
