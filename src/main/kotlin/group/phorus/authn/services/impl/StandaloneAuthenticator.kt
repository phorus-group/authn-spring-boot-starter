package group.phorus.authn.services.impl

import group.phorus.authn.config.SecurityConfiguration
import group.phorus.authn.config.toAuthNConfig
import group.phorus.authn.core.dtos.AuthData
import group.phorus.authn.core.services.Authenticator
import group.phorus.authn.core.services.Validator
import group.phorus.authn.core.services.impl.StandaloneTokenValidator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwe
import io.jsonwebtoken.Jws
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

/**
 * Spring-managed [Authenticator] that delegates all JWT validation logic to the
 * [StandaloneTokenValidator] from `authn-core`.
 *
 * Converts the Spring [SecurityConfiguration] to an
 * [AuthNConfig][group.phorus.authn.core.config.AuthNConfig] and delegates all calls.
 *
 * @see StandaloneTokenValidator
 * @see group.phorus.authn.core.services.Authenticator
 */
@AutoConfiguration
@Service
@Primary
class StandaloneAuthenticator(
    securityConfiguration: SecurityConfiguration,
    validators: List<Validator>,
) : Authenticator {

    private val delegate = StandaloneTokenValidator(securityConfiguration.toAuthNConfig(), validators)

    override fun authenticate(jwt: String, enableValidators: Boolean): AuthData =
        delegate.authenticate(jwt, enableValidators)

    override fun parseEncryptedClaims(jwt: String): Jwe<Claims> =
        delegate.parseEncryptedClaims(jwt)

    override fun parseSignedClaims(jwt: String): Jws<Claims> =
        delegate.parseSignedClaims(jwt)
}
