import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

/** Simple test runner (no external library needed). Run with: java LibraryTests */
public class LibraryTests {
    private static int passed = 0;
    private static int failed = 0;

    private interface Action {
        void run() throws Exception;
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    /** Passes only if the action throws a LibraryException. */
    private static void expectError(String name, Action action) {
        try {
            action.run();
            check(name, false);
        } catch (LibraryException e) {
            check(name, true);
        } catch (Exception e) {
            System.out.println("   (unexpected " + e.getClass().getSimpleName() + ")");
            check(name, false);
        }
    }

    private static LibraryService newLibrary(Path dir) throws Exception {
        return new LibraryService(new DataStore(dir.toString()));
    }

    public static void main(String[] args) throws Exception {
        LocalDate day = LocalDate.of(2026, 1, 1);

        System.out.println("=== UNIT TESTS ===");
        Path dir1 = Files.createTempDirectory("lib-unit");
        LibraryService lib = newLibrary(dir1);
        Book b1 = lib.addBook("Java Basics", "James Gosling");
        Book b2 = lib.addBook("Clean Code", "Robert Martin");
        Member m1 = lib.addMember("Prakhar");
        check("U1 first book gets id 1 and next gets id 2", b1.getId() == 1 && b2.getId() == 2);
        check("U2 new book is available", b1.isAvailable());
        Book copy = Book.fromLine(b1.toLine());
        check("U3 Book converts to a line and back", copy.getId() == 1
                && copy.getTitle().equals("Java Basics") && copy.isAvailable());
        check("U4 Member converts to a line and back", Member.fromLine(m1.toLine()).getName().equals("Prakhar"));
        IssueRecord open = IssueRecord.fromLine(new IssueRecord(1, 1, day, null).toLine());
        IssueRecord closed = IssueRecord.fromLine(new IssueRecord(1, 1, day, day.plusDays(2)).toLine());
        check("U5 IssueRecord converts to a line and back", open.isActive() && !closed.isActive()
                && closed.getReturnDate().equals(day.plusDays(2)));
        expectError("U6 empty title is rejected", () -> lib.addBook("", "Someone"));
        expectError("U7 blank member name is rejected", () -> lib.addMember("   "));
        Book clean = lib.addBook("A|B", "C|D");
        check("U8 the | character is removed from text", clean.getTitle().equals("A B") && clean.getAuthor().equals("C D"));
        check("U9 search by title is not case sensitive", lib.searchBooks("CLEAN").size() == 1);
        check("U10 search by author works", lib.searchBooks("gosling").size() == 1);
        check("U11 search with no match gives an empty list", lib.searchBooks("zzz").isEmpty());

        System.out.println("\n=== INTEGRATION TESTS ===");
        Path dir2 = Files.createTempDirectory("lib-int");
        LibraryService s = newLibrary(dir2);
        s.addBook("Java Basics", "James Gosling");
        s.addBook("Clean Code", "Robert Martin");
        s.addBook("Book 3", "A");
        s.addBook("Book 4", "B");
        Member m = s.addMember("Prakhar");
        s.issueBook(1, m.getId(), day);
        check("I1 issued book becomes unavailable", !s.findBook(1).isAvailable());
        expectError("I2 issuing an already issued book is rejected", () -> s.issueBook(1, m.getId(), day));
        expectError("I3 unknown book id is rejected", () -> s.findBook(99));
        expectError("I4 unknown member id is rejected", () -> s.issueBook(2, 99, day));
        check("I5 on-time return has no fine and book is available",
                s.returnBook(1, day.plusDays(5)) == 0 && s.findBook(1).isAvailable());
        s.issueBook(1, m.getId(), day);
        check("I6 late return fine is correct (3 late days x Rs 2)", s.returnBook(1, day.plusDays(10)) == 6);
        expectError("I7 returning a book that was not issued is rejected", () -> s.returnBook(2, day));
        s.issueBook(1, m.getId(), day);
        s.issueBook(2, m.getId(), day);
        s.issueBook(3, m.getId(), day);
        expectError("I8 member cannot borrow more than 3 books", () -> s.issueBook(4, m.getId(), day));
        s.returnBook(3, day);
        s.issueBook(4, m.getId(), day);
        check("I9 member can borrow again after returning a book", !s.findBook(4).isAvailable());
        LibraryService again = newLibrary(dir2);
        check("I10 books, members and records are saved and loaded again",
                again.getBooks().size() == 4 && again.getMembers().size() == 1
                        && again.getActiveRecords().size() == 3);
        check("I11 issued status is kept after reload", !again.findBook(1).isAvailable() && again.findBook(3).isAvailable());
        check("I12 ids continue correctly after reload", again.addBook("New", "X").getId() == 5);

        System.out.println("\n=== EDGE CASE AND SYSTEM TESTS ===");
        Path dir3 = Files.createTempDirectory("lib-edge");
        LibraryService e = newLibrary(dir3);
        e.addBook("Edge Book", "Tester");
        Member em = e.addMember("Edge Member");
        e.issueBook(1, em.getId(), day);
        check("E1 returning on exactly day 7 has no fine", e.returnBook(1, day.plusDays(7)) == 0);
        e.issueBook(1, em.getId(), day);
        check("E2 returning on day 8 has a fine of Rs 2", e.returnBook(1, day.plusDays(8)) == 2);
        e.issueBook(1, em.getId(), day);
        expectError("E3 return date before issue date is rejected", () -> e.returnBook(1, day.minusDays(1)));
        check("E3b book stays issued after a rejected return", !e.findBook(1).isAvailable());

        Path bad = Files.createTempDirectory("lib-bad");
        Files.write(bad.resolve("books.txt"), Arrays.asList("1|Java|Gosling|true", "this line is broken"));
        expectError("E4 damaged data file gives a clear error instead of a crash", () -> newLibrary(bad));

        Path pipe = Files.createTempDirectory("lib-pipe");
        LibraryService ps = newLibrary(pipe);
        ps.addBook("Title|With|Pipes", "Author");
        check("E5 title with | is saved and loaded correctly",
                newLibrary(pipe).getBooks().get(0).getTitle().equals("Title With Pipes"));

        System.out.println("\n=== OPERATIONS TESTS (BACKUP AND RESTORE) ===");
        Path ops = Files.createTempDirectory("lib-ops");
        LibraryService os = newLibrary(ops);
        os.addBook("Backup Book", "Tester");
        os.addMember("Backup Member");
        os.issueBook(1, 1, day);
        Path backupFolder = java.nio.file.Paths.get(os.backupData());
        check("D1 backup creates copies of all data files", Files.exists(backupFolder.resolve("books.txt"))
                && Files.exists(backupFolder.resolve("members.txt")) && Files.exists(backupFolder.resolve("records.txt")));
        os.addBook("Added after backup", "Tester");
        LibraryService restored = newLibrary(backupFolder);
        check("D2 a backup can be restored and has the data from backup time",
                restored.getBooks().size() == 1 && restored.getMembers().size() == 1
                        && restored.getActiveRecords().size() == 1);

        System.out.println("\n=== PERFORMANCE TEST ===");
        Path perf = Files.createTempDirectory("lib-perf");
        LibraryService ls = newLibrary(perf);
        long start = System.nanoTime();
        for (int i = 0; i < 2000; i++) {
            ls.addBook("Book " + i, "Author");
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("Adding 2000 books took " + ms + " ms");
        check("P1 adding 2000 books takes less than 3 seconds", ms < 3000);
        check("P2 all 2000 books are saved correctly", newLibrary(perf).getBooks().size() == 2000);

        System.out.println("\nTotal: " + (passed + failed) + " | Passed: " + passed + " | Failed: " + failed);
        if (failed > 0) System.exit(1);
    }
}
