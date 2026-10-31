# 🚆 Smart Railway Reservation System

A Java desktop application that simulates the core workflow of a railway
reservation system. Users can search for trains, book seats, check
ticket status using a PNR, and cancel reservations. Administrators can
review users, trains, tickets, and booking reports through a graphical
interface.

Built as an educational project to practise **Java, object-oriented
programming, Java Swing, JDBC, and SQLite**.

------------------------------------------------------------------------

## 📌 Table of Contents

-   [Overview](#-overview)
-   [Features](#-features)
-   [Technology Stack](#-technology-stack)
-   [Architecture](#-architecture)
-   [Project Structure](#-project-structure)
-   [Database Design](#-database-design)
-   [Requirements](#-requirements)
-   [Getting Started](#-getting-started)
-   [Default Admin Login](#-default-admin-login)
-   [How the Application Works](#-how-the-application-works)
-   [Important Implementation Notes](#-important-implementation-notes)
-   [Future Improvements](#-future-improvements)
-   [Learning Outcomes](#-learning-outcomes)
-   [License](#-license)

------------------------------------------------------------------------

## 🎯 Overview

The Smart Railway Reservation System provides a simple desktop interface
for managing common railway booking tasks. Instead of handling
reservations manually, users can complete the main booking workflow
through the application, while the system stores relevant records in a
local SQLite database.

The project is organized into separate interface, service, and database
components. This separation makes the code easier to navigate, maintain,
and explain.

> **Project scope:** This is an educational desktop application, not an
> official railway booking platform. It does not provide live railway
> data or real ticket reservations.

## ✨ Features

### 👤 User Features

-   **Registration and login** --- create an account and sign in.
-   **Train search** --- search for trains by source and destination.
-   **Class and seat selection** --- choose a travel class and number of
    seats.
-   **Ticket booking** --- enter passenger details and save a
    reservation.
-   **PNR generation** --- receive a PNR reference for a booking.
-   **PNR status check** --- look up ticket details and status.
-   **Booking history** --- view previous bookings.
-   **Ticket cancellation** --- cancel eligible tickets belonging to the
    logged-in user.
-   **Refund estimate** --- view the estimated refund amount before
    cancellation.

### 🛠️ Admin Features

-   View registered users.
-   View train information and fares.
-   Review tickets and booking records.
-   Open summary and train-wise reports.
-   View confirmed bookings, cancellations, passenger counts, and
    confirmed-ticket revenue.

## 🧰 Technology Stack

  Technology   Purpose
  ------------ -------------------------------------------------
  Java 17      Core programming language and application logic
  Java Swing   Desktop graphical user interface
  SQLite       Local relational database storage
  JDBC         Connects Java code to SQLite
  Maven        Project and dependency configuration

### Why SQLite?

SQLite is a lightweight, serverless database that stores information in
local database files. It keeps this project easy to set up because a
separate database server is not required.

For a larger multi-user deployment, a server-based database such as
PostgreSQL or MySQL may be more suitable.

## 🏗️ Architecture

The application follows a simple layered structure:

``` text
┌─────────────────────────────────┐
│       GUI / Presentation        │
│ Java Swing screens and forms    │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│      Service / Business Logic   │
│ Authentication, booking,        │
│ cancellation, reports           │
└────────────────┬────────────────┘
                 │
                 ▼
┌─────────────────────────────────┐
│        Database Layer           │
│ JDBC connection and SQLite      │
└─────────────────────────────────┘
```

-   **GUI layer:** collects user input and displays information.
-   **Service layer:** applies the application's rules and performs
    operations.
-   **Database layer:** creates connections and saves or retrieves
    records.

Keeping these responsibilities separate helps avoid placing all
application logic inside the UI code.

## 📁 Project Structure

``` text
RAILWAY/
├── pom.xml
├── run.bat
├── README.md
├── lib/
│   └── sqlite-jdbc-3.43.0.0.jar
├── data/
│   ├── users.db
│   ├── trains.db
│   └── tickets.db
└── src/
    └── main/
        └── java/
            └── railway/
                ├── SmartRailwaySystem.java
                ├── db/
                │   ├── DBConnection.java
                │   └── DatabaseInitializer.java
                ├── services/
                │   ├── AuthService.java
                │   ├── BookingService.java
                │   └── ReportService.java
                └── gui/
                    ├── Ui.java
                    ├── LoginScreen.java
                    ├── MainMenu.java
                    ├── TrainBookingScreen.java
                    ├── PNRStatusScreen.java
                    ├── CancellationScreen.java
                    ├── ReportsScreen.java
                    └── AdminPanel.java
```

*The structure above reflects the documented project layout. If your
local folder differs, use the files actually present in your checkout.*

### Important Classes

  -----------------------------------------------------------------------
  Class                               Responsibility
  ----------------------------------- -----------------------------------
  `SmartRailwaySystem.java`           Starts the application, creates
                                      shared components, and switches
                                      between screens

  `DBConnection.java`                 Creates JDBC connections to the
                                      local SQLite database files

  `DatabaseInitializer.java`          Creates required database tables
                                      and initializes the default admin
                                      account

  `AuthService.java`                  Handles registration, login,
                                      password changes, and admin
                                      user-list access

  `BookingService.java`               Searches trains, calculates fares,
                                      books tickets, generates PNRs, and
                                      handles cancellations

  `ReportService.java`                Produces booking summaries and
                                      train-wise reports

  `Ui.java`                           Provides shared UI styling helpers

  `TrainBookingScreen.java`           Supports train search, class
                                      selection, fare display, and
                                      booking

  `PNRStatusScreen.java`              Displays ticket details and booking
                                      information

  `CancellationScreen.java`           Checks ticket eligibility,
                                      estimates refunds, and cancels
                                      tickets

  `ReportsScreen.java`                Displays booking summaries and
                                      train-wise reports

  `AdminPanel.java`                   Displays administrative tables for
                                      tickets, users, and trains
  -----------------------------------------------------------------------

## 🗄️ Database Design

The application uses three local SQLite database files.

### `users.db`

Stores user account information, including the user ID, username,
password, and role (`user` or `admin`).

### `trains.db`

Stores train information such as train code, name, source, destination,
departure and arrival times, journey duration, and class fares. It also
contains seat-availability information for each train and class.

### `tickets.db`

Stores reservation records, including:

-   PNR and username
-   Passenger name and age
-   Train and journey details
-   Travel date and selected class
-   Number of seats and total fare
-   Ticket status (`CONFIRMED` or `CANCELLED`)
-   Booking date and time

The database initialization code creates required tables when the
application starts.

## 💻 Requirements

Before running the application, make sure you have:

-   **Java Development Kit (JDK) 17 or later**
-   The project source files
-   The SQLite JDBC driver included with the project
-   Maven, if you want to build the project through Maven

Check your Java installation with:

``` bash
java -version
javac -version
```

Both commands should report a compatible Java version.

## 🚀 Getting Started

### Option 1: Run on Windows

1.  Download or clone the project repository.
2.  Open the project folder.
3.  Run `run.bat`.
4.  Follow the instructions displayed by the application.

The batch file should be used as the primary launch option if it is
configured for your current project setup.

### Option 2: Run from the command line

If the compiled classes and source list already exist, compile the
source files:

``` bash
javac -encoding UTF-8 -d target/classes @target/sources.txt
```

Then launch the application:

``` bash
java -cp "target/classes;lib/sqlite-jdbc-3.43.0.0.jar" railway.SmartRailwaySystem
```

These classpath commands use Windows path separators. On macOS or Linux,
use `:` instead of `;` in the classpath and adapt the launch steps to
your environment.

If `target/sources.txt` or the compiled classes are missing, use the
project's configured build process to generate them first.

### First launch

The application initializes its required database tables and stores
local data under the `data/` directory. Keep this folder if you want to
retain the records created during a demonstration.

## 🔐 Default Admin Login

The documented default administrator credentials are:

``` text
Username: admin
Password: admin123
```

Enter the CAPTCHA displayed on the login screen as well.

**Security note:** These are demonstration credentials. Change or
disable default credentials before sharing a runnable build, and do not
use real or sensitive passwords in this educational project.

## 🔄 How the Application Works

### Booking a ticket

1.  The user logs in.
2.  The user enters a source and destination.
3.  The application searches for matching trains.
4.  The user selects a train, travel class, and number of seats.
5.  The application checks seat availability.
6.  The user enters passenger details.
7.  The system calculates the fare and updates the available seat count.
8.  The system generates a PNR and saves the reservation in
    `tickets.db`.

### Cancelling a ticket

1.  The user enters the PNR.
2.  The application retrieves the ticket details.
3.  The system checks that the ticket belongs to the logged-in user and
    has not already been cancelled.
4.  The ticket status is updated to `CANCELLED`.
5.  The seats are returned to the availability records.
6.  The application displays the estimated refund.

The documented implementation uses an estimated **75% refund**. This is
a rule in the project, not an official railway refund policy.

## ⚠️ Important Implementation Notes

-   **Local storage:** The database files are stored on the local
    machine.
-   **Password storage:** The current documented implementation stores
    passwords as plain text. Password hashing should be implemented
    before treating the application as secure.
-   **Refund calculation:** The displayed refund is an estimate based on
    the application's configured rule.
-   **Railway data:** Train and fare information is managed by the
    application; live railway schedules and official booking systems are
    not integrated.
-   **Refresh controls:** Use the relevant Refresh buttons to reload
    table or report data where available.
-   **Intended use:** The application is designed for learning and
    demonstration, not production railway operations.

## 🔮 Future Improvements

Potential improvements include:

-   Hash passwords using a suitable password-hashing algorithm.
-   Add stronger input validation and error handling.
-   Introduce online payment integration.
-   Send booking confirmations through email or SMS.
-   Add live train tracking through an appropriate data source.
-   Improve date, passenger, and booking validation.
-   Add automated unit and integration tests.
-   Migrate to MySQL or PostgreSQL for a multi-user deployment.
-   Develop web and mobile versions.

## 🎓 Learning Outcomes

This project provides practical experience with:

-   Java classes, objects, and object-oriented programming.
-   Building desktop interfaces using Java Swing.
-   Separating presentation, business logic, and data access.
-   Connecting Java applications to databases with JDBC.
-   Designing relational tables with SQLite.
-   Implementing booking, cancellation, PNR lookup, and reporting
    workflows.
-   Organizing a multi-class Java project.

## 📄 License

This project is intended for educational and demonstration use. Add a
`LICENSE` file to the repository if you want to specify formal reuse and
distribution terms.
