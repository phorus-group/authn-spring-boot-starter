package group.phorus.authn.bdd.steps

import group.phorus.authn.bdd.app.controllers.BridgeResponse
import group.phorus.authn.config.SecurityConfiguration
import group.phorus.authn.core.config.AuthMode
import group.phorus.test.commons.bdd.BaseResponseScenarioScope
import group.phorus.test.commons.bdd.BaseScenarioScope
import group.phorus.test.commons.bdd.bodyAs
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.Then
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.springframework.beans.factory.annotation.Autowired
import tools.jackson.databind.ObjectMapper

class IdpBridgeStepsDefinition(
    @Autowired private val baseScenarioScope: BaseScenarioScope,
    @Autowired private val responseScenarioScope: BaseResponseScenarioScope,
    @Autowired private val securityConfiguration: SecurityConfiguration,
    @Autowired private val objectMapper: ObjectMapper,
) {

    @Before("@idp-bridge")
    fun switchToIdpBridgeMode() {
        securityConfiguration.mode = AuthMode.IDP_BRIDGE
    }

    @After("@idp-bridge")
    fun resetToStandaloneMode() {
        securityConfiguration.mode = AuthMode.STANDALONE
    }

    @Then("the response contains a self-issued access token")
    fun `the response contains a self-issued access token`() {
        val response = responseScenarioScope.bodyAs<BridgeResponse>(objectMapper)!!

        assertNotNull(response.accessToken)
        assertNotNull(response.accessToken.token)
        assertTrue(response.accessToken.token.isNotBlank())

        baseScenarioScope.objects["accessToken"] = response.accessToken.token
    }
}
