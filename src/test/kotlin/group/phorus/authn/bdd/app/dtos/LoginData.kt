package group.phorus.authn.bdd.app.dtos

data class LoginData (
    var email: String? = null,
    var password: String? = null,
    var device: String? = null,
    var expires: Boolean = true,
)
