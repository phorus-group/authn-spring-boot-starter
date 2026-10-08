package group.phorus.authn.bdd.app.services.impl

import group.phorus.authn.core.services.Validator
import io.jsonwebtoken.Claims
import org.springframework.stereotype.Service

@Service
class TestValidatorImpl : Validator {
    override fun accepts(property: String): Boolean = property == "tokenThingy"

    override fun isValid(value: Any?, properties: Map<String, Any?>): Boolean =
        properties[Claims.SUBJECT] != null && value.toString().lowercase() == "true"
}
