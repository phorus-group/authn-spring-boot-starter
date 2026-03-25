package group.phorus.authn.config

import group.phorus.authn.core.services.impl.JwksKeyLocator
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean

/**
 * Autoconfiguration that creates a [JwksKeyLocator] bean when an IdP JWKS endpoint is configured.
 *
 * The [JwksKeyLocator] uses [java.net.http.HttpClient] internally (no Spring WebClient needed).
 */
@AutoConfiguration
class IdpAutoConfiguration {

    @Bean
    @ConditionalOnProperty(
        prefix = "group.phorus.security",
        name = ["idp.jwk-set-uri"],
    )
    fun jwksKeyLocator(securityConfiguration: SecurityConfiguration): JwksKeyLocator =
        JwksKeyLocator(securityConfiguration.toAuthNConfig().idp)
}
