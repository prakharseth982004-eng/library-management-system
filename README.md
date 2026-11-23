# Library Management System (Core Java)

A console-based Library Management System built with Core Java as the practical project of my Full Stack Developer internship.

## Features
- Add and view books and members
- Issue a book to a member (maximum 3 books per member)
- Return a book with automatic fine calculation (7 days allowed, Rs 2 per extra day)
- Search books by title or author
- View all currently issued books
- Data is saved in text files, so it is not lost when the program closes

## Project structure
| File | Responsibility |
|------|----------------|
| `Book.java`, `Member.java`, `IssueRecord.java` | Data classes (model) |
| `LibraryService.java` | Business rules (issue, return, fine, search) |
| `DataStore.java` | Saving and loading data from files |
| `LibraryException.java` | Custom exception for clear error messages |
| `Main.java` | Menu and user input |
| `LibraryTests.java` | Test cases for the main logic |

## How to run
```
javac *.java
java Main
```

## How to run the tests
```
java LibraryTests
```

## Concepts used
OOP (encapsulation, abstraction), ArrayList, custom exceptions, file handling with java.nio, java.time.LocalDate, input validation, layered design.
