package group.phorus.authn.bdd.steps

import group.phorus.authn.bdd.app.dtos.AuthResponse
import group.phorus.authn.bdd.app.dtos.LoginData
import group.phorus.authn.bdd.app.repositories.UserRepository
import group.phorus.authn.core.dtos.AccessToken
import group.phorus.test.commons.bdd.BaseRequestScenarioScope
import group.phorus.test.commons.bdd.BaseResponseScenarioScope
import group.phorus.test.commons.bdd.BaseScenarioScope
import group.phorus.test.commons.bdd.bodyAs
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import org.junit.jupiter.api.Assertions.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder
import tools.jackson.databind.ObjectMapper
import kotlin.collections.set


class AuthStepsDefinition(
    @Autowired private val baseScenarioScope: BaseScenarioScope,
    @Autowired private val requestScenarioScope: BaseRequestScenarioScope,
    @Autowired private val responseScenarioScope: BaseResponseScenarioScope,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @Given("the caller has the given login information:")
    fun `the caller has the given login information`(data: DataTable) {
        val loginData = data.asMaps().first().let {
            LoginData(
                email = it["email"],
                password = it["password"],
                device = it["device"],
                expires = it["expires"].toBoolean(),
            )
        }

        requestScenarioScope.request = loginData
    }


    @Then("the service returns the AuthResponse")
    fun `the service returns the AuthResponse`() {
        val response = responseScenarioScope.bodyAs<AuthResponse>(objectMapper)!!

        assertNotNull(response)

        baseScenarioScope.objects["loginResponse"] = response
        baseScenarioScope.objects["accessToken"] = response.accessToken.token
        baseScenarioScope.objects["refreshToken"] = response.refreshToken
    }

    @Then("the service returns the AccessToken")
    fun `the service returns the AccessToken`() {
        val response = responseScenarioScope.bodyAs<AccessToken>(objectMapper)!!

        assertNotNull(response)

        baseScenarioScope.objects["accessToken"] = response.token
    }
}
