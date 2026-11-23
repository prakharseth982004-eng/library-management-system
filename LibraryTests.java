import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

/** Simple test runner. Run with: java LibraryTests */
public class LibraryTests {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }


    public static void main(String[] args) throws Exception {
        Path temp = Files.createTempDirectory("library-test");
        LibraryService lib = new LibraryService(new DataStore(temp.toString()));
        LocalDate day = LocalDate.of(2026, 1, 1);

        Book b1 = lib.addBook("Java Basics", "James Gosling");
        lib.addBook("Clean Code", "Robert Martin");
        Member m1 = lib.addMember("Prakhar");
        check("Book gets id 1", b1.getId() == 1);
        check("Two books stored", lib.getBooks().size() == 2);

        lib.issueBook(1, m1.getId(), day);
        check("Issued book becomes unavailable", !lib.findBook(1).isAvailable());

        try {
            lib.issueBook(1, m1.getId(), day);
            check("Cannot issue an already issued book", false);
        } catch (LibraryException e) {
            check("Cannot issue an already issued book", true);
        }

        try {
            lib.findBook(99);
            check("Unknown book id gives error", false);
        } catch (LibraryException e) {
            check("Unknown book id gives error", true);
        }

        check("On-time return has no fine", lib.returnBook(1, day.plusDays(5)) == 0);
        check("Returned book is available again", lib.findBook(1).isAvailable());

        lib.issueBook(1, m1.getId(), day);
        check("Late return fine is correct (3 late days x Rs 2)", lib.returnBook(1, day.plusDays(10)) == 6);

        try {
            lib.returnBook(2, day);
            check("Returning a book that was not issued gives error", false);
        } catch (LibraryException e) {
            check("Returning a book that was not issued gives error", true);
        }

        lib.addBook("Book 3", "A");
        lib.addBook("Book 4", "B");
        lib.issueBook(1, m1.getId(), day);
        lib.issueBook(2, m1.getId(), day);
        lib.issueBook(3, m1.getId(), day);
        try {
            lib.issueBook(4, m1.getId(), day);
            check("Member cannot borrow more than 3 books", false);
        } catch (LibraryException e) {
            check("Member cannot borrow more than 3 books", true);
        }

        check("Search finds a book by keyword", lib.searchBooks("clean").size() == 1);

        try {
            lib.addBook("", "Someone");
            check("Empty title is rejected", false);
        } catch (LibraryException e) {
            check("Empty title is rejected", true);
        }

        LibraryService reloaded = new LibraryService(new DataStore(temp.toString()));
        check("Data is saved and loaded again", reloaded.getBooks().size() == 4
                && reloaded.getMembers().size() == 1 && reloaded.getActiveRecords().size() == 3);

        System.out.println("\nTotal: " + (passed + failed) + " | Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) System.exit(1);
    }
}
