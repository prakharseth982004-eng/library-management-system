import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** Saves and loads library data in simple text files so data is not lost when the program closes. */
public class DataStore {
    private final Path dir;

    public DataStore(String folderName) {
        this.dir = Paths.get(folderName);
    }

    private List<String> readLines(String file) throws IOException {
        Path path = dir.resolve(file);
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(path);
    }

    private void writeLines(String file, List<String> lines) throws IOException {
        Files.createDirectories(dir);
        Files.write(dir.resolve(file), lines);
    }

    private void appendLine(String file, String line) throws IOException {
        Files.createDirectories(dir);
        Files.write(dir.resolve(file), java.util.Collections.singletonList(line),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    /** Adds one book at the end of the file without rewriting the whole file. */
    public void appendBook(Book book) throws IOException {
        appendLine("books.txt", book.toLine());
    }

    /** Adds one member at the end of the file without rewriting the whole file. */
    public void appendMember(Member member) throws IOException {
        appendLine("members.txt", member.toLine());
    }

    /** Copies all data files into a new time-stamped folder inside the data folder. */
    public Path backup() throws IOException {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path target = dir.resolve("backups").resolve(stamp);
        Files.createDirectories(target);
        for (String file : new String[] {"books.txt", "members.txt", "records.txt"}) {
            Path source = dir.resolve(file);
            if (Files.exists(source)) {
                Files.copy(source, target.resolve(file), StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return target;
    }

    public List<Book> loadBooks() throws IOException {
        List<Book> list = new ArrayList<>();
        for (String line : readLines("books.txt")) {
            if (!line.isBlank()) list.add(Book.fromLine(line));
        }
        return list;
    }

    public List<Member> loadMembers() throws IOException {
        List<Member> list = new ArrayList<>();
        for (String line : readLines("members.txt")) {
            if (!line.isBlank()) list.add(Member.fromLine(line));
        }
        return list;
    }

    public List<IssueRecord> loadRecords() throws IOException {
        List<IssueRecord> list = new ArrayList<>();
        for (String line : readLines("records.txt")) {
            if (!line.isBlank()) list.add(IssueRecord.fromLine(line));
        }
        return list;
    }

    public void saveBooks(List<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Book b : books) lines.add(b.toLine());
        writeLines("books.txt", lines);
    }

    public void saveMembers(List<Member> members) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Member m : members) lines.add(m.toLine());
        writeLines("members.txt", lines);
    }

    public void saveRecords(List<IssueRecord> records) throws IOException {
        List<String> lines = new ArrayList<>();
        for (IssueRecord r : records) lines.add(r.toLine());
        writeLines("records.txt", lines);
    }
}
