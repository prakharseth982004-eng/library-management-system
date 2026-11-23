public class Book {
    private final int id;
    private final String title;
    private final String author;
    private boolean available;

    public Book(int id, String title, String author, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = available;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String toLine() {
        return id + "|" + title + "|" + author + "|" + available;
    }

    public static Book fromLine(String line) {
        String[] p = line.split("\\|");
        return new Book(Integer.parseInt(p[0]), p[1], p[2], Boolean.parseBoolean(p[3]));
    }

    @Override
    public String toString() {
        return String.format("%-4d %-28s %-22s %s", id, title, author,
                available ? "Available" : "Issued");
    }
}
