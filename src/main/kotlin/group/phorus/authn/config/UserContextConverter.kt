package group.phorus.authn.config

import group.phorus.authn.core.config.AuthMode
import group.phorus.authn.core.dtos.AuthContextData
import group.phorus.authn.core.dtos.TokenType
import group.phorus.authn.core.services.Authenticator
import group.phorus.authn.extractBearerToken
import group.phorus.authn.services.impl.IdpAuthenticator
import group.phorus.exception.core.Unauthorized
import group.phorus.mapper.mapping.extensions.mapTo
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.core.convert.converter.Converter

/**
 * Spring [Converter] that transforms a raw `Authorization` header value into [AuthContextData].
 *
 * This enables controller parameter injection via `@RequestHeader`:
 *
 * ```kotlin
 * @GetMapping("/me")
 * suspend fun me(
 *     @RequestHeader(HttpHeaders.AUTHORIZATION) auth: AuthContextData,
 * ): UserProfile {
 *     return userService.getProfile(auth.userId)
 * }
 * ```
 *
 * The converter extracts the Bearer token from the header and authenticates it using the
 * same authenticator as [group.phorus.authn.filters.AuthFilter], based on the
 * configured [AuthMode]. Registered [Validator][group.phorus.authn.core.services.Validator] beans
 * run, and a refresh token is refused.
 *
 * @see group.phorus.authn.filters.AuthFilter
 * @see AuthContextData
 */
@AutoConfiguration
class UserContextConverter(
    private val securityConfiguration: SecurityConfiguration,
    private val authenticator: Authenticator,
    idpAuthenticatorProvider: ObjectProvider<IdpAuthenticator>,
) : Converter<String, AuthContextData> {
    private val idpAuthenticator = idpAuthenticatorProvider.getIfAvailable()

    override fun convert(authorization: String): AuthContextData {
        val jwt = extractBearerToken(authorization)

        val effectiveAuthenticator = when (securityConfiguration.mode) {
            AuthMode.STANDALONE, AuthMode.IDP_BRIDGE -> authenticator
            AuthMode.IDP_DELEGATED -> idpAuthenticator
                ?: throw IllegalStateException(
                    "IdpAuthenticator is not configured. Set group.phorus.security.idp.jwk-set-uri " +
                    "for IDP_DELEGATED mode."
                )
        }

        val authData = effectiveAuthenticator.authenticate(jwt)

        if (authData.tokenType == TokenType.REFRESH_TOKEN) throw Unauthorized("Invalid access token")

        return authData.mapTo<AuthContextData>()!!
    }
}
