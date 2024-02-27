package com.alex.Karat.pages.employee

import androidx.compose.runtime.*
import com.alex.Karat.styles.buttonStyles
import com.alex.Karat.styles.textInputStyles
import com.alex.Karat.util.*
import com.stevdza.san.kotlinbs.components.*
import com.stevdza.san.kotlinbs.forms.BSTextArea
import com.stevdza.san.kotlinbs.models.DropdownDirection
import com.stevdza.san.kotlinbs.models.SpinnerVariant
import com.stevdza.san.kotlinbs.models.ToastPlacement
import com.varabyte.kobweb.browser.api
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.style.toModifier
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Leave
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.min
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import responses.LeaveApiResponse
import kotlin.js.Date
import kotlin.math.ceil

@Page
@Composable
fun employeeHomepage() {
    // set tab name
    document.title = "Employee Homepage"

    // obtain user_id from the document's cookies
    val userID = getCookie("user_id", path = "employee")

    // check if cookies are set first
    // if no user_id cookie found, it means the user is not logged in
    if (userID == null) {
        window.alert("You must be logged in to access this page")
        window.location.replace("/login")

        return
    }

    // ensures only employees can access this page by checking the is_super_admin = false and is_admin = false
    if (getCookie("is_admin", path = "employee").toBoolean() or (
            getCookie(
                "is_super_admin",
                path = "employee",
            ).toBoolean()
        )
    ) {
        window.alert("Page only accessible by employees")
        window.history.back()

        return
    }

    // input field variables
    var leaveDate: Date? by remember { mutableStateOf(null) }
    var returnDate: Date? by remember { mutableStateOf(null) }
    var reason: String? by remember { mutableStateOf(null) }
    var leaveType: String? by remember { mutableStateOf(null) }

    var duration by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    // list containing the retrieved leaves
    var leaveList by remember { mutableStateOf<List<Leave>>(emptyList()) }

    var appliedForLeave by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }

    // number of leaves to get at a time
    val limit by remember { mutableStateOf(1) }
    var offset by remember { mutableStateOf(0) }

    LaunchedEffect(true) {
        leaveList = emptyList()

        when (val response = retrieveLeaves(userID = userID, limit = limit, offset = offset)) {
            // if there was an error or no leaves found, display a warning
            is LeaveApiResponse.Error -> {
                println("no leave applications found")
            }

            // if there were leaves found, initialise the leave list with the list of retrieved leaves
            is LeaveApiResponse.Success -> {
                leaveList = response.data

                offset += response.data.size
            }
        }
    }

    /*coroutineScope.launch {

        delay(3000)

        appliedForLeave = when (val response = checkIfPendingLeave(userID = userID)["exists"]) {
            true -> true
            false -> false
            null -> true
        }
    }*/

    Box(
        modifier = Modifier.fillMaxSize().backgroundColor(Color.black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // //////////////////   Buttons  ////////////////////

            // leave application button
            BSButton(
                text = "Apply for Leave",
                modifier = buttonStyles.toModifier().showModalOnClick(id = "leaveApplicationModal"),
                disabled = appliedForLeave,
                onClick = {},
            )

            P {}

            // retrieve leaves
            BSButton(
                text = "View Leaves",
                modifier = buttonStyles.toModifier().showModalOnClick(id = "viewLeavesModal"),
                disabled = leaveList.isEmpty(),
                onClick = {
                    // get leaves from a new thread
                    /*coroutineScope.launch {
                        // get leaves for the user
                        when (val response = retrieveLeaves(userID = userID, limit = limit, offset = 0)) {
                            // if there was an error or no leaves found, display a warning
                            is LeaveApiResponse.Error -> {
                                window.alert(response.errorMessage)
                                window.location.reload()
                            }

                            // if there were leaves found, initialise the leave list with the list of retrieved leaves
                            is LeaveApiResponse.Success -> {
                                leaveList = response.data

                                loading = false
                            }
                        }
                    }*/

                    /*if (leaveList.isEmpty()) {
                        window.alert("Apply for a leave first to view")
                        window.stop()
                        window.location.reload()
                    }*/
                },
            )

            if (leaveList.isEmpty()) {
                Row(modifier = Modifier.color(Color.red)) {
                    Text("Make at least one leave request")
                }
            }

            P {}

            // log the employee out
            BSButton(
                text = "Logout",
                modifier = buttonStyles.toModifier(),
                onClick = {
                    logoutUser(path = "employee")
                },
            )

            // //////////////////   Modals  ////////////////////

            // the modals are only displayed after the corresponding button is clicked
            BSModal(
                id = "leaveApplicationModal",
                title = "Leave Application",
                positiveButtonText = "Submit",
                negativeButtonText = "Close",
                modifier = textInputStyles.toModifier().backgroundColor(Color.black),
                onPositiveButtonClick = {
                    // if all the fields are valid
                    if (duration >= 1 && returnDate != null && leaveDate != null && leaveType != null && leaveType != null) {
                        // start a new thread
                        coroutineScope.launch {
                            // POST the leave
                            when (
                                val response =
                                    leaveApplication(
                                        userID = userID,
                                        applicationDate = JSDateTOLocalDate(Date()),
                                        duration = duration,
                                        startDate = JSDateTOLocalDate(leaveDate!!),
                                        endDate = JSDateTOLocalDate(returnDate!!),
                                        leaveType = leaveType!!,
                                        requestReason =
                                            if (reason == null || reason!!.isBlank()) {
                                                null
                                            } else {
                                                trimStatement(reason!!)
                                            },
                                    )
                            ) {
                                // if the leave was successfully sent
                                is LeaveApiResponse.Success -> {
                                    reason = ""

                                    showToast("leaveSuccessfulToast")
                                    delay(1500)

                                    window.location.reload()
                                }

                                // if there was an error, display it
                                is LeaveApiResponse.Error -> {
                                    window.alert(response.errorMessage)
                                }
                            }
                        }

                        return@BSModal
                    }

                    if (duration <= 0) {
                        window.alert("Duration cannot be less than 1 day")
                    }

                    if (leaveType == null) {
                        window.alert("Select a leave type")
                    }

                    if (returnDate == null || leaveDate == null) {
                        window.alert("Fill in the start and return dates")
                    }
                },
                onNegativeButtonClick = {
                    reason = ""
                },
                body = {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Leave Date: ")

                            Input(
                                type = InputType.Date,
                                attrs = /*textInputStyles.toModifier().toAttrs() */ {
                                    placeholder("Enter leave start date")

                                    onInput {
                                        leaveDate = Date(it.value)
                                    }

                                    // Set the min attribute. The minimum date is tomorrow
                                    min(js("new Date(Date.now() + 86400000).toISOString().split('T')[0]") as String)
                                },
                            )
                        }

                        P {}

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Return Date:")

                            Input(
                                type = InputType.Date,
                                attrs = /*textInputStyles.toModifier().toAttrs() */ {
                                    placeholder("Enter return date")

                                    onInput {
                                        returnDate = Date(it.value)
                                    }

                                    // Set the min attribute. The minimum date is a day after tomorrow
                                    min(js("new Date(Date.now() + 172800000).toISOString().split('T')[0]") as String)
                                },
                            )
                        }

                        // set duration when both fields are filled
                        if (leaveDate != null && returnDate != null) {
                            duration =
                                ceil((returnDate!!.getTime() - leaveDate!!.getTime()) / (1000 * 60 * 60 * 24))
                                    .toInt()

                            P {
                                Text("Duration: $duration days")
                            }
                        }

                        P {}

                        BSDropdown(
                            placeholder = "Select Leave Type",
                            items = LeaveTypes.entries.map { it.type }.sorted(),
                            direction = DropdownDirection.Right,
                            onItemSelect = { _, value -> leaveType = value },
                        )

                        P {}

                        // Reason
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Reason:\n(optional)")

                            BSTextArea(
                                // modifier = textInputStyles.toModifier(),
                                value = if (reason is String && reason != null) reason!! else "",
                                // label = "Reason",
                                placeholder = "Enter reason for leave..",
                                onValueChange = { reason = it },
                            )

                            println("Reason: ${reason?.replace("[^\\S\\r\\n]+".toRegex(), " ")?.trim()}")
                        }
                    }
                },
            )

            BSModal(
                id = "viewLeavesModal",
                title = "View Leaves",
                positiveButtonText = "Done",
                negativeButtonText = "Close",
                modifier = textInputStyles.toModifier().backgroundColor(Color.black),
                onPositiveButtonClick = {
                    /*leaveList = emptyList()
                    offset = 2*/
                },
                onNegativeButtonClick = {
                    /*leaveList = emptyList()
                     offset = 2*/
                },
                body = {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(0.5),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (loading) {
                                    BSSpinner(
                                        variant = SpinnerVariant.Default,
                                    )
                                }
                            }

                            BSButton(
                                text = "Load More Leaves",
                                modifier = buttonStyles.toModifier(),
                                onClick = {
                                    loading = true

                                    coroutineScope.launch {
                                        when (
                                            val response =
                                                retrieveLeaves(userID = userID, limit = limit, offset = offset)
                                        ) {
                                            is LeaveApiResponse.Error -> {
                                                window.alert(response.errorMessage)
                                                // window.location.reload()
                                            }

                                            is LeaveApiResponse.Success -> {
                                                println("before: ")
                                                println(leaveList)

                                                leaveList += response.data

                                                println("after; ")
                                                println(leaveList)

                                                // increase the offset by the number of leaves retrieved
                                                offset += response.data.size
                                            }
                                        }

                                        loading = false
                                    }
                                },
                            )
                        }

                        P {}

                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // leaveList.map{  }.forEach { leave ->
                            leaveList.sortedBy { it.startDate }.forEach { leave ->
                                Column(
                                    modifier =
                                        Modifier.fillMaxWidth().border(
                                            width = 0.5.px,
                                            style = LineStyle.Solid,
                                            color = Color.darkblue,
                                        )
                                            .borderRadius(5.px)
                                            .padding(5.px),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column {
                                            Text("Start Date:")
                                        }
                                        Column {
                                            Row {
                                                Text(
                                                    leave.startDate.dayOfWeek.toString()
                                                        .plus(", ")
                                                        .plus(leave.startDate.dayOfMonth)
                                                        .plus(" ")
                                                        .plus(leave.startDate.month)
                                                        .plus(" ")
                                                        .plus(leave.startDate.year),
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Return Date:")
                                        }
                                        Row {
                                            Text(
                                                leave.endDate.dayOfWeek.toString()
                                                    .plus(", ")
                                                    .plus(leave.endDate.dayOfMonth)
                                                    .plus(" ")
                                                    .plus(leave.endDate.month)
                                                    .plus(" ")
                                                    .plus(leave.endDate.year),
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Application Date:")
                                        }
                                        Row {
                                            Text(
                                                leave.applicationDate.dayOfWeek.toString()
                                                    .plus(", ")
                                                    .plus(leave.applicationDate.dayOfMonth)
                                                    .plus(" ")
                                                    .plus(leave.applicationDate.month)
                                                    .plus(" ")
                                                    .plus(leave.applicationDate.year),
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Duration (days):")
                                        }
                                        Row {
                                            Text(leave.duration.toString().plus(""))
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Type:")
                                        }
                                        Row {
                                            Text(leave.leaveType)
                                        }
                                    }
                                    if (leave.requestReason != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Row {
                                                Text("Reason:")
                                            }
                                            Row {
                                                Text(leave.requestReason.toString())
                                            }
                                        }
                                    }

                                    if (leave.responseReason != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Row {
                                                Text("Feedback:")
                                            }
                                            Row {
                                                Text(leave.responseReason.toString())
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Status:")
                                        }
                                        Row {
                                            Text(leave.status)
                                        }
                                    }
                                }

                                P {}
                            }
                        }
                    }
                },
            )

            P {}

            BSToastGroup(placement = ToastPlacement.BottomRight) {
                BSToast(
                    id = "leaveSuccessfulToast",
                    title = "Success",
                    body = "Your leave application has been sent",
                    autoHide = true,
                    onCloseClick = {},
                )
            }
        }
    }
}

// checks if the user has a pending leave
suspend fun checkIfPendingLeave(userID: String): Map<String, Boolean> {
    return Json.decodeFromString<Map<String, Boolean>>(
        window.api.get(apiPath = "checkForpendingleave?user_id=$userID").decodeToString(),
    )
}

suspend fun leaveApplication(
    userID: String,
    applicationDate: LocalDate,
    duration: Int,
    startDate: LocalDate,
    endDate: LocalDate,
    leaveType: String,
    requestReason: String?,
): LeaveApiResponse {
    println("in LeaveApplication function")

    val leave =
        Leave(
            // leaveID = "",
            userID = userID,
            startDate = startDate,
            endDate = endDate,
            duration = duration,
            applicationDate = applicationDate,
            leaveType = leaveType,
            requestReason = requestReason,
            responseReason = null,
            status = "PENDING",
            approvedBy = null,
        )

    val response =
        window.api.post(
            apiPath = "addleave",
            headers = mapOf("Content-Type" to "application/json"),
            body = Json.encodeToString<Leave>(leave).encodeToByteArray(),
        )

    println("Leave Application response: ${response.decodeToString()}")

    return Json.decodeFromString<LeaveApiResponse>(response.decodeToString())
}

// gets the leaves filed by a particular user
suspend fun retrieveLeaves(
    userID: String,
    limit: Int,
    offset: Int,
): LeaveApiResponse {
    println("in retrieveLeaves function")

    val response = window.api.get("getuserleaves?user_id=$userID&limit=$limit&offset=$offset")

    println("Leave Retrieval response: ${response.decodeToString()}")

    return Json.decodeFromString<LeaveApiResponse>(response.decodeToString())
}
