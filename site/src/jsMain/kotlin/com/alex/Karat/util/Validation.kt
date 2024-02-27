package com.alex.Karat.util

// validates a name
fun isNameValid(name: String): Boolean = name.isNotBlank() && Regex("^[A-Za-z']+\\s?[A-Za-z']*\$").matches(name)

// validaes an email address
fun isEmailValid(email: String): Boolean = email.matches(Regex("""^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+(\.[a-zA-Z]{2,})+$"""))

// Email validation
// returns true if valid

fun isPhoneNumberValid(phone: String): Boolean = phone.matches(Regex("^\\+(?:[0-9] ?){6,14}[0-9]\$"))
// Phone Number validation (international number format)

// ^: Matches the beginning of the string.
// \+: Matches the plus sign for international numbers.
// (?:[0-9] ?){6,14}: Non-capturing group matching 6 to 14 digits with optional spaces in between.
// [0-9]$: Matches the final digit.

// returns true if valid

fun isPasswordValid(password: String): Boolean =
    password.matches(
        Regex(
            "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!\"#$%&'()*+,-./:;<=>?@\\[\\\\\\]^_`{|}~])[A-Za-z\\d!\"#$%&'()*+,-./:;<=>?@\\[\\\\\\]^_`{|}~]{8,}$",
        ),
    )

// Password Validation
// minimum 8 characters, at least 1 letter, 1 special character, and 1 digit

// (?=.*[a-zA-Z]): At least one letter
// (?=.*\\d): At least one digit
// (?=.*[!\"#$%&'()*+,-./:;<=>?@[\\\\\\]^_{|}~])`: At least one special character
// [A-Za-z\\d!\"#$%&'()*+,-./:;<=>?@[\\\\\\]^_{|}~]{8,}`: Minimum length of 8 characters, containing only letters, digits, and the specified special characters.

// returns true if valid

fun invalidPasswordFeedback(password: String): String {
    // Check minimum length
    if (password.length < 8) {
        return "Length must be 8 characters or more"
    }

    if (password.any { it.isWhitespace() }) {
        return "Cannot contain whitespace"
    }

    // Check for at least 1 digit
    if (password.any { it.isDigit() }.not()) {
        return "Must contain at least 1 digit"
    }

    // Check for at least 1 letter
    if (password.any { it.isLetter() }.not()) {
        return "Must contain at least 1 letter"
    }

    // Check for at least 1 special character
    val specialCharacters =
        setOf(
            '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*',
            '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '_',
            '`', '{', '|', '}', '~',
        )

    if (password.any { it in specialCharacters }.not()) {
        return "Must contain at least one special character"
    }

    // Password meets all requirements
    return "Password is strong"
}

// validates a department name
fun isDepartmentNameValid(departmentName: String): Boolean =
    departmentName.isNotBlank() and (Regex("^[A-Za-z0-9\\s'_():,\\-]+$").matches(departmentName))

// removes double spaces, trims the string, and returns it
fun trimStatement(statement: String): String = statement.replace("[^\\S\\r\\n]+".toRegex(), " ").trim()
