package repository;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Repository {
    private Properties properties;
    private String propertyLocation = "src/main/java/settings.properties";

    public Repository() throws IOException {
        properties = new Properties();
        properties.load(new FileInputStream(propertyLocation));
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("ConnectionString"),
                properties.getProperty("Username"),
                properties.getProperty("SecretPassword")
        );
    }

    public List<Inventory> getInventory() {
        List<Inventory> inventory = new ArrayList<>();

        try (Connection con = getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from Inventory;")
        ) {
            while (rs.next()) {
                int tempID = rs.getInt("ID");
                int tempShoe = rs.getInt("ShoeID");
                int tempColor = rs.getInt("ColorID");
                int tempSize = rs.getInt("SizeID");
                int tempQuantity = rs.getInt("Quantity");
                inventory.add(new Inventory(tempID, tempShoe, tempColor, tempSize, tempQuantity));
            }

            return inventory;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Shoe> getShoes() {
        List<Shoe> shoes = new ArrayList<>();

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from Shoe;")
        ) {
            while (rs.next()) {
                int tempID = rs.getInt("ID");
                String tempBrand = rs.getString("Brand");
                String tempModel = rs.getString("Model");
                double tempPrice = rs.getDouble("Price");
                shoes.add(new Shoe(tempID, tempBrand, tempModel, tempPrice));
            }

            return shoes;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Customer> getCustomers() {
        List<Customer> customers = new ArrayList<>();

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from Customer;")
        ) {
            while (rs.next()) {
                int tempID = rs.getInt("ID");
                String fName = rs.getString("Firstname");
                String lName = rs.getString("Lastname");
                int tempCity = rs.getInt("CityID");
                String address = rs.getString("Address");
                String password = rs.getString("Password");
                customers.add(new Customer(tempID, fName, lName, tempCity, address, password));
            }
            return customers;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Color> getColors() {
        List<Color> colors = new ArrayList<>();

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from Color;")
        ) {
            while (rs.next()) {
                int tempID = rs.getInt("ID");
                String tempName = rs.getString("Name");
                colors.add(new Color(tempID, tempName));
            }
            return colors;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Size> getSizes() {
        List<Size> sizes = new ArrayList<>();

        try (Connection con = getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from Size;")
        ) {
            while (rs.next()) {
                int tempID = rs.getInt("ID");
                int tempSize = rs.getInt("ShoeSize");
                sizes.add(new Size(tempID, tempSize));
            }
            return sizes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String callAddToCart(int customer, int inventory, int quantity) {
        try (Connection con = getConnection();
            CallableStatement stmt = con.prepareCall(
                "call addtocart(?, ?, ?, ?);"
        )) {
            stmt.setInt(1, customer);
            stmt.setObject(2, null);
            stmt.setInt(3, inventory);
            stmt.setInt(4, quantity);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                if (rs.getString(1).equals("Error"))
                    return rs.getString(3);
                else
                    return rs.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public PurchaseCost showCurrentPurchaseCost(int customer, boolean paid) {
        try (Connection con = getConnection();
            PreparedStatement stmt = con.prepareStatement(
                "select * from Purchase_Cost where CustomerID = ? and Paid = ?;"
        )) {
            stmt.setInt(1, customer);
            stmt.setBoolean(2, paid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String date = rs.getString("Purchase_Date");
                String cost = rs.getString("Cost");
                return new PurchaseCost(date, cost);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String callFinalizePurchase(int customer, boolean pay) {
        try (Connection con = getConnection();
             CallableStatement stmt = con.prepareCall(
                "call finalizepurchase(?, ?, ?, ?);"
        )) {
            stmt.setInt(1, customer);
            stmt.setObject(2, null);
            stmt.setBoolean(3, pay);
            stmt.setString(4, "default");
            stmt.registerOutParameter(4, Types.VARCHAR);
            stmt.executeQuery();
            return stmt.getString(4);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
}
