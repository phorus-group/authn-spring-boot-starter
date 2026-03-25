package group.phorus.authn.bdd.app.services.impl

import group.phorus.authn.bdd.app.repositories.DeviceRepository
import group.phorus.authn.core.services.Validator
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service
import java.util.*
import kotlin.jvm.optionals.getOrNull

@Service
class DeviceValidatorImpl(
    private val deviceRepository: DeviceRepository,
) : Validator {
    override fun accepts(property: String): Boolean = property == Claims.ID

    override fun isValid(value: String, properties: Map<String, String>): Boolean =
        deviceRepository.findByUserIdAndJTI(UUID.fromString(properties[Claims.SUBJECT]), value).getOrNull().let {
            it != null && !it.disabled
        }
}
