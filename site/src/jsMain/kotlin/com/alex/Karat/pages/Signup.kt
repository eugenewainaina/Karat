package com.alex.Karat.pages

import androidx.compose.runtime.*
import com.alex.Karat.styles.buttonStyles
import com.alex.Karat.styles.headingStyle
import com.alex.Karat.styles.textInputStyles
import com.alex.Karat.util.*
import com.stevdza.san.kotlinbs.components.BSButton
import com.stevdza.san.kotlinbs.forms.BSInput
import com.stevdza.san.kotlinbs.models.InputValidation
import com.varabyte.kobweb.browser.api
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.style.toModifier
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.User
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import responses.UserApiResponse

@Page
@Composable
fun Signup() {
    // input field variables
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // button variables
    var signUpButtonClicked by remember { mutableStateOf(false) }
    var backLoginButtonClicked by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var doesEmailExist: Boolean

    // set tab name
    document.title = "Sign Up"

    Box(
        modifier = Modifier.fillMaxSize().backgroundColor(Color.black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            // modifier = Modifier.backgroundColor(Color.orange),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = headingStyle.toModifier(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Sign Up")
            }

            P {}
            P {}

            // input fields
            BSInput(
                modifier = textInputStyles.toModifier(),
                type = InputType.Text,
                value = firstName,
                label = "First Name",
                placeholder = "Type Here",
                floating = true,
                disabled = signUpButtonClicked,
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
            )

            P {}

            BSInput(
                type = InputType.Text,
                value = lastName,
                label = "Last Name",
                placeholder = "Type Here",
                floating = true,
                disabled = signUpButtonClicked,
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
                disabled = signUpButtonClicked,
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
                modifier = textInputStyles.toModifier(),
                type = InputType.Email,
                value = email,
                label = "Email Address",
                placeholder = "Type Here",
                floating = true,
                required = true,
                disabled = signUpButtonClicked,
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
                modifier = textInputStyles.toModifier(),
                type = InputType.Password,
                value = password,
                label = "Password",
                placeholder = "Enter your Password",
                floating = true,
                required = true,
                disabled = signUpButtonClicked,
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
            )

            P {}

            // back to login page button
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BSButton(
                    modifier = buttonStyles.toModifier(),
                    text = "Back",
                    loading = backLoginButtonClicked,
                    loadingText = "Please wait...",
                    disabled = signUpButtonClicked,
                    onClick = {
                        backLoginButtonClicked = true
                        window.location.replace("/login")
                    },
                )

                // validate the input values and sign a user in when done
                BSButton(
                    modifier = buttonStyles.toModifier(),
                    text = "Sign Up",
                    loading = signUpButtonClicked,
                    loadingText = "Please wait...",
                    disabled = (
                        isNameValid(firstName).not() || isNameValid(lastName).not() ||
                            isPhoneNumberValid(phone = phone).not() || isEmailValid(email = email).not() ||
                            isPasswordValid(password = password).not() || signUpButtonClicked || backLoginButtonClicked
                    ),
                    onClick = {
                        signUpButtonClicked = true

                        // a new thread is started
                        coroutineScope.launch {
                            // get response in a Json string format
                            val doesEmailExistResponse =
                                window.api.get("getifemailexists?email=$email").decodeToString()

                            // decode Json string into a map of String:Boolean
                            val doesEmailExistResponseMap =
                                Json.decodeFromString<Map<String, Boolean>>(doesEmailExistResponse)

                            doesEmailExist = doesEmailExistResponseMap["exists"] ?: true

                            println("Does email exist? $doesEmailExist")

                            // if the email entered is not found, proceed. Else, alert user to use different email address
                            if (doesEmailExist.not()) {
                                val organization =
                                    window.prompt(
                                        message = "You're about to create a Super Admin account:\n($email)\n\nEnter a name for your organization: ",
                                    )
                                        ?.replace("\\s+".toRegex(), " ")?.trim()

                                // if the organization name entered is valid
                                if (organization.isNullOrBlank().not()) {
                                    // create the user account
                                    when (
                                        val response =
                                            signUpPageLogin(
                                                firstName = trimStatement(firstName),
                                                lastName = trimStatement(lastName),
                                                phone = phone,
                                                email = email,
                                                password = password,
                                                organizationName = organization.toString(),
                                            )
                                    ) {
                                        // if the account was successfully created
                                        is UserApiResponse.Success -> {
                                            // log the user in
                                            when (
                                                val signInResponse =
                                                    signIn(email = email, password = password)
                                            ) {
                                                // if account details are found and correct
                                                is UserApiResponse.Success -> {
                                                    val userData = signInResponse.data
                                                    val user = userData[0]

                                                    setCookies(
                                                        userID = user.userID,
                                                        email = email,
                                                        organizationID = user.organizationID,
                                                        departmentID = user.departmentID,
                                                        isSuperAdmin = user.isSuperAdmin,
                                                        isAdmin = user.isAdmin,
                                                    )
                                                    println(document.cookie)

                                                    homepageNavigation(
                                                        path =
                                                            if (user.isSuperAdmin) {
                                                                "super_admin"
                                                            } else if (user.isAdmin) {
                                                                "admin"
                                                            } else {
                                                                "employee"
                                                            },
                                                    )
                                                }

                                                is UserApiResponse.Error -> {
                                                    window.alert(
                                                        signInResponse.errorMessage,
                                                    ) // No users found of email $email, or, incorrect password

                                                    signUpButtonClicked = false
                                                }
                                            }
                                        }

                                        // if the account was not created
                                        is UserApiResponse.Error -> {
                                            window.alert(response.errorMessage)

                                            signUpButtonClicked = false
                                        }
                                    }
                                } else {
                                    window.alert("Invalid organization name")
                                    signUpButtonClicked = false
                                }
                            } else {
                                window.alert("The email you entered already exists\nUse a different email address")

                                email = ""
                                password = ""
                                signUpButtonClicked = false
                            }
                        }
                    },
                )
            }
        }
    }
}

// creates organization and a new user
suspend fun signUpPageLogin(
    firstName: String,
    lastName: String,
    email: String,
    password: String,
    phone: String,
    organizationName: String,
): UserApiResponse {
    // send organization name in a POST request to an API to create the organization and returns the org ID
    val organizationID =
        window.api.post(apiPath = "addorganization?organizationName=$organizationName").decodeToString()

    println("org ID: $organizationID")

    // create user object with the obtained org ID
    val user =
        User(
            userID = "",
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            phone = phone,
            isSuperAdmin = true,
            isAdmin = false,
            organizationID = organizationID,
            departmentID = null,
        )

    println("encoded user: ".plus(Json.encodeToString<User>(user)))

    // call API to create the user in the database
    val response =
        window.api.post(
            apiPath = "adduser",
            headers = mapOf("Content-Type" to "application/json"),
            body = Json.encodeToString<User>(user).encodeToByteArray(),
        )

    println(response.decodeToString())

    // return the API's response
    return Json.decodeFromString<UserApiResponse>(response.decodeToString())
}
