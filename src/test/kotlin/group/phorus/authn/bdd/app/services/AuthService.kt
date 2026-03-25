package group.phorus.authn.bdd.app.services

import group.phorus.authn.bdd.app.dtos.AuthResponse
import group.phorus.authn.bdd.app.dtos.LoginData
import group.phorus.authn.core.dtos.AccessToken

interface AuthService {
    suspend fun login(loginData: LoginData): AuthResponse
    suspend fun refreshAccessToken(refreshToken: String): AccessToken
}
