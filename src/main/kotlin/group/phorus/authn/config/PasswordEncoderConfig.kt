package group.phorus.authn.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.scrypt.SCryptPasswordEncoder

/**
 * Autoconfiguration that creates an [SCryptPasswordEncoder] bean using the tuning parameters
 * from [SecurityConfiguration.passwordEncoder].
 *
 * @see PasswordEncoderConfiguration
 */
@AutoConfiguration
class PasswordEncoderConfig(
    private val securityConfiguration: SecurityConfiguration,
) {

    @Bean
    fun passwordEncoder(): SCryptPasswordEncoder =
        with(securityConfiguration.passwordEncoder) {
            SCryptPasswordEncoder(cpuCost, memoryCost, parallelization, keyLength, saltLength)
        }
}
