package repository;

public record Size(int ID, int ShoeSize) implements Comparable<Size> {
    @Override
    public int compareTo(Size o) {
        return ShoeSize - o.ShoeSize;
    }
}
