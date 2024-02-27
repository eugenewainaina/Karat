package com.alex.Karat.databaseOperations

import models.Department
import models.DepartmentLeave
import models.Leave
import models.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileInputStream
import java.util.*

// this variable stores the absolute path to the databaseinfo.properties file
// when first accessing this project, copy the absolute path of the databaseinfo.properties file in the util package
// // and paste it as so
val databaseinfoPropertiesFilePath =
    "C:\\Users\\eugen\\Desktop\\DesktopStuff\\Karat" +
        "\\KaratWebsite\\site\\src\\jvmMain\\kotlin\\com\\alex\\Karat\\util\\databaseInfo.properties"

// this is a singleton object
// the init block is called whenever the object is instantiated
// since it is a singleton object, there is only one instance of it throughout the entire project
// so all instances, if there are more than one, refer to the same instance
object DatabaseFactory {
    init {
        // load the databaseinfo.properties file
        val properties = Properties()
        properties.load(FileInputStream(databaseinfoPropertiesFilePath))

        // make a database connection
        // this is in the init block so that the connection is established
        // // whenever the DatabaseFactory singleton object is instantiated
        // reference the url, driver, user, and password from the properties file
        Database.connect(
            url = properties.getProperty("database.url"),
            driver = properties.getProperty("database.driver"),
            user = properties.getProperty("database.user"),
            password = properties.getProperty("database.password"),
        )

        // create the tables if they do not exist
        transaction {
            SchemaUtils.create(Organizations, Departments, Users, Leaves)
        }
    }

    // //////////////////   database functions  ////////////////////

    // checks if an email is present in the database and returns true or false
    fun doesEmailExist(email: String): Boolean {
        var emailExists = true

        transaction {
            // The query is converted to a list and if the list isn't empty, the email exists (true)
            emailExists = Users.select(Users.email eq email).toList().isNotEmpty()
        }

        return emailExists
    }

    // creates a new admin, super admin, or employee
    fun addUser(user: User): UUID? {
        var userID: UUID?

        return transaction {
            try {
                userID =
                    Users.insertAndGetId {
                        it[firstName] = user.firstName
                        it[lastName] = user.lastName
                        it[email] = user.email
                        it[password] = user.password
                        it[phone] = user.phone
                        it[isSuperAdmin] = user.isSuperAdmin
                        it[isAdmin] = user.isAdmin
                        it[organizationId] = UUID.fromString(user.organizationID)
                        it[departmentId] = if (user.departmentID == null) null else UUID.fromString(user.departmentID)
                    }.value

                // if successful, returns the user ID
                userID
            } catch (e: Exception) {
                // if unsuccessful, returns null
                null
            }
        }
    }

    // adds an organization
    fun addOrganization(orgName: String): UUID? {
        var organizationID: UUID?

        return transaction {
            try {
                organizationID =
                    Organizations.insertAndGetId {
                        it[organizationName] = orgName
                    }.value

                organizationID
            } catch (e: Exception) {
                null
            }
        }
    }

    // adds a leave application
    fun addLeave(leave: Leave): UUID? {
        var leaveID: UUID?

        return transaction {
            try {
                leaveID =
                    Leaves.insertAndGetId {
                        it[userId] = UUID.fromString(leave.userID)
                        it[startDate] = leave.startDate
                        it[endDate] = leave.endDate
                        it[duration] = leave.duration
                        it[applicationDate] = leave.applicationDate
                        it[leaveType] = leave.leaveType
                        it[status] = leave.status
                        it[requestReason] = leave.requestReason
                        it[responseReason] = null
                        it[approvedBy] = if (leave.approvedBy == null) null else UUID.fromString(leave.approvedBy)
                    }.value

                leaveID
            } catch (e: Exception) {
                null
            }
        }
    }

    // approve/reject a leave request
    fun updateLeave(
        leaveID: UUID,
        adminID: UUID,
        newResponseReason: String?,
        newStatus: String,
    ): Int {
        return transaction {
            try {
                // will return 0 or more, depending on the number of affected rows
                Leaves.update({ Leaves.id eq leaveID }) {
                    it[status] = newStatus
                    it[approvedBy] = adminID
                    it[responseReason] = newResponseReason
                }
            } catch (e: Exception) {
                // return -2 instead if transaction fails
                -2
            }
        }
    }

    // adds a new department
    fun addDepartment(department: Department): UUID? {
        var departmentID: UUID?

        return transaction {
            try {
                departmentID =
                    Departments.insertAndGetId {
                        it[departmentName] = department.departmentName
                        it[organizationId] = UUID.fromString(department.organizationID)
                    }.value

                departmentID
            } catch (e: Exception) {
                null
            }
        }
    }

    fun checkPendingLeave(userID: UUID): Boolean {
        var pendingLeave = false

        transaction {
            pendingLeave =
                Leaves.select(Leaves.userId eq userID and (Leaves.status eq "PENDING"))
                    .toList().isNotEmpty()
        }

        return pendingLeave
    }

    /*fun checkExistingDepartment(organizationID: UUID): Boolean {
        var existingDepartment = false

        try {p
            transaction {
                existingDepartment =
                    Departments.select(Departments.organizationId eq organizationID).toList().isNotEmpty()
            }
        } catch (e: Exception) {
            println(e.message)
        }

        return existingDepartment
    }*/

    fun getDepartments(organizationID: UUID): List<Department> {
        val departmentList = arrayListOf<Department>()

        transaction {
            try {
                Departments.select(Departments.organizationId eq organizationID)
                    .map { departmentList.add(it.toDepartment()) }

                addLogger(StdOutSqlLogger)
            } catch (e: Exception) {
                println(e.message)
            }
        }

        return departmentList
    }

    // returns a user's leaves
    fun getUserLeaves(
        userID: UUID,
        limit: Int,
        offset: Int,
    ): List<Leave> {
        val leaveList = arrayListOf<Leave>()

        transaction {
            try {
                Leaves.select(Leaves.userId eq userID).limit(n = limit, offset = offset.toLong())
                    .map { leaveList.add(it.toLeave()) }
            } catch (e: Exception) {
                println(e.message)
            }
        }

        return leaveList
    }

    // return leaves belonging to an admin's department
    fun getDepartmentLeaves(
        departmentID: UUID,
        limit: Int,
        offset: Int,
    ): List<DepartmentLeave> {
        val departmentLeaves = mutableListOf<DepartmentLeave>()

        transaction {
            // Join Users and Departments on departmentId and then join the result with Leaves
            val userDepartmentJoin =
                Users.join(Departments, JoinType.INNER, additionalConstraint = { Users.departmentId eq Departments.id })
            val leavesJoin =
                userDepartmentJoin.join(Leaves, JoinType.INNER, additionalConstraint = { Leaves.userId eq Users.id })

            leavesJoin.select { Departments.id eq departmentID }.limit(n = limit, offset = offset.toLong())
                .map { it.toDepartmentLeave() }
                .forEach { departmentLeaves.add(it) }
        }

        return departmentLeaves
    }

    // returns a User list with a User object with the matching email
    fun getUser(email: String): List<User> {
        val userList = arrayListOf<User>()

        transaction {
            try {
                Users.select(Users.email eq email).map { userList.add(it.toUser()) }
            } catch (e: Exception) {
                println("getUser transaction failed: ${e.message}")
            }
        }

        return userList
    }

    // returns employees/users belonging to a certain department
    fun getUsers(
        departmentID: UUID,
        isAdmin: Boolean,
        isSuperAdmin: Boolean,
        limit: Int,
        offset: Int,
    ): List<User> {
        val users = arrayListOf<User>()

        transaction {
            try {
                Users.select(Users.departmentId eq departmentID and (Users.isAdmin eq isAdmin) and (Users.isSuperAdmin eq isSuperAdmin))
                    .limit(n = limit, offset = offset.toLong())
                    .map { users.add(it.toUser()) }
            } catch (e: Exception) {
                println("getUsers transaction failed".plus(e.message))
            }
        }

        return users
    }
}

// extension function that converts a query result to a Leave object
private fun ResultRow.toLeave(): Leave {
    return Leave(
        leaveID = this[Leaves.id].toString(),
        userID = this[Leaves.userId].toString(),
        startDate = this[Leaves.startDate],
        endDate = this[Leaves.endDate],
        duration = this[Leaves.duration],
        applicationDate = this[Leaves.applicationDate],
        leaveType = this[Leaves.leaveType],
        status = this[Leaves.status],
        requestReason = this[Leaves.requestReason],
        responseReason = this[Leaves.responseReason],
        approvedBy = this[Leaves.approvedBy].toString(),
    )
}

// extension function that converts a query result to a User object
private fun ResultRow.toUser(): User {
    return User(
        userID = this[Users.id].toString(),
        firstName = this[Users.firstName],
        lastName = this[Users.lastName],
        email = this[Users.email],
        password = this[Users.password],
        phone = this[Users.phone],
        isSuperAdmin = this[Users.isSuperAdmin],
        isAdmin = this[Users.isAdmin],
        organizationID = this[Users.organizationId].toString(),
        departmentID = this[Users.departmentId].toString(),
    )
}

// extension function that converts a query result to a Department object
private fun ResultRow.toDepartment(): Department {
    return Department(
        departmentID = this[Departments.id].toString(),
        departmentName = this[Departments.departmentName],
        organizationID = this[Departments.organizationId].toString(),
    )
}

// extension function that returns a department leave
private fun ResultRow.toDepartmentLeave(): DepartmentLeave {
    return DepartmentLeave(
        employeeID = this[Users.id].toString(),
        firstName = this[Users.firstName],
        lastName = this[Users.lastName],
        email = this[Users.email],
        leaveID = this[Leaves.id].toString(),
        startDate = this[Leaves.startDate],
        endDate = this[Leaves.endDate],
        applicationDate = this[Leaves.applicationDate],
        duration = this[Leaves.duration],
        leaveType = this[Leaves.leaveType],
        requestReason = this[Leaves.requestReason],
        responseReason = this[Leaves.responseReason],
        status = this[Leaves.status],
        approvedBy = this[Leaves.approvedBy].toString(),
    )
}
