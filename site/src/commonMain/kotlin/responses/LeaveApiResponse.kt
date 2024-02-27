package responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.Leave

// wraps UserLeave in a response class
@Serializable
sealed class LeaveApiResponse {
    @Serializable
    @SerialName("success")
    data class Success(val data: List<Leave>) : LeaveApiResponse()

    @SerialName("error")
    @Serializable
    data class Error(val errorMessage: String) : LeaveApiResponse()
}
