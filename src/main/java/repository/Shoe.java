package repository;

public record Shoe(int ID, String Brand, String Model, double Price) implements Comparable<Shoe> {
    @Override
    public int compareTo(Shoe o) {
        String brandModel = Brand + " " + Model;
        String oBrandModel = o.Brand + " " + o.Model;
        return brandModel.compareTo(oBrandModel);
    }
}
