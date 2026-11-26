# Library Management System (Core Java)

A console-based Library Management System built with Core Java as the practical project of my Full Stack Developer internship.

## Features
- Add and view books and members
- Issue a book to a member (maximum 3 books per member)
- Return a book with automatic fine calculation (7 days allowed, Rs 2 per extra day)
- Search books by title or author
- View all currently issued books
- Data is saved in text files, so it is not lost when the program closes
- Backup of all data from the menu
- Log file for monitoring (`logs` folder)

## Project structure
| File | Responsibility |
|------|----------------|
| `Book.java`, `Member.java`, `IssueRecord.java` | Data classes (model) |
| `LibraryService.java` | Business rules (issue, return, fine, search) |
| `DataStore.java` | Saving and loading data from files |
| `LibraryException.java` | Custom exception for clear error messages |
| `Main.java` | Menu and user input |
| `LibraryTests.java` | 31 automated test cases |
| `TESTING.md` | Test documentation and bug report |
| `DEPLOYMENT.md` | Build, release, monitoring, backup and rollback guide |
| `.github/workflows/ci-cd.yml` | GitHub Actions pipeline: build, test, staging check, release |

## How to run
```
javac *.java
java Main
```

## Download and run the released version
Open the **Releases** section of this repository, download `library-management-system.jar` and run:
```
java -jar library-management-system.jar
```
See [DEPLOYMENT.md](DEPLOYMENT.md) for settings, monitoring and rollback.

## How to run the tests
```
java LibraryTests
```
33 automated tests cover unit, integration, edge case and performance scenarios. See [TESTING.md](TESTING.md) for the test cases, bugs found and fixed, and known limitations.

## Concepts used
OOP (encapsulation, abstraction), ArrayList, custom exceptions, file handling with java.nio, java.time.LocalDate, input validation, layered design.
