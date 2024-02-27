`Karat` is a Leave Application and Management [Kobweb](https://github.com/varabyte/kobweb) project using [KotlinBootstrap](https://github.com/stevdza-san/KotlinBootstrap) framework for a simple user-friendly UI and [Exposed](https://github.com/JetBrains/Exposed) ORM framework for a MySQL Database. It streamlines the process of requesting, approving, and managing leave applications within an organization.

## Features

* Supports various leave types (e.g., Bereavement Leave, Medical Leave)
* Role-based access control with Super Admin, Admin, and Employee roles
* Leave approval workflow with reason provision
* Employee leave history and status tracking
* Super Admin management of departments and Admin accounts



## Getting Started

1) First, ensure you have Java and Kotlin installed.
2) Clone the project:
```bash
$ git clone https://github.com/eugenewainaina/Karat.git
```
3) Run the development server by typing the following command in a terminal under the `site` folder:

```bash
$ cd site
$ kobweb run
```

Press `Q` in the terminal to gracefully stop the server.

The first page is the login page [http://localhost:8080/login](http://localhost:8080/login). You can create an account (a Super Admin Account) in the sign up page [http://localhost:8080/signup](http://localhost:8080/signup)

## Structure

The login details are stored in a cookie (the username, Department_ID, Organization_ID, and Admin and Super Admin determining details)

On Signup, a `Super Admin` account is created along with the specified `Organization` details. After creating a Super Admin account, you will need to create a `Department` so you can create an assign an `Admin` account to a Department in the Super Admin Homepage. You can then create an `Employee` account from the Admin account Homepage.

`Employees` apply for different kinds of `leaves` (eg Bereavement Leave, Medical Leave, etc) with reason and after  leave request is made, an Admin in that particular Department can view and approve/reject the leave with reason.
The Employee can see the applications they made and their status.

Super Admins can view and manage Departments in the organization and the Admin Accounts for each Department.





