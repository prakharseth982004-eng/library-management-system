import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/** All the business rules of the library live in this class. */
public class LibraryService {
    public static final int LOAN_DAYS = 7;
    public static final int FINE_PER_DAY = 2;
    public static final int MAX_BOOKS_PER_MEMBER = 3;

    private final DataStore store;
    private final List<Book> books;
    private final List<Member> members;
    private final List<IssueRecord> records;

    public LibraryService(DataStore store) throws LibraryException {
        this.store = store;
        try {
            this.books = store.loadBooks();
            this.members = store.loadMembers();
            this.records = store.loadRecords();
        } catch (IOException e) {
            throw new LibraryException("Could not load saved data: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new LibraryException("Saved data files are damaged or in a wrong format. "
                    + "Please fix or delete the data folder.");
        }
    }

    private String clean(String text) {
        return text == null ? "" : text.replace("|", " ").trim();
    }

    private void saveBooks() throws LibraryException {
        try {
            store.saveBooks(books);
        } catch (IOException e) {
            throw new LibraryException("Could not save data: " + e.getMessage());
        }
    }

    private void saveRecords() throws LibraryException {
        try {
            store.saveRecords(records);
        } catch (IOException e) {
            throw new LibraryException("Could not save data: " + e.getMessage());
        }
    }

    public Book addBook(String title, String author) throws LibraryException {
        title = clean(title);
        author = clean(author);
        if (title.isEmpty() || author.isEmpty()) {
            throw new LibraryException("Title and author cannot be empty.");
        }
        int id = books.isEmpty() ? 1 : books.get(books.size() - 1).getId() + 1;
        Book book = new Book(id, title, author, true);
        books.add(book);
        try {
            store.appendBook(book);
        } catch (IOException e) {
            throw new LibraryException("Could not save data: " + e.getMessage());
        }
        return book;
    }

    public Member addMember(String name) throws LibraryException {
        name = clean(name);
        if (name.isEmpty()) {
            throw new LibraryException("Member name cannot be empty.");
        }
        int id = members.isEmpty() ? 1 : members.get(members.size() - 1).getId() + 1;
        Member member = new Member(id, name);
        members.add(member);
        try {
            store.appendMember(member);
        } catch (IOException e) {
            throw new LibraryException("Could not save data: " + e.getMessage());
        }
        return member;
    }

    public Book findBook(int id) throws LibraryException {
        for (Book b : books) {
            if (b.getId() == id) return b;
        }
        throw new LibraryException("No book found with id " + id);
    }

    public Member findMember(int id) throws LibraryException {
        for (Member m : members) {
            if (m.getId() == id) return m;
        }
        throw new LibraryException("No member found with id " + id);
    }

    private int activeCount(int memberId) {
        int count = 0;
        for (IssueRecord r : records) {
            if (r.isActive() && r.getMemberId() == memberId) count++;
        }
        return count;
    }

    public void issueBook(int bookId, int memberId, LocalDate date) throws LibraryException {
        Book book = findBook(bookId);
        Member member = findMember(memberId);
        if (!book.isAvailable()) {
            throw new LibraryException("This book is already issued.");
        }
        if (activeCount(memberId) >= MAX_BOOKS_PER_MEMBER) {
            throw new LibraryException(member.getName() + " already has "
                    + MAX_BOOKS_PER_MEMBER + " books. Return one first.");
        }
        book.setAvailable(false);
        records.add(new IssueRecord(bookId, memberId, date, null));
        saveBooks();
        saveRecords();
    }

    /** Returns the fine to pay (0 if the book is returned on time). */
    public int returnBook(int bookId, LocalDate date) throws LibraryException {
        Book book = findBook(bookId);
        for (IssueRecord r : records) {
            if (r.isActive() && r.getBookId() == bookId) {
                if (date.isBefore(r.getIssueDate())) {
                    throw new LibraryException("Return date cannot be before the issue date ("
                            + r.getIssueDate() + ").");
                }
                r.setReturnDate(date);
                book.setAvailable(true);
                long daysKept = ChronoUnit.DAYS.between(r.getIssueDate(), date);
                long lateDays = Math.max(0, daysKept - LOAN_DAYS);
                saveBooks();
                saveRecords();
                return (int) (lateDays * FINE_PER_DAY);
            }
        }
        throw new LibraryException("This book is not issued.");
    }

    public List<Book> searchBooks(String keyword) {
        List<Book> result = new ArrayList<>();
        String k = keyword.toLowerCase().trim();
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(k) || b.getAuthor().toLowerCase().contains(k)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Book> getBooks() { return books; }
    public List<Member> getMembers() { return members; }

    public List<IssueRecord> getActiveRecords() {
        List<IssueRecord> active = new ArrayList<>();
        for (IssueRecord r : records) {
            if (r.isActive()) active.add(r);
        }
        return active;
    }
}
