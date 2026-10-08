package group.phorus.authn.services.impl

import group.phorus.authn.config.JwtConfiguration
import group.phorus.authn.config.JwtSigningConfiguration
import group.phorus.authn.config.SecurityConfiguration
import group.phorus.authn.core.config.TokenFormat
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

class TokenFactoryImplTest {

    companion object {
        private const val SIG_PRIVATE_KEY =
            "MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDCFB8ZviusbeTHR/iqQqS+rwbVhAWg/+oZ2gFjJ2yQRwNg9mH5MHexS06oTTjncWaihZANiAASktw4raXCAg6DL/7p0ypZKnGhpzBtXKWbndB2alBcZtykvNO+nOCyf2PVua14ppyFgZQC3V+TwQ1uTgOXf34SgTYj+qgkRzRuQfBFEgozMKqxUBQx0SmRZUpnv5AhmK6E="
        private const val SIG_PUBLIC_KEY =
            "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAEpLcOK2lwgIOgy/+6dMqWSpxoacwbVylm53QdmpQXGbcpLzTvpzgsn9j1bmteKachYGUAt1fk8ENbk4Dl39+EoE2I/qoJEc0bkHwRRIKMzCqsVAUMdEpkWVKZ7+QIZiuh"

        private val TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001")

        private fun factory() = TokenFactoryImpl(
            SecurityConfiguration(
                jwt = JwtConfiguration(
                    issuer = "phorus.group",
                    tokenFormat = TokenFormat.JWS,
                    signing = JwtSigningConfiguration(
                        algorithm = "EC",
                        encodedPrivateKey = SIG_PRIVATE_KEY,
                        encodedPublicKey = SIG_PUBLIC_KEY,
                    ),
                ),
            ),
        )

        /** The claim set of a JWS, decoded from the second segment. */
        private fun payload(token: String): String =
            Base64.getUrlDecoder().decode(token.split('.')[1]).toString(Charsets.UTF_8)
    }

    @Nested
    @DisplayName("the claims-map overrides reach TokenCreator")
    inner class ClaimsMapOverrides {

        @Test
        fun `an access token carries a list claim as a JSON array`() = runBlocking {
            val token = factory().createAccessToken(
                TEST_USER_ID,
                mapOf("roles" to listOf("ADMIN@organization:9b1c", "VIEWER@organization:7a21")),
            )

            assertEquals(
                true,
                payload(token.token).contains("""["ADMIN@organization:9b1c","VIEWER@organization:7a21"]"""),
            )
            assertEquals(listOf("ADMIN@organization:9b1c", "VIEWER@organization:7a21"), token.roles)
        }

        @Test
        fun `a refresh token carries a list claim as a JSON array`() = runBlocking {
            val token = factory().createRefreshToken(
                TEST_USER_ID,
                mapOf("roles" to listOf("ADMIN@organization:9b1c")),
                expires = true,
            )

            assertEquals(true, payload(token).contains("""["ADMIN@organization:9b1c"]"""))
        }

        @Test
        fun `a refresh token without expiry carries no exp claim`() = runBlocking {
            val token = factory().createRefreshToken(TEST_USER_ID, emptyMap(), expires = false)

            assertEquals(false, payload(token).contains("\"exp\""))
        }
    }
}
