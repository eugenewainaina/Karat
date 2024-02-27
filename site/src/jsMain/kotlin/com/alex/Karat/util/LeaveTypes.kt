package com.alex.Karat.util

// enum that stores the various kinds of leaves allowed
// the code accesses these leaves and displays the "type" (specified in brackets) to the employee while selecting leave type
enum class LeaveTypes(val type: String) {
    Vacation("Vacation"),
    SickLeave("Sick Leave"),
    Personal("Personal leave"),
    Bereavement("Bereavement leave"),
    JuryDuty("Jury duty leave"),
    Military("Military leave"),
    Parental("Parental leave "),
    Election("Election leave"),

    LeaveOfAbsence("Leave of absence (LOA)"),
    CompensatoryTime("Compensatory time"),
    Sabbatical("Sabbatical"),
    DomesticViolence("Domestic violence leave"),
    Medical("Medical leave"),
}
