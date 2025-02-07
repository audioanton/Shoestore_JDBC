package repository;

public record Inventory(int ID, int ShoeID, int ColorID, int SizeID, int Quantity) implements Comparable<Inventory> {
    @Override
    public int compareTo(Inventory o) {
        String data = String.format("%d%d%d", ShoeID, ColorID, SizeID);
        String oData = String.format("%d%d%d", o.ShoeID, o.ColorID, o.SizeID);
        return data.compareTo(oData);
    }
}
