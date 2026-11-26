import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class Main {
    private static final String VERSION = "1.0.0";
    private static final Logger LOG = Logger.getLogger("library");
    private static final Scanner sc = new Scanner(System.in);

    /** Reads a setting from an environment variable, or uses a default value. */
    private static String setting(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /** Writes log messages to a file; files rotate after 1 MB and 3 old files are kept. */
    private static void setupLogging(String logFolder) {
        try {
            Files.createDirectories(Paths.get(logFolder));
            FileHandler handler = new FileHandler(logFolder + "/library-%g.log", 1_000_000, 3, true);
            handler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord r) {
                    return String.format("%1$tF %1$tT [%2$s] %3$s%n", new Date(r.getMillis()),
                            r.getLevel(), r.getMessage());
                }
            });
            LOG.setUseParentHandlers(false);
            LOG.setLevel(Level.INFO);
            LOG.addHandler(handler);
        } catch (IOException e) {
            System.out.println("Warning: logging is disabled (" + e.getMessage() + ")");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!sc.hasNextLine()) {
                return 0; // input closed, so exit safely
            }
            String line = sc.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private static String readText(String prompt) {
        System.out.print(prompt);
        return sc.hasNextLine() ? sc.nextLine() : "";
    }

    private static void printMenu() {
        System.out.println("\n===== Library Management System =====");
        System.out.println("1. Show all books");
        System.out.println("2. Add a book");
        System.out.println("3. Add a member");
        System.out.println("4. Show all members");
        System.out.println("5. Issue a book");
        System.out.println("6. Return a book");
        System.out.println("7. Search books");
        System.out.println("8. Show issued books");
        System.out.println("9. Backup data");
        System.out.println("0. Exit");
    }

    public static void main(String[] args) {
        String dataFolder = setting("LIBRARY_DATA_DIR", "data");
        setupLogging(setting("LIBRARY_LOG_DIR", "logs"));
        System.out.println("Library Management System v" + VERSION);
        LOG.info("Application started (version " + VERSION + ", data folder: " + dataFolder + ")");

        LibraryService library;
        try {
            library = new LibraryService(new DataStore(dataFolder));
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
            LOG.severe("Application could not start: " + e.getMessage());
            return;
        }

        int choice;
        do {
            printMenu();
            choice = readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        if (library.getBooks().isEmpty()) System.out.println("No books yet.");
                        for (Book b : library.getBooks()) System.out.println(b);
                        break;
                    case 2:
                        Book nb = library.addBook(readText("Title: "), readText("Author: "));
                        System.out.println("Book added with id " + nb.getId());
                        break;
                    case 3:
                        Member nm = library.addMember(readText("Member name: "));
                        System.out.println("Member added with id " + nm.getId());
                        break;
                    case 4:
                        if (library.getMembers().isEmpty()) System.out.println("No members yet.");
                        for (Member m : library.getMembers()) System.out.println(m);
                        break;
                    case 5:
                        int bid = readInt("Book id: ");
                        int mid = readInt("Member id: ");
                        library.issueBook(bid, mid, LocalDate.now());
                        System.out.println("Book issued. Return within " + LibraryService.LOAN_DAYS
                                + " days to avoid a fine.");
                        break;
                    case 6:
                        int fine = library.returnBook(readInt("Book id: "), LocalDate.now());
                        System.out.println(fine == 0 ? "Book returned. No fine."
                                : "Book returned late. Fine: Rs " + fine);
                        break;
                    case 7:
                        List<Book> found = library.searchBooks(readText("Search keyword: "));
                        if (found.isEmpty()) System.out.println("No matching books.");
                        for (Book b : found) System.out.println(b);
                        break;
                    case 8:
                        if (library.getActiveRecords().isEmpty()) System.out.println("No books are issued.");
                        for (IssueRecord r : library.getActiveRecords()) {
                            System.out.println("Book: " + library.findBook(r.getBookId()).getTitle()
                                    + " | Member: " + library.findMember(r.getMemberId()).getName()
                                    + " | Issued on: " + r.getIssueDate());
                        }
                        break;
                    case 9:
                        System.out.println("Backup saved in: " + library.backupData());
                        break;
                    case 0:
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
                LOG.warning("Request rejected: " + e.getMessage());
            }
        } while (choice != 0);
        LOG.info("Application stopped");
    }
}
