package repository;

public record Color(int ID, String name) implements Comparable<Color> {
    @Override
    public int compareTo(Color o) {
        return name.compareTo(o.name);
    }
}
