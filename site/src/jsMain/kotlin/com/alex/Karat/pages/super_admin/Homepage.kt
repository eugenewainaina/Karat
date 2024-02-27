package com.alex.Karat.pages.super_admin

import androidx.compose.runtime.*
import com.alex.Karat.styles.buttonStyles
import com.alex.Karat.styles.textInputStyles
import com.alex.Karat.util.*
import com.stevdza.san.kotlinbs.components.*
import com.stevdza.san.kotlinbs.forms.BSInput
import com.stevdza.san.kotlinbs.models.BackgroundStyle
import com.stevdza.san.kotlinbs.models.DropdownDirection
import com.stevdza.san.kotlinbs.models.InputValidation
import com.stevdza.san.kotlinbs.models.ToastPlacement
import com.varabyte.kobweb.browser.api
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.style.toModifier
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.Department
import models.User
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import responses.DepartmentApiResponse
import responses.UserApiResponse

@Page
@Composable
fun superAdminHomepage() {
    val userID = getCookie(key = "user_id", path = "super_admin")
    val organizationID = getCookie("organization_id", path = "super_admin")

    // check if cookie is set first
    if (userID == null || organizationID == null) {
        window.alert("You must be logged in to access this page")
        window.location.replace("/login")

        return
    }

    // ensures only super admins can access this page
    if (getCookie("is_super_admin", path = "super_admin").toBoolean().not()) {
        window.alert("Page only accessible by super admins")
        window.history.back()

        return
    }

    document.title = "Super Admin Homepage"

    var departmentName: String by remember { mutableStateOf("") }
    var createDepartmentButtonClicked by remember { mutableStateOf(false) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var createAdminButtonClicked by remember { mutableStateOf(false) }
    var adminDepartment: String? by remember { mutableStateOf(null) }
    val departments = remember { mutableStateListOf<Department>() }
    var doesEmailExist: Boolean

    val coroutineScope = rememberCoroutineScope()

    // get departments from the super admin's organization when the page loads
    LaunchedEffect(true) {
        departments.clear()

        // check if there are any departments, since each admin belongs to a department
        when (val response = getDepartments(organizationID = organizationID)) {
            is DepartmentApiResponse.Error -> {
                // window.alert(response.errorMessage)
                // window.stop()
                // window.location.reload()
            }

            is DepartmentApiResponse.Success -> {
                departments.addAll(response.data)
                println(departments.map { it.departmentName })
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
            BSButton(
                text = "Create Department",
                modifier = buttonStyles.toModifier().showModalOnClick(id = "createDepartmentModal"),
                onClick = {},
            )

            P {}

            // sometimes, especially when server has just started, the page will reload when this button is clicked
            // because the departments have not been gotten yet
            // after the first reload it will work as intended
            BSButton(
                text = "Add Admin",
                modifier = buttonStyles.toModifier().showModalOnClick(id = "createAdminModal"),
                disabled = departments.isEmpty(),
                onClick = {
                    if (departments.isEmpty()) {
                        window.alert("Add at least 1 department before creating admin")
                        window.stop()
                        window.location.reload()
                    }
                },
            )

            if (departments.isEmpty()) {
                Row(modifier = Modifier.color(Color.red)) {
                    Text("Create a department to add an admin")
                }
            }

            P {}

            BSButton(
                text = "Logout",
                modifier = buttonStyles.toModifier(),
                onClick = {
                    logoutUser(path = "super_admin")
                },
            )

            // the modals are only displayed after the corresponding button is clicked
            BSModal(
                id = "createDepartmentModal",
                title = "Create Department",
                positiveButtonText = "Create",
                negativeButtonText = "Close",
                modifier = textInputStyles.toModifier().backgroundColor(Color.black),
                onPositiveButtonClick = {
                    println("clicked")

                    departmentName = trimStatement(departmentName)

                    // check if the department name is valid
                    if (isDepartmentNameValid(departmentName).not()) {
                        window.alert("Department name is not valid")

                        departmentName = ""

                        return@BSModal
                    }

                    // check if the organization has a similarly named department
                    if (departments.map { it.departmentName }.contains(departmentName)) {
                        window.alert("Department with similar name already exists")

                        departmentName = ""

                        return@BSModal
                    }

                    coroutineScope.launch {
                        createDepartmentButtonClicked = true

                        when (
                            val response =
                                createDepartment(
                                    departmentName = departmentName,
                                    organizationID = organizationID,
                                )
                        ) {
                            is DepartmentApiResponse.Error -> {
                                window.alert(response.errorMessage)
                            }

                            is DepartmentApiResponse.Success -> {
                                showToast("createDepartmentSuccessfulToast")

                                departments.addAll(response.data)

                                println(departments.map { it.departmentName })

                                departmentName = ""
                            }
                        }
                        createDepartmentButtonClicked = false
                    }
                },
                onNegativeButtonClick = {
                    departmentName = ""
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
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Department Name: ")

                            BSInput(
                                type = InputType.Text,
                                value = departmentName,
                                label = "Name of Department",
                                floating = true,
                                disabled = createDepartmentButtonClicked,
                                required = true,
                                validation =
                                    InputValidation(
                                        isValid = isDepartmentNameValid(departmentName),
                                        isInvalid = isDepartmentNameValid(departmentName).not(),
                                        validFeedback = "Valid Name",
                                        invalidFeedback = if (departmentName.isBlank()) "" else "Invalid name",
                                    ),
                                onValueChange = {
                                    departmentName = it
                                },
                                modifier = textInputStyles.toModifier(),
                            )
                        }
                    }
                },
            )

            BSModal(
                id = "createAdminModal",
                title = "Create Admin",
                positiveButtonText = "Add",
                negativeButtonText = "Close",
                modifier = textInputStyles.toModifier().backgroundColor(Color.black),
                onPositiveButtonClick = {
                    createAdminButtonClicked = true

                    firstName = trimStatement(firstName)
                    lastName = trimStatement(lastName)
                    phone = trimStatement(phone)
                    email = trimStatement(email)
                    password = trimStatement(password)

                    // check validity of fields
                    if (isNameValid(firstName).not() || isNameValid(lastName).not() ||
                        isPhoneNumberValid(phone = phone).not() || isEmailValid(email = email).not() ||
                        isPasswordValid(password = password).not() // || createAdminButtonClicked
                    ) {
                        window.alert("Fill all fields till marked valid")

                        createAdminButtonClicked = false

                        return@BSModal
                    }

                    // check if the passed email exists
                    coroutineScope.launch {
                        val doesEmailExistResponse =
                            window.api.get("getifemailexists?email=$email").decodeToString()
                        val doesEmailExistResponseMap =
                            Json.decodeFromString<Map<String, Boolean>>(doesEmailExistResponse)

                        doesEmailExist = doesEmailExistResponseMap["exists"] ?: false

                        println("Does email exist? $doesEmailExist")

                        if (doesEmailExist) {
                            window.alert("The email you entered already exists\nUse a different email address")

                            email = ""
                            password = ""
                            createAdminButtonClicked = false

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
                                    isAdmin = true,
                                    departmentID = departments.first { it.departmentName == adminDepartment }.departmentID,
                                    organizationID = organizationID,
                                )
                        ) {
                            is UserApiResponse.Error -> {
                                window.alert(response.errorMessage)
                            }

                            is UserApiResponse.Success -> {
                                showToast("addAdminSuccessfulToast")

                                firstName = ""
                                lastName = ""
                                phone = ""
                                email = ""
                                password = ""
                                adminDepartment = ""
                            }
                        }

                        createAdminButtonClicked = false
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
                            placeholder = "Type Here",
                            floating = true,
                            disabled = createAdminButtonClicked,
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
                            type = InputType.Text,
                            value = lastName,
                            label = "Last Name",
                            placeholder = "Type Here",
                            floating = true,
                            disabled = createAdminButtonClicked,
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
                            modifier = textInputStyles.toModifier(), // .padding ( topBottom = 5.px )
                        )

                        P {}

                        // International Format, eg +254722333444
                        BSInput(
                            type = InputType.Tel,
                            value = phone,
                            label = "Phone Number (Intl. format)",
                            placeholder = "Type Here",
                            floating = true,
                            disabled = createAdminButtonClicked,
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
                            modifier = textInputStyles.toModifier(),
                        )

                        P {}

                        BSInput(
                            type = InputType.Email,
                            value = email,
                            label = "Email Address",
                            placeholder = "Type Here",
                            floating = true,
                            disabled = createAdminButtonClicked,
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
                            modifier = textInputStyles.toModifier(),
                        )

                        P {}

                        BSInput(
                            type = InputType.Password,
                            value = password,
                            label = "Password",
                            placeholder = "Enter your Password",
                            floating = true,
                            disabled = createAdminButtonClicked,
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
                            modifier = textInputStyles.toModifier(),
                        )

                        P {}

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Select Department:")

                            BSDropdown(
                                placeholder = "Select One",
                                items =
                                    departments.takeIf { it.isNotEmpty() }?.map { it.departmentName }?.sorted()
                                        ?: arrayListOf("If you're seeing this, reload the page"),
                                direction = DropdownDirection.Right,
                                onItemSelect = { _, value -> adminDepartment = value },
                            )
                        }
                    }
                },
            )

            P {}
        }
    }

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
            id = "addAdminSuccessfulToast",
            title = "Success",
            body = "Admin has been created successfully",
            autoHide = true,
            indicatorStyle = BackgroundStyle.Success,
            onCloseClick = {},
        )
    }
}

suspend fun getDepartments(organizationID: String): DepartmentApiResponse {
    val response = window.api.get("getdepartments?organization_id=$organizationID")

    println("in getDepartments function")
    println(response.decodeToString())

    return Json.decodeFromString<DepartmentApiResponse>(response.decodeToString())
}

suspend fun createDepartment(
    departmentName: String,
    organizationID: String,
): DepartmentApiResponse {
    println("in createDepartment function")

    val department =
        Department(
            departmentID = "",
            departmentName = departmentName,
            organizationID = organizationID,
        )

    try {
        val response =
            window.api.post(
                apiPath = "adddepartment",
                headers = mapOf("Content-Type" to "application/json"),
                body = Json.encodeToString<Department>(department).encodeToByteArray(),
            )

        println("createDepartment response: ${response.decodeToString()}")

        return Json.decodeFromString<DepartmentApiResponse>(response.decodeToString())
    } catch (e: Exception) {
        println("Error in createDepartment: ${e.message}")

        return DepartmentApiResponse.Error(errorMessage = "Failed to create department. ${e.message}")
    }
}

suspend fun createUser(
    firstName: String,
    lastName: String,
    phone: String,
    email: String,
    password: String,
    departmentID: String,
    organizationID: String,
    isAdmin: Boolean,
): UserApiResponse {
    val user =
        User(
            userID = "",
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            phone = phone,
            isSuperAdmin = false,
            isAdmin = isAdmin,
            organizationID = organizationID,
            departmentID = departmentID,
        )

    val response =
        window.api.post(
            apiPath = "adduser",
            headers = mapOf("Content-Type" to "application/json"),
            body = Json.encodeToString<User>(user).encodeToByteArray(),
        )

    println(response.decodeToString())

    return Json.decodeFromString<UserApiResponse>(response.decodeToString())
}
