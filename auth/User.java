package auth;

public abstract class User implements Authenticable {
    protected String id;
    protected String fullName;
    protected String phoneNumber;
    private String username;
    private String password;

    private String accountNumber;
    private String BranchAffiliation;
    private String branchNumber;
    String EmployeeNumber;
    String role;


    public User(String fullName, String id, String username, String password, String phoneNumber)
    {
        this.fullName = fullName;
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.username = username;
        this.password = password; // In a real-world scenario, this should be a hashed version of the password

    }
    @Override
    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
        // Implement basic authentication logic here
    }

    // Getters
    public String getFullName() {
        return fullName;
    }

    public String getIdNumber() {
        return id;
    }

    public String getPhone() {
        return phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    // We typically don't provide a direct getter for the password due to security reasons.
    // If necessary, you could provide a method that verifies if a password matches.

    // Setters
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setIdNumber(String newId) {
        this.id = newId;
    }

    public void setPhone(String phone) {
        this.phoneNumber = phone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // Similar to the getter, we set the password indirectly to ensure security.
    // For instance, the setPassword can also enforce certain password policies.
    public void setPassword(String newPassword) {
        password = newPassword;
    }

    // Helper methods
//    private boolean isValidPassword(String password) {
//        // For simplicity, let's just check for password length.
//        // In real applications, you would have more rigorous checks (e.g., patterns, special chars, etc.).
//        return password.length() >= 8;
//    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        // Change password logic as earlier
        if(this.password.equals(oldPassword)) {
            this.password = newPassword;
        } else {
            // Throw an error or notify the user
        }
    }


    public String getPassword() {
        return password;
    }
}
