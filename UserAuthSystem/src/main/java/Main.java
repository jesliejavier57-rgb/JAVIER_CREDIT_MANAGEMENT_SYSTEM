import dao.DatabaseConnection;
import ui.MainMenu;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initialize();   // creates app.db and users table if missing
        new MainMenu().start();
    }
}
