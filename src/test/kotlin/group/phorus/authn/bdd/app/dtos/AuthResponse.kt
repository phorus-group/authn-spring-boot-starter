package group.phorus.authn.bdd.app.dtos

import group.phorus.authn.core.dtos.AccessToken

data class AuthResponse(
    var accessToken: AccessToken,
    var refreshToken: String,
)
