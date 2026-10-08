package group.phorus.authn.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SecurityConfigurationExtTest {

    @Nested
    @DisplayName("toAuthNConfig carries the jwt properties")
    inner class JwtProperties {

        private fun configuration() = SecurityConfiguration(
            jwt = JwtConfiguration(
                issuer = "phorus.group",
                claims = JwtClaimsMapping(scope = "scp", roles = "entitlements"),
                audience = "sb-backend",
                requireAudience = true,
                requireIssuer = true,
                clockSkewSeconds = 45,
            ),
        )

        @Test
        fun `claim names reach the core config`() {
            val jwt = configuration().toAuthNConfig().jwt

            assertEquals("scp", jwt.claims.scope)
            assertEquals("entitlements", jwt.claims.roles)
        }

        @Test
        fun `audience and its requirement reach the core config`() {
            val jwt = configuration().toAuthNConfig().jwt

            assertEquals("sb-backend", jwt.audience)
            assertEquals(true, jwt.requireAudience)
        }

        @Test
        fun `issuer requirement reaches the core config`() {
            assertEquals(true, configuration().toAuthNConfig().jwt.requireIssuer)
        }

        @Test
        fun `clock skew reaches the core config`() {
            assertEquals(45L, configuration().toAuthNConfig().jwt.clockSkewSeconds)
        }

        @Test
        fun `defaults are carried rather than invented`() {
            val jwt = SecurityConfiguration().toAuthNConfig().jwt

            assertEquals("scope", jwt.claims.scope)
            assertEquals("roles", jwt.claims.roles)
            assertEquals(null, jwt.audience)
            assertEquals(false, jwt.requireAudience)
            assertEquals(false, jwt.requireIssuer)
            assertEquals(0L, jwt.clockSkewSeconds)
        }
    }

    @Nested
    @DisplayName("toAuthNConfig carries the idp claim names")
    inner class IdpProperties {

        @Test
        fun `claim names reach the core config`() {
            val configuration = SecurityConfiguration(
                idp = IdpConfiguration(
                    claims = IdpClaimsMapping(
                        subject = "oid",
                        scope = "scp",
                        roles = "realm_access.roles",
                    ),
                ),
            )

            val claims = configuration.toAuthNConfig().idp.claims

            assertEquals("oid", claims.subject)
            assertEquals("scp", claims.scope)
            assertEquals("realm_access.roles", claims.roles)
        }
    }
}
