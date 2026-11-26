# Deployment Guide

This guide explains how the Library Management System is prepared, built, released and monitored. The application is a Core Java console program, so "production" means a tested and versioned JAR file published as a GitHub Release that users can download and run.

## Environments
| Environment | Purpose | How it runs | Data folder |
|-------------|---------|-------------|-------------|
| Development | Writing and trying code | `java Main` on my laptop | `data` |
| Staging | Final check before release | Automatic GitHub Actions job on every push | `staging-data` (temporary, deleted after the job) |
| Production | The real version for users | JAR file from a GitHub Release (tag `v1.0.0`) | `data` (or any folder set by the user) |

## Requirements
- Java JDK 17 or newer
- No external libraries (only the Java standard library)

## Settings (environment variables)
| Variable | Meaning | Default |
|----------|---------|---------|
| `LIBRARY_DATA_DIR` | Folder where books, members and records are saved | `data` |
| `LIBRARY_LOG_DIR` | Folder where log files are written | `logs` |

Example (Windows PowerShell):
```
$env:LIBRARY_DATA_DIR = "staging-data"
java -jar library-management-system.jar
```
Example (Linux / Mac):
```
LIBRARY_DATA_DIR=staging-data java -jar library-management-system.jar
```

## Build the JAR file manually
Windows PowerShell:
```
javac -d out *.java
java -cp out LibraryTests
Remove-Item out\LibraryTests*.class
jar cfe library-management-system.jar Main -C out .
java -jar library-management-system.jar
```
Linux / Mac:
```
javac -d out *.java
java -cp out LibraryTests
rm out/LibraryTests*.class
jar cfe library-management-system.jar Main -C out .
java -jar library-management-system.jar
```

## Deployment pipeline (GitHub Actions)
The file `.github/workflows/ci-cd.yml` automates everything, so there are no manual mistakes and every release is built the same way.

1. **Trigger:** a push or pull request to `main` starts the job `build-and-test`. Pushing a tag that starts with `v` also starts the job `release`.
2. **Compile** all Java files with Java 17.
3. **Run the 33 automated tests.** The pipeline stops if any test fails.
4. **Build the JAR file.**
5. **Staging smoke test:** the JAR is started with a separate data folder (`staging-data`), a book is added, a backup is taken, and the pipeline checks that the book is shown and that a log file is created.
6. **Release (production):** only for version tags, and only if all steps above passed, the same tested JAR is published as a GitHub Release with automatic release notes.

## How to release a new version
```
git add .
git commit -m "Describe the change"
git push
```
Wait until the GitHub **Actions** tab shows a green tick, then:
```
git tag v1.0.0
git push origin v1.0.0
```
After a minute, the **Releases** section of the repository shows `v1.0.0` with the file `library-management-system.jar`.

## Monitoring and logs
The program writes a log file in the `logs` folder (`library-0.log`). Files rotate after 1 MB and 3 old files are kept. Each line has the time, the level (INFO, WARNING, SEVERE) and the message.

```
2026-11-26 17:47:22 [INFO] Application started (version 1.0.0, data folder: staging-data)
2026-11-26 17:47:22 [INFO] Loaded 0 books, 0 members, 0 records
2026-11-26 17:47:22 [INFO] Book added: id 1 "Smoke Book"
2026-11-26 17:47:22 [INFO] Backup created in staging-data/backups/20261126-174722
2026-11-26 17:47:22 [INFO] Application stopped
```
| Level | Meaning | Action |
|-------|---------|--------|
| INFO | Normal events (start, add, issue, return, backup, stop) | None |
| WARNING | A request was rejected (for example a wrong book id) | Check if users are confused |
| SEVERE | Data could not be loaded or saved | Fix immediately, restore from backup |

## Backup and restore
- Menu option **9. Backup data** copies the data files into `data/backups/<date-time>/`.
- To restore, close the program, then copy `books.txt`, `members.txt` and `records.txt` from the chosen backup folder back into the `data` folder.

## Rollback (if a release has a problem)
1. Open the **Releases** page and download the JAR of the last good version (for example `v1.0.0`) and use it again.
2. Delete the bad release and its tag on GitHub (`git push origin --delete v1.0.1`).
3. Fix the problem on `main` (for example with `git revert <commit>`), wait for the green tick and release a new version (`v1.0.2`).
4. If data was damaged, restore the latest backup.

## Security hardening
- No passwords or keys exist in the code or the repository.
- Data and log folders are listed in `.gitignore`, so personal data is never uploaded.
- The release job is the only job with permission to write to the repository (`contents: write`).
- Input is validated and errors are handled, so wrong input does not crash the program or damage data.

## Common deployment problems
| Problem | Cause | Solution |
|---------|-------|----------|
| `java` is not recognized | Java is not installed or not in PATH | Install JDK 17+ and set PATH |
| `Unsupported class file version` | Java on the computer is older than 17 | Install JDK 17 or newer |
| Error: Saved data files are damaged | A data file was edited by hand | Fix the file or restore a backup |
| Release job did not run | Tag does not start with `v` or tests failed | Check the Actions tab, use tags like `v1.0.0` |
| `Permission denied` for logs or data | Folder is read-only | Set `LIBRARY_DATA_DIR` and `LIBRARY_LOG_DIR` to a writable folder |
