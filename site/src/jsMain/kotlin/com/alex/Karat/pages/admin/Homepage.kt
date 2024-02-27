package com.alex.Karat.pages.admin

import androidx.compose.runtime.*
import com.alex.Karat.pages.super_admin.createUser
import com.alex.Karat.styles.buttonStyles
import com.alex.Karat.styles.textInputStyles
import com.alex.Karat.util.*
import com.stevdza.san.kotlinbs.components.*
import com.stevdza.san.kotlinbs.forms.BSInput
import com.stevdza.san.kotlinbs.forms.BSRadioButton
import com.stevdza.san.kotlinbs.forms.BSRadioButtonGroup
import com.stevdza.san.kotlinbs.models.BackgroundStyle
import com.stevdza.san.kotlinbs.models.InputValidation
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.DepartmentLeave
import models.User
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import responses.DepartmentLeaveApiResponse
import responses.UserApiResponse

@Page
@Composable
fun adminHomepage() {
    val userID = getCookie("user_id", path = "admin")
    val organizationID = getCookie("organization_id", path = "admin")
    val departmentID = getCookie("department_id", path = "admin")

    // set tab name
    document.title = "Admin Homepage"

    // check if cookie is set first
    if (userID == null || organizationID == null || departmentID == null) {
        window.alert("You must be logged in to access this page")
        window.location.replace("/login")

        // exit function if requirements are not met
        return
    }

    // ensures only admins can access this page
    else if (getCookie("is_admin", path = "admin").toBoolean().not()) {
        window.alert("Page only accessible by admins")
        window.history.back()

        // exit function if requirements are not met
        return
    }

    // //////////////////   Variables  ////////////////////
    var createEmployeeButtonClicked by remember { mutableStateOf(false) }

    // input field variables
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // true if the admin email exists, false otherwise
    var doesEmailExist: Boolean

    // list of retrieved employees
    var employeesList by remember { mutableStateOf<List<User>>(emptyList()) }

    // list of retrieved leave applications
    var departmentLeavesList by remember { mutableStateOf<List<DepartmentLeave>>(emptyList()) }

    val approvedLeaves by remember {
        mutableStateOf(
            departmentLeavesList.filter { leave ->
                leave.status == "APPROVED"
            },
        )
    }
    var pendingLeaves by remember {
        mutableStateOf(
            departmentLeavesList.filter { leave ->
                leave.status == "PENDING"
            },
        )
    }
    var rejectedLeaves by remember {
        mutableStateOf(
            departmentLeavesList.filter { leave ->
                leave.status == "REJECTED"
            },
        )
    }

    // for the loading spinner
    var loading by remember { mutableStateOf(false) }

    // limits for how many records are retrieved at a time
    val employeesLimit by remember { mutableStateOf(1) }
    var employeesOffset by remember { mutableStateOf(0) }

    val departmentLeavesLimit by remember { mutableStateOf(1) }
    var departmentLeavesOffset by remember { mutableStateOf(0) }

    // for when a new thread of execution (more precisely, a coroutine) needs to be launched
    val coroutineScope = rememberCoroutineScope()

    // ran on page load
    LaunchedEffect(true) {
        employeesList = emptyList()

        when (
            val response =
                retrieveEmployees(departmentID = departmentID, limit = employeesLimit, offset = employeesOffset)
        ) {
            is UserApiResponse.Error -> {
                /*window.alert(response.errorMessage)
                window.location.reload()*/
                println("no employees found")
            }

            is UserApiResponse.Success -> {
                employeesList = response.data
                employeesOffset += response.data.size
            }
        }
    }

    // ran on page load
    LaunchedEffect(true) {
        departmentLeavesList = emptyList()

        when (
            val response =
                retrieveDepartmentLeaves(
                    departmentID = departmentID,
                    limit = departmentLeavesLimit,
                    offset = departmentLeavesOffset,
                )
        ) {
            is DepartmentLeaveApiResponse.Error -> {
                /*window.alert(response.errorMessage)
                window.location.reload()*/
                println("no department leave applications found")
            }

            is DepartmentLeaveApiResponse.Success -> {
                departmentLeavesList = response.data

                departmentLeavesOffset += response.data.size
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().backgroundColor(Color.black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // //////////////////   Buttons  ////////////////////
            BSButton(
                text = "Add Employee",
                modifier = buttonStyles.toModifier().showModalOnClick(id = "addEmployeeModal"),
                onClick = {},
            )

            P {}

            BSButton(
                text = "View Employees",
                modifier = buttonStyles.toModifier().showModalOnClick(id = "viewEmployeesModal"),
                disabled = employeesList.isEmpty(),
                onClick = {
                    if (employeesList.isEmpty()) {
                        window.alert("Add an employee first to view")
                        window.stop()
                        window.location.reload()
                    }
                    /*coroutineScope.launch {
                        when (val response =
                            retrieveEmployees(departmentID = departmentID, limit = limit, offset = 0)) {
                            is UserApiResponse.Error -> {
                                window.alert(response.errorMessage)
                                window.location.reload()
                            }

                            is UserApiResponse.Success -> {
                                employeesList = response.data

                                loading = false
                            }
                        }
                    }*/
                },
            )

            if (employeesList.isEmpty()) {
                Row(modifier = Modifier.color(Color.red)) {
                    Text("Add at least 1 employee to view employees or leave applications")
                }
            }

            P {}

            BSButton(
                text = "Department Leave Applications",
                modifier = buttonStyles.toModifier().showModalOnClick("viewDepartmentLeavesModal"),
                disabled = employeesList.isEmpty() or (departmentLeavesList.isEmpty()),
                onClick = {
                    /*coroutineScope.launch {
                        when (val response =
                            retrieveDepartmentLeaves(departmentID = departmentID, limit = limit, offset = 0)) {
                            is DepartmentLeaveApiResponse.Error -> {
                                window.alert(response.errorMessage)
                                window.location.reload()
                            }

                            is DepartmentLeaveApiResponse.Success -> {
                                departmentLeavesList = response.data

                                println("department leave request successful")

                                departmentLeavesList.forEach {
                                    println(it.startDate)
                                }

                                loading = false
                            }
                        }
                    }*/
                    if (departmentLeavesList.isEmpty()) {
                        window.alert("List is empty")
                        window.stop()
                        window.location.reload()
                    }
                },
            )

            if (departmentLeavesList.isEmpty())
                {
                    Row(modifier = Modifier.color(Color.red)) {
                        Text("No leave applications found")
                    }
                }

            P {}

            BSButton(
                text = "Logout",
                modifier = buttonStyles.toModifier(),
                onClick = {
                    logoutUser(path = "admin")
                },
            )

            // //////////////////   Modals  ////////////////////

            // the modals are only displayed after the corresponding button is clicked
            BSModal(
                id = "addEmployeeModal",
                title = "Create Employee",
                positiveButtonText = "Add",
                negativeButtonText = "Close",
                modifier = textInputStyles.toModifier().backgroundColor(Color.black),
                onPositiveButtonClick = {
                    createEmployeeButtonClicked = true

                    firstName = trimStatement(firstName)
                    lastName = trimStatement(lastName)
                    phone = trimStatement(phone)
                    email = trimStatement(email)
                    password = trimStatement(password)

                    // check validity of fields
                    if (isNameValid(firstName).not() || isNameValid(lastName).not() ||
                        isPhoneNumberValid(phone = phone).not() || isEmailValid(email = email).not() ||
                        isPasswordValid(password = password).not()
                    ) {
                        window.alert("Fill all fields till marked valid")

                        createEmployeeButtonClicked = false

                        return@BSModal
                    }

                    // check if the passed email exists in a new thread
                    coroutineScope.launch {
                        // api call to check if email exists
                        val doesEmailExistResponse =
                            window.api.get("getifemailexists?email=$email").decodeToString()

                        val doesEmailExistResponseMap =
                            Json.decodeFromString<Map<String, Boolean>>(doesEmailExistResponse)

                        // checks the response. If for some reason the response was lost, assume it exists and alert user
                        doesEmailExist = doesEmailExistResponseMap["exists"] ?: true

                        println("Does email exist? $doesEmailExist")

                        if (doesEmailExist) {
                            window.alert("The email you entered already exists\nUse a different email address")

                            email = ""
                            password = ""
                            createEmployeeButtonClicked = false

                            return@launch
                        }

                        // proceed if the email does not exist
                        when (
                            val response =
                                createUser(
                                    firstName = firstName,
                                    lastName = lastName,
                                    phone = phone,
                                    email = email,
                                    password = password,
                                    isAdmin = false,
                                    departmentID = departmentID, // departments.first { it.departmentName == employeeDepartment }.departmentID,
                                    organizationID = organizationID,
                                )
                        ) {
                            is UserApiResponse.Error -> {
                                window.alert(response.errorMessage)
                            }

                            is UserApiResponse.Success -> {
                                showToast("addEmployeeSuccessfulToast")

                                delay(1500)
                                window.location.reload()

                                // update the employeesList by adding the new employee
                                /*employeesList = ArrayList<User>().apply {
                                    this.addAll(employeesList)
                                    this.add(response.data[0])
                                }*/

                                firstName = ""
                                lastName = ""
                                phone = ""
                                email = ""
                                password = ""
                            }
                        }

                        createEmployeeButtonClicked = false
                    }
                },
                onNegativeButtonClick = {
                    firstName = ""
                    lastName = ""
                    phone = ""
                    email = ""
                    password = ""
                },
                body = {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        BSInput(
                            type = InputType.Text,
                            value = firstName,
                            label = "First Name",
                            floating = true,
                            disabled = createEmployeeButtonClicked,
                            required = true,
                            validation =
                                InputValidation(
                                    isValid = isNameValid(firstName),
                                    isInvalid = isNameValid(firstName).not(),
                                    validFeedback = "Valid",
                                    invalidFeedback = if (firstName.isBlank()) "" else "Invalid",
                                ),
                            onValueChange = {
                                firstName = it
                            },
                            modifier = textInputStyles.toModifier(),
                        )

                        P {}

                        BSInput(
                            modifier = textInputStyles.toModifier().padding(topBottom = 5.px),
                            type = InputType.Text,
                            value = lastName,
                            label = "Last Name",
                            floating = true,
                            disabled = createEmployeeButtonClicked,
                            required = true,
                            validation =
                                InputValidation(
                                    isValid = isNameValid(lastName),
                                    isInvalid = isNameValid(lastName).not(),
                                    validFeedback = "Valid",
                                    invalidFeedback = if (lastName.isBlank()) "" else "Invalid",
                                ),
                            onValueChange = {
                                lastName = it
                            },
                        )

                        P {}

                        // International Format, eg +254722333444
                        BSInput(
                            modifier = textInputStyles.toModifier(),
                            type = InputType.Tel,
                            value = phone,
                            label = "Phone Number (Intl. format)",
                            floating = true,
                            disabled = createEmployeeButtonClicked,
                            required = true,
                            validation =
                                InputValidation(
                                    isValid = isPhoneNumberValid(phone),
                                    isInvalid = isPhoneNumberValid(phone).not(),
                                    validFeedback = "Valid",
                                    invalidFeedback = if (phone.isBlank()) "" else "Invalid Phone Number",
                                ),
                            onValueChange = {
                                phone = it.replace("\\s".toRegex(), "")
                            },
                        )

                        P {}

                        BSInput(
                            type = InputType.Email,
                            modifier = textInputStyles.toModifier(),
                            value = email,
                            label = "Email Address",
                            placeholder = "Type Here",
                            floating = true,
                            disabled = createEmployeeButtonClicked,
                            required = true,
                            validation =
                                InputValidation(
                                    isValid = isEmailValid(email),
                                    isInvalid = isEmailValid(email).not(),
                                    validFeedback = "Valid",
                                    invalidFeedback = if (email.isBlank()) "" else "Invalid Email",
                                ),
                            onValueChange = {
                                email = it.replace("\\s".toRegex(), "")
                            },
                        )

                        P {}

                        BSInput(
                            type = InputType.Password,
                            modifier = textInputStyles.toModifier(),
                            value = password,
                            label = "Password",
                            placeholder = "Enter your Password",
                            floating = true,
                            disabled = createEmployeeButtonClicked,
                            validation =
                                InputValidation(
                                    isValid = isPasswordValid(password),
                                    isInvalid = isPasswordValid(password).not(),
                                    validFeedback = "",
                                    invalidFeedback = invalidPasswordFeedback(password),
                                ),
                            onValueChange = {
                                password = it.replace("\\s".toRegex(), "")
                            },
                            required = true,
                        )
                    }
                },
            )

            BSModal(
                id = "viewEmployeesModal",
                title = "View Employees",
                positiveButtonText = "Done",
                negativeButtonText = "Close",
                modifier = textInputStyles.toModifier().backgroundColor(Color.black),
                onPositiveButtonClick = {
                    /*employeesList = emptyList()
                    offset = 1*/
                },
                onNegativeButtonClick = {
                    /*employeesList = emptyList()
                    offset = 1*/
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
                                text = "Load More Employees",
                                modifier = buttonStyles.toModifier(),
                                onClick = {
                                    loading = true

                                    coroutineScope.launch {
                                        when (
                                            val response =
                                                retrieveEmployees(
                                                    departmentID = departmentID,
                                                    limit = employeesLimit,
                                                    offset = employeesOffset,
                                                )
                                        ) {
                                            is UserApiResponse.Error -> {
                                                window.alert(response.errorMessage)
                                                // window.location.reload()
                                            }

                                            is UserApiResponse.Success -> {
                                                println("before: ")
                                                println(employeesList)

                                                employeesList += response.data

                                                println("after; ")
                                                println(employeesList)

                                                // increase the offset by the number of employees retrieved
                                                employeesOffset += response.data.size
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
                            // display all the employees sorted by their first names
                            employeesList.sortedBy { it.firstName }.forEach { employee ->
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
                                            Text("Employee ID:")
                                        }
                                        Column {
                                            Text(employee.userID)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column {
                                            Text("First Name:")
                                        }
                                        Column {
                                            Row {
                                                Text(employee.firstName)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Last Name:")
                                        }
                                        Row {
                                            Text(employee.lastName)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Email:")
                                        }
                                        Row {
                                            Text(employee.email)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Tel Number:")
                                        }
                                        Row {
                                            Text(employee.phone)
                                        }
                                    }
                                }

                                P {}
                            }
                        }
                    }
                },
            )

            BSModal(
                id = "viewDepartmentLeavesModal",
                title = "Leave Applications",
                positiveButtonText = "Done",
                negativeButtonText = "Close",
                modifier = textInputStyles.toModifier().backgroundColor(Color.black),
                onPositiveButtonClick = {
                    /*departmentLeavesList.forEach { println(it.status) }

                    if (approvedLeaves.isEmpty()) {
                        println("still empty")
                    }
                    departmentLeavesList = emptyList()
                    offset = 1*/
                },
                onNegativeButtonClick = {
                    /*departmentLeavesList = emptyList()
                    offset = 1*/
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
                                                retrieveDepartmentLeaves(
                                                    departmentID = departmentID,
                                                    limit = departmentLeavesLimit,
                                                    offset = departmentLeavesOffset,
                                                )
                                        ) {
                                            is DepartmentLeaveApiResponse.Error -> {
                                                window.alert(response.errorMessage)
                                                // window.location.reload()
                                            }

                                            is DepartmentLeaveApiResponse.Success -> {
                                                println("before: ")
                                                println(departmentLeavesList)

                                                departmentLeavesList += response.data

                                                println("after; ")
                                                println(departmentLeavesList)

                                                // increase the offset by the number of retrieved leave applications
                                                departmentLeavesOffset += response.data.size
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
                            // TODO add filter here for approved or rejected or pending leaves
                            // display the leave applications
                            departmentLeavesList.sortedBy { it.startDate }.forEach { departmentLeaveApplication ->
                                var manageButtonClicked by remember { mutableStateOf(false) }
                                var approveRejectButtonClicked by remember { mutableStateOf(false) }
                                var submitApprovalButtonClicked by remember { mutableStateOf(false) }
                                var approvalStatus by remember { mutableStateOf("") }
                                var responseReason: String by remember { mutableStateOf("") }

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
                                            Text("Employee ID:")
                                        }
                                        Column {
                                            Text(departmentLeaveApplication.employeeID)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column {
                                            Text("First Name:")
                                        }
                                        Column {
                                            Row {
                                                Text(departmentLeaveApplication.firstName)
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Last Name:")
                                        }
                                        Row {
                                            Text(departmentLeaveApplication.lastName)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Email:")
                                        }
                                        Row {
                                            Text(departmentLeaveApplication.email)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Start Date:")
                                        }
                                        Row {
                                            Text(
                                                departmentLeaveApplication.startDate.dayOfWeek.toString()
                                                    .plus(", ")
                                                    .plus(departmentLeaveApplication.startDate.dayOfMonth)
                                                    .plus(" ")
                                                    .plus(departmentLeaveApplication.startDate.month)
                                                    .plus(" ")
                                                    .plus(departmentLeaveApplication.startDate.year),
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("End Date:")
                                        }
                                        Row {
                                            Text(
                                                departmentLeaveApplication.endDate.dayOfWeek.toString()
                                                    .plus(", ")
                                                    .plus(departmentLeaveApplication.endDate.dayOfMonth)
                                                    .plus(" ")
                                                    .plus(departmentLeaveApplication.endDate.month)
                                                    .plus(" ")
                                                    .plus(departmentLeaveApplication.endDate.year),
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
                                                departmentLeaveApplication.applicationDate.dayOfWeek.toString()
                                                    .plus(", ")
                                                    .plus(departmentLeaveApplication.applicationDate.dayOfMonth)
                                                    .plus(" ")
                                                    .plus(departmentLeaveApplication.applicationDate.month)
                                                    .plus(" ")
                                                    .plus(departmentLeaveApplication.applicationDate.year),
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Duration:")
                                        }
                                        Row {
                                            Text(departmentLeaveApplication.duration.toString())
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row {
                                            Text("Leave type:")
                                        }
                                        Row {
                                            Text(departmentLeaveApplication.leaveType)
                                        }
                                    }
                                    if (departmentLeaveApplication.requestReason != null) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Row {
                                                Text("Request Reason:")
                                            }
                                            Row {
                                                Text(departmentLeaveApplication.requestReason)
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
                                            Text(departmentLeaveApplication.status)
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        BSButton(
                                            text = "Manage",
                                            modifier = buttonStyles.toModifier(),
                                            disabled = submitApprovalButtonClicked,
                                            onClick = {
                                                manageButtonClicked = true
                                            },
                                        )
                                    }

                                    if (manageButtonClicked) {
                                        P {}

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center,
                                        ) {
                                            BSRadioButtonGroup(
                                                toggleButton = true,
                                                inline = true,
                                            ) {
                                                BSRadioButton(
                                                    label = "Approve",
                                                    disabled = submitApprovalButtonClicked,
                                                    onClick = {
                                                        approvalStatus = "APPROVED"
                                                        approveRejectButtonClicked = true
                                                    },
                                                )
                                                BSRadioButton(
                                                    label = "Reject",
                                                    disabled = submitApprovalButtonClicked,
                                                    onClick = {
                                                        approvalStatus = "REJECTED"
                                                        approveRejectButtonClicked = true
                                                    },
                                                )
                                            }
                                        }
                                    }
                                    if (approveRejectButtonClicked) {
                                        P {}

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            BSInput(
                                                modifier = textInputStyles.toModifier(),
                                                type = InputType.Text,
                                                value = responseReason,
                                                label = "Reason",
                                                floating = true,
                                                onValueChange = {
                                                    responseReason = it
                                                },
                                                required = true,
                                            )

                                            BSButton(
                                                text = "Submit",
                                                modifier = buttonStyles.toModifier(),
                                                loading = submitApprovalButtonClicked,
                                                loadingText = "Submitting approval...",
                                                disabled = submitApprovalButtonClicked,
                                                onClick = {
                                                    submitApprovalButtonClicked = true

                                                    coroutineScope.launch {
                                                        when (
                                                            val response =
                                                                approveLeaveApplication(
                                                                    adminID = userID,
                                                                    newResponse = trimStatement(responseReason),
                                                                    status = approvalStatus,
                                                                    departmentLeave = departmentLeaveApplication,
                                                                )
                                                        ) {
                                                            is DepartmentLeaveApiResponse.Error -> {
                                                                window.alert(response.errorMessage)
                                                            }

                                                            is DepartmentLeaveApiResponse.Success -> {
                                                                // window.alert("Success!\nReload when done to see changes")

                                                                showToast("leaveApplicationModificationToast")

                                                                departmentLeaveApplication.status = approvalStatus
                                                                departmentLeaveApplication.approvedBy = userID
                                                                departmentLeaveApplication.responseReason = responseReason
                                                            }
                                                        }
                                                    }

                                                    submitApprovalButtonClicked = false
                                                    approveRejectButtonClicked = false
                                                    manageButtonClicked = false
                                                },
                                            )
                                        }
                                    }
                                }

                                P {}
                            }
                        }
                    }
                },
            )
        }
    }

    // //////////////////   Toasts  ////////////////////
    BSToastGroup(placement = ToastPlacement.BottomRight) {
        BSToast(
            id = "createDepartmentSuccessfulToast",
            title = "Success",
            body = "Department has been created successfully",
            autoHide = true,
            indicatorStyle = BackgroundStyle.Success,
            onCloseClick = {},
        )

        BSToast(
            id = "addEmployeeSuccessfulToast",
            title = "Success",
            body = "Employee has been created successfully",
            autoHide = true,
            indicatorStyle = BackgroundStyle.Success,
            onCloseClick = {},
        )

        BSToast(
            id = "leaveApplicationModificationToast",
            title = "Success",
            body = "Application has been successfully modified",
            autoHide = true,
            indicatorStyle = BackgroundStyle.Success,
            onCloseClick = {},
        )
    }
}

suspend fun retrieveEmployees(
    departmentID: String,
    limit: Int,
    offset: Int,
): UserApiResponse {
    println("in retrieveEmployees function")

    val response =
        window.api.get("getusers?department_id=$departmentID&is_admin=false&is_super_admin=false&limit=$limit&offset=$offset")

    println("Employee Retrieval response: ${response.decodeToString()}")

    return Json.decodeFromString<UserApiResponse>(response.decodeToString())
}

suspend fun retrieveDepartmentLeaves(
    departmentID: String,
    limit: Int,
    offset: Int,
): DepartmentLeaveApiResponse {
    println("in retrieveEmployeeLeaves function")

    val response =
        window.api.get("getdepartmentleaves?department_id=$departmentID&limit=$limit&offset=$offset")

    println("Employee Leaves Retrieval response: ${response.decodeToString()}")

    return Json.decodeFromString<DepartmentLeaveApiResponse>(response.decodeToString())
}

suspend fun approveLeaveApplication(
    adminID: String,
    newResponse: String?,
    status: String,
    departmentLeave: DepartmentLeave,
): DepartmentLeaveApiResponse {
    println("in approveLeaveApplication function")

    val response =
        window.api.post(
            apiPath = "approveleaveapplication?&admin_id=$adminID&response=$newResponse&status=$status",
            headers = mapOf("Content-Type" to "application/json"),
            body = Json.encodeToString<DepartmentLeave>(departmentLeave).encodeToByteArray(),
        )

    println("Leave Approval response: ${response.decodeToString()}")

    return Json.decodeFromString<DepartmentLeaveApiResponse>(response.decodeToString())
}
