package com.alex.Karat.util

import kotlinx.browser.window

fun homepageNavigation(path: String) {
    when (path) {
        "super_admin" -> {
            window.location.replace("/super_admin/homepage")
        }
        "admin" -> {
            window.location.replace("/admin/homepage")
        }
        "employee" -> {
            window.location.replace("/employee/homepage")
        }
    }
}
