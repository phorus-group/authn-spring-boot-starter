package group.phorus.authn.services.impl

import group.phorus.authn.config.SecurityConfiguration
import group.phorus.authn.config.toAuthNConfig
import group.phorus.authn.core.dtos.AuthData
import group.phorus.authn.core.services.Authenticator
import group.phorus.authn.core.services.Validator
import group.phorus.authn.config.IdpAutoConfiguration
import group.phorus.authn.core.services.impl.IdpTokenValidator
import group.phorus.authn.core.services.impl.JwksKeyLocator
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwe
import io.jsonwebtoken.Jws
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service

/**
 * Spring-managed [Authenticator] that delegates all IdP token validation logic to the
 * [IdpTokenValidator] from `authn-core`.
 *
 * @see IdpTokenValidator
 * @see group.phorus.authn.core.services.Authenticator
 */
@AutoConfiguration(after = [IdpAutoConfiguration::class])
@Service
@Qualifier("idp")
@ConditionalOnBean(JwksKeyLocator::class)
class IdpAuthenticator(
    securityConfiguration: SecurityConfiguration,
    jwksKeyLocator: JwksKeyLocator,
    validators: List<Validator>,
) : Authenticator {

    private val delegate = IdpTokenValidator(
        securityConfiguration.toAuthNConfig(),
        jwksKeyLocator,
        validators,
    )

    override fun authenticate(jwt: String, enableValidators: Boolean): AuthData =
        delegate.authenticate(jwt, enableValidators)

    override fun parseEncryptedClaims(jwt: String): Jwe<Claims> =
        delegate.parseEncryptedClaims(jwt)

    override fun parseSignedClaims(jwt: String): Jws<Claims> =
        delegate.parseSignedClaims(jwt)
}
