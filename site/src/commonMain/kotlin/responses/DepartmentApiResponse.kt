package responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import models.Department

@Serializable
sealed class DepartmentApiResponse {
    @Serializable
    @SerialName("success")
    data class Success(val data: List<Department>) : DepartmentApiResponse()

    @SerialName("error")
    @Serializable
    data class Error(val errorMessage: String) : DepartmentApiResponse()
}