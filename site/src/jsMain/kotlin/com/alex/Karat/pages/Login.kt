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
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.style.toModifier
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text
import responses.UserApiResponse

@Page()
@Composable
fun Login() {
    // button variables
    var signUpButtonClicked by remember { mutableStateOf(false) }
    var signInButtonClicked by remember { mutableStateOf(false) }

    // input field variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // for launching new threads
    val coroutineScope = rememberCoroutineScope()

    // set the tab name
    document.title = "Login"

    Box(
        modifier =
            textInputStyles.toModifier().fillMaxSize()
                .backgroundColor(Color.black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = headingStyle.toModifier()) {
                Text("Login")
            }

            P {}

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // email input field
                BSInput(
                    type = InputType.Email,
                    value = email,
                    label = "Email Address",
                    placeholder = "Type Here",
                    floating = true,
                    disabled = signInButtonClicked,
                    required = true,
                    validation =
                        InputValidation(
                            isValid = isEmailValid(email),
                            isInvalid = !isEmailValid(email),
                            validFeedback = "Valid Email",
                            invalidFeedback = if (email.isBlank()) "" else "Invalid Email",
                        ),
                    onValueChange = {
                        email = it.replace("\\s".toRegex(), "")
                    },
                    modifier = textInputStyles.toModifier(),
                )
            }

            P {}

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // password input field
                BSInput(
                    modifier = textInputStyles.toModifier(),
                    type = InputType.Password,
                    value = password,
                    label = "Password",
                    placeholder = "Enter your Password",
                    floating = true,
                    disabled = signInButtonClicked,
                    validation =
                        InputValidation(
                            isValid = isPasswordValid(password),
                            isInvalid = !isPasswordValid(password),
                            validFeedback = "",
                            invalidFeedback = "", // if (password.isEmpty()) "" else invalidPasswordFeedback(password)
                        ),
                    onValueChange = {
                        password = it.replace("\\s".toRegex(), "")
                    },
                    required = true,
                )
            }

            // add on enter to submit password

            P {}

            // sign in button that logs a user in
            BSButton(
                text = "Sign In",
                modifier = buttonStyles.toModifier(),
                loading = signInButtonClicked,
                loadingText = "Please wait...",
                disabled =
                    isEmailValid(email = email).not() ||
                        isPasswordValid(password = password).not() ||
                        signUpButtonClicked,
                onClick = {
                    signInButtonClicked = true

                    // start a new thread
                    coroutineScope.launch {
                        // call the SignIn function, which can only be called from within a CoroutineScope
                        when (val response = signIn(email = email, password = password)) {
                            // if the user details were successfully found
                            is UserApiResponse.Success -> {
                                val user = response.data[0]

                                // set the page's cookies
                                setCookies(
                                    userID = user.userID,
                                    email = email,
                                    organizationID = user.organizationID,
                                    departmentID = user.departmentID,
                                    isSuperAdmin = user.isSuperAdmin,
                                    isAdmin = user.isAdmin,
                                )

                                // navigate to the appropriate home screen
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

                            // if the user details were not found
                            is UserApiResponse.Error -> {
                                window.alert(response.errorMessage) // No users found of email $email

                                signInButtonClicked = false
                            }
                        }
                    }
                },
            )

            P {}

            Row(modifier = Modifier.color(Color.aquamarine)) {
                Text("No account?")
            }

            // sign up button
            BSButton(
                text = "Sign Up",
                loading = signUpButtonClicked,
                loadingText = "Please wait...",
                disabled = signInButtonClicked,
                onClick = {
                    signUpButtonClicked = true

                    window.location.assign("http://localhost:8080/signup")
                },
                modifier = buttonStyles.toModifier(),
            )
        }
    }
}

// get request to an API that queries the database for the user's details and returns the response
suspend fun signIn(
    email: String,
    password: String,
): UserApiResponse {
    val response = window.api.get("getuser?email=$email&password=$password")

    // return the User response from the API as either success or error
    return Json.decodeFromString<UserApiResponse>(response.decodeToString())
}
