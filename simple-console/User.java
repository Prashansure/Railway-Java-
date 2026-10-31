/** Represents one person who can log in to the railway system. */
public class User {

    private final String username;
    private final String password;
    private final String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public boolean matchesPassword(String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
