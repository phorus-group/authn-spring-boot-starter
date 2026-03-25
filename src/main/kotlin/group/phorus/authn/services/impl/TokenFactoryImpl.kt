package group.phorus.authn.services.impl

import group.phorus.authn.config.SecurityConfiguration
import group.phorus.authn.config.toAuthNConfig
import group.phorus.authn.core.dtos.AccessToken
import group.phorus.authn.core.services.TokenFactory
import group.phorus.authn.core.services.impl.TokenCreator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.stereotype.Service
import java.util.*

/**
 * Spring-managed [TokenFactory] that delegates all JWT creation logic to the
 * [TokenCreator] from `authn-core`.
 *
 * Converts the Spring [SecurityConfiguration] to an
 * [AuthNConfig][group.phorus.authn.core.config.AuthNConfig] and delegates all calls.
 *
 * @see TokenCreator
 * @see group.phorus.authn.core.services.TokenFactory
 */
@AutoConfiguration
@Service
class TokenFactoryImpl(
    securityConfiguration: SecurityConfiguration,
) : TokenFactory {

    private val delegate = TokenCreator(securityConfiguration.toAuthNConfig())

    override suspend fun createAccessToken(
        userId: UUID,
        privileges: List<String>,
        properties: Map<String, String>,
    ): AccessToken = delegate.createAccessToken(userId, privileges, properties)

    override suspend fun createRefreshToken(
        userId: UUID,
        expires: Boolean,
        properties: Map<String, String>,
    ): String = delegate.createRefreshToken(userId, expires, properties)
}
