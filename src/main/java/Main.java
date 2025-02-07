import repository.Repository;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            Repository repo = new Repository();
            CustomerApp app = new CustomerApp(repo);
            app.authenticateUser();
            app.startApp();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
