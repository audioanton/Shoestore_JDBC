import repository.*;

import java.sql.SQLOutput;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class CustomerApp {
    private Repository repo;
    private Scanner scan;
    private int userID;

    public CustomerApp(Repository repo) {
        userID = 0;
        scan = new Scanner(System.in);
        this.repo = repo;
    }

    public void authenticateUser() {
        List<Customer> customers = repo.getCustomers();
        /*customers.forEach(customer -> {
            System.out.printf("%s %s\n", customer.firstName(), customer.lastName());
        });*/

        String fName = "";
        String lName = "";
        String password = "";

        while (true) {
            try {
                System.out.println("Enter firstname:");
                fName = scan.nextLine().trim();
                System.out.println("Enter lastname:");
                lName = scan.nextLine().trim();
                System.out.println("Enter password:");
                password = scan.nextLine().trim();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            Collections.sort(customers);
            int index = Collections.binarySearch(customers, new Customer(0, fName, lName, 0, "", ""));
            if (index >= 0) {
                if (password.equals(customers.get(index).password())) {
                    userID = customers.get(index).ID();
                    System.out.printf("\nCustomer authenticated: %s %s\n", customers.get(index).firstName(), customers.get(index).lastName());
                    break;
                }
            }
            else
                System.out.println("\nInvalid password or user.\n");
        }
    }

    public void showInventory() {
        List<Shoe> shoes = repo.getShoes();
        Collections.sort(shoes);

        System.out.println("All Shoe Models:");
        for (Shoe s : shoes) {
            System.out.printf("%s  %s  Price: %.2f\n", s.Brand(), s.Model(), s.Price());
        }
    }

    public void addToCart() {
        int inventory = getInventoryID();

        System.out.println("How many pairs do you wish to add?");
        int quantity = scan.nextInt();
        scan.nextLine();

        System.out.println();
        System.out.println(repo.callAddToCart(userID, inventory, quantity));
    }

    private int getInventoryID() {
        List<Inventory> inventory = repo.getInventory();
        Collections.sort(inventory);
        List<Size> sizes = repo.getSizes();
        List<Color> colors = repo.getColors();

        while (true) {
            int shoeID = getShoeID();

            System.out.println();
            System.out.println("All colors: ");
            colors.forEach(c -> {
                System.out.println(c.name());
            });
            System.out.println();
            int colorID = getColorID(colors);

            System.out.println("Available sizes: ");
            sizes.forEach(size -> {
                int index = Collections.binarySearch(inventory, new Inventory(0, shoeID, colorID, size.ID(), 0));
                if (index >= 0)
                    System.out.printf("%d - (%d in stock)\n", size.ShoeSize(), inventory.get(index).Quantity());
            });
            System.out.println();
            int sizeID = getSizeID(sizes);

            int index = Collections.binarySearch(inventory, new Inventory(0, shoeID, colorID, sizeID, 0));

            if (index >= 0) {
                return inventory.get(index).ID();
            } else {
                System.out.println("\nNo such product in stock. Please retry.\n");
            }
        }
    }

    public int getShoeID() {
        List<Shoe> shoes = repo.getShoes();
        String brand = "", model = "";

        while (true) {
            try {
                System.out.println("Enter brand:");
                brand = scan.nextLine().trim();
                System.out.println("Enter model:");
                model = scan.nextLine().trim();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            Collections.sort(shoes);
            int index = Collections.binarySearch(shoes, new Shoe(0, brand, model, 0));
            if (index >= 0) {
                System.out.println();
                System.out.printf("Selected: %s, %s, %.2f\n", shoes.get(index).Brand(), shoes.get(index).Model(), shoes.get(index).Price());
                return shoes.get(index).ID();
            }
            else
                System.out.println("Invalid brand or model.");
        }
    }

    public int getColorID(List<Color> colors) {

        String color = "";

        while (true) {
            try {
                System.out.println("Select color:");
                color = scan.nextLine().trim();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            Collections.sort(colors);
            int index = Collections.binarySearch(colors, new Color(0, color));
            if (index >= 0) {
                System.out.printf("\nSelected color: %s\n\n", colors.get(index).name());
                return colors.get(index).ID();
            } else
                System.out.println("\nInvalid color.\n");
        }
    }

    public int getSizeID(List<Size> sizes) {
        int size = 0;

        while (true) {
            try {
                System.out.println("Select size:");
                size = scan.nextInt();
                scan.nextLine();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            Collections.sort(sizes);
            int index = Collections.binarySearch(sizes, new Size(0, size));
            if (index >= 0) {
                System.out.printf("\nSelected size: %d\n\n", sizes.get(index).ShoeSize());
                return sizes.get(index).ID();
            } else
                System.out.println("Invalid size.");
        }
    }

    public void finalizePurchase() {
        String cost = showCurrentPurchaseCost();
        if (cost == null)
            System.out.println("No active order found.");
        else {
            System.out.println("Active order:\n" + cost);
            System.out.println("Pay order: enter 'p'\nCancel: enter 'c'");
            String selection = scan.nextLine().trim();
            String response = "";
            if (selection.equals("p"))
                response = repo.callFinalizePurchase(userID, true);
            else if (selection.equals("c"))
                response = repo.callFinalizePurchase(userID, false);
            else
                System.out.println("Invalid selection.");
            System.out.println(response);
        }
    }

    public String showCurrentPurchaseCost() {
        PurchaseCost cost = repo.showCurrentPurchaseCost(userID, false);
        if (cost == null)
            return null;
        else
            return String.format("%s - Cost: %s \n", cost.date(), cost.cost());
    }

    public void startApp() {
        while (true) {
            System.out.println();
            System.out.println("Select option:");
            System.out.println("1: exit program");
            System.out.println("2: add product to cart");
            System.out.println("3: approve purchase or delete order");
            System.out.println("4: show products");

            String selection = scan.nextLine().trim();
            System.out.println();

            switch (selection) {
                case "1": {
                    System.out.println("Exiting program.");
                    System.exit(0);
                    break;
                }
                case "2": addToCart();break;
                case "3": finalizePurchase();break;
                case "4": showInventory();break;
            }
        }
    }
}
