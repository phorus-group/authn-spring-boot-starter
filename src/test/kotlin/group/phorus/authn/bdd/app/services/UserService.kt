package group.phorus.authn.bdd.app.services

import group.phorus.authn.bdd.app.dtos.UserDTO
import group.phorus.authn.bdd.app.model.User
import java.util.*

interface UserService {
    suspend fun create(userDTO: UserDTO): UUID
    suspend fun findById(id: UUID): User
    suspend fun findByEmail(email: String): User
}
