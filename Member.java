public class Member {
    private final int id;
    private final String name;

    public Member(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public String toLine() {
        return id + "|" + name;
    }

    public static Member fromLine(String line) {
        String[] p = line.split("\\|");
        return new Member(Integer.parseInt(p[0]), p[1]);
    }

    @Override
    public String toString() {
        return String.format("%-4d %s", id, name);
    }
}
