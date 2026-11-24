# Testing and Quality Assurance

This document records how the Library Management System was tested, the problems that were found and fixed, and the known limitations.

## How to run the tests
```
javac *.java
java LibraryTests
```
Expected last line: `Total: 31 | Passed: 31 | Failed: 0`

## Test strategy
| Type | What it checks | Tests |
|------|----------------|-------|
| Unit tests | One class or method alone (data classes, validation, search) | 11 |
| Integration tests | Service, data classes and file storage working together (issue, return, fine, save and reload) | 12 |
| Edge case and system tests | Boundary values and wrong data (day 7 and day 8, damaged files, special characters) | 6 |
| Performance tests | Speed and correctness with 2000 books | 2 |
| Manual user tests | Running the menu with normal and wrong inputs | done by hand |

## Test cases
| ID | Description | Expected result |
|----|-------------|-----------------|
| U1 | Add two books | Ids are 1 and 2 |
| U2 | New book | Available |
| U3 | Book to line and back | Same values |
| U4 | Member to line and back | Same values |
| U5 | IssueRecord to line and back | Same values, active or returned |
| U6 | Empty title | Rejected |
| U7 | Blank member name | Rejected |
| U8 | Title with the `\|` character | Character removed |
| U9 | Search with capital letters | Book found |
| U10 | Search by author | Book found |
| U11 | Search with no match | Empty list |
| I1 | Issue a book | Book becomes unavailable |
| I2 | Issue an issued book | Rejected |
| I3 | Unknown book id | Rejected |
| I4 | Unknown member id | Rejected |
| I5 | Return on time | Fine Rs 0, book available |
| I6 | Return 3 days late | Fine Rs 6 |
| I7 | Return a book that is not issued | Rejected |
| I8 | Member borrows a 4th book | Rejected (limit is 3) |
| I9 | Borrow again after a return | Allowed |
| I10 | Close and reopen the program | Books, members and records are loaded |
| I11 | Issued status after reopening | Kept |
| I12 | New id after reopening | Continues correctly |
| E1 | Return on exactly day 7 | No fine |
| E2 | Return on day 8 | Fine Rs 2 |
| E3 | Return date before issue date | Rejected |
| E3b | Book status after rejected return | Still issued |
| E4 | Damaged data file | Clear error, no crash |
| E5 | Title with `\|` saved and reloaded | Correct title |
| P1 | Add 2000 books | Less than 3 seconds |
| P2 | Reload 2000 books | All 2000 present |

## Bugs found and fixed
| ID | Problem found | Fix |
|----|---------------|-----|
| BUG-1 | A damaged line in a data file crashed the program with `NumberFormatException` | The loading code now reports a clear `LibraryException` message |
| BUG-2 | A book could be returned with a date before its issue date | Such a return is rejected and the book stays issued |
| BUG-3 | The program crashed with `NoSuchElementException` when the input stream was closed | The menu now exits safely |
| BUG-4 | Every add operation rewrote all data files, so adding 2000 books took about 910 ms | New books and members are appended to the file, which now takes about 100 ms (about 9 times faster; exact times depend on the computer) |

## Known limitations
- Data is stored in plain text files, not in a database.
- Two people cannot use the same data files at the same time.
- Two members can have the same name.
- Searching with an empty keyword shows all books.
- The program is single-user and console based.
