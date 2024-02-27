package responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.DepartmentLeave


@Serializable
sealed class DepartmentLeaveApiResponse {
    @Serializable
    @SerialName("success")
    data class Success(val data: List<DepartmentLeave>): DepartmentLeaveApiResponse()
    @Serializable
    @SerialName("error")
    data class Error(val errorMessage: String): DepartmentLeaveApiResponse()
}