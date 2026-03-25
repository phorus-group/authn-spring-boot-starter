package group.phorus.authn.config

import group.phorus.authn.core.config.AuthNConfig
import group.phorus.authn.core.config.ClaimsMapping
import group.phorus.authn.core.config.EncryptionConfig
import group.phorus.authn.core.config.ExpirationConfig
import group.phorus.authn.core.config.IdpConfig
import group.phorus.authn.core.config.IdpEncryptionConfig
import group.phorus.authn.core.config.JwtConfig
import group.phorus.authn.core.config.SigningConfig

/**
 * Converts the Spring [SecurityConfiguration] to a Kotlin [AuthNConfig] suitable
 * for use with core implementations ([group.phorus.authn.core.services.impl.TokenCreator],
 * [group.phorus.authn.core.services.impl.StandaloneTokenValidator]).
 */
fun SecurityConfiguration.toAuthNConfig(): AuthNConfig = AuthNConfig(
    mode = mode,
    jwt = JwtConfig(
        issuer = jwt.issuer,
        tokenFormat = jwt.tokenFormat,
        signing = SigningConfig(
            algorithm = jwt.signing.algorithm,
            signatureAlgorithm = jwt.signing.signatureAlgorithm,
            encodedPrivateKey = jwt.signing.encodedPrivateKey,
            encodedPublicKey = jwt.signing.encodedPublicKey,
        ),
        encryption = EncryptionConfig(
            algorithm = jwt.encryption.algorithm,
            keyAlgorithm = jwt.encryption.keyAlgorithm,
            aeadAlgorithm = jwt.encryption.aeadAlgorithm,
            encodedPublicKey = jwt.encryption.encodedPublicKey,
            encodedPrivateKey = jwt.encryption.encodedPrivateKey,
        ),
        expiration = ExpirationConfig(
            tokenMinutes = jwt.expiration.tokenMinutes,
            refreshTokenMinutes = jwt.expiration.refreshTokenMinutes,
        ),
    ),
    idp = IdpConfig(
        issuerUri = idp.issuerUri,
        jwkSetUri = idp.jwkSetUri,
        jwksCacheTtlMinutes = idp.jwksCacheTtlMinutes,
        claims = ClaimsMapping(
            subject = idp.claims.subject,
            privileges = idp.claims.privileges,
        ),
        encryption = IdpEncryptionConfig(
            algorithm = idp.encryption.algorithm,
            encodedPrivateKey = idp.encryption.encodedPrivateKey,
        ),
    ),
)
