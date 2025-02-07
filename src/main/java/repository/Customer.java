package repository;

public record Customer(int ID, String firstName, String lastName, int cityID, String address, String password) implements Comparable<Customer> {

    @Override
    public int compareTo(Customer o) {

        String fullname = firstName + " " + lastName;
        String otherFullname = o.firstName() + " " + o.lastName();

        return fullname.compareTo(otherFullname);
    }
}
