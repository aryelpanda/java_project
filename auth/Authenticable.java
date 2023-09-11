package auth;//Handles authentication for users.txt.

public interface Authenticable {
    boolean authenticate(String username, String password);
    void changePassword(String oldPassword,String newPassword);
}

