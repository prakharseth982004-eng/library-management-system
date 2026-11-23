import java.time.LocalDate;

public class IssueRecord {
    private final int bookId;
    private final int memberId;
    private final LocalDate issueDate;
    private LocalDate returnDate; // null while the book is still issued

    public IssueRecord(int bookId, int memberId, LocalDate issueDate, LocalDate returnDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.returnDate = returnDate;
    }

    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public boolean isActive() { return returnDate == null; }

    public String toLine() {
        return bookId + "|" + memberId + "|" + issueDate + "|" + (returnDate == null ? "-" : returnDate);
    }

    public static IssueRecord fromLine(String line) {
        String[] p = line.split("\\|");
        LocalDate ret = p[3].equals("-") ? null : LocalDate.parse(p[3]);
        return new IssueRecord(Integer.parseInt(p[0]), Integer.parseInt(p[1]),
                LocalDate.parse(p[2]), ret);
    }
}
