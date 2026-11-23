import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
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
        return sc.nextLine();
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
        System.out.println("0. Exit");
    }

    public static void main(String[] args) {
        LibraryService library;
        try {
            library = new LibraryService(new DataStore("data"));
        } catch (LibraryException e) {
            System.out.println("Error: " + e.getMessage());
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
                    case 0:
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (LibraryException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (choice != 0);
    }
}
