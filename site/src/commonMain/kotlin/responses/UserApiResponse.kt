package responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.User

// wraps User in a response class
@Serializable
sealed class UserApiResponse {
    @Serializable
    @SerialName("success")
    data class Success(val data: List<User>): UserApiResponse()
    @Serializable
    @SerialName("error")
    data class Error(val errorMessage: String): UserApiResponse()
}
