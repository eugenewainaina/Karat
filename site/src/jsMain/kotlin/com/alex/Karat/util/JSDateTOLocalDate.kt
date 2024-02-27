package com.alex.Karat.util

import kotlinx.datetime.LocalDate
import kotlin.js.Date

// converts the JS Date String to a LocalDate
fun JSDateTOLocalDate(jsDate: Date): LocalDate  {
    val year = jsDate.getFullYear().toString().padStart(4, '0')
    val month = (jsDate.getMonth() + 1).toString().padStart(2, '0') // Months are zero-based
    val day = jsDate.getDate().toString().padStart(2, '0')

    return LocalDate.parse("$year-$month-$day")
}
