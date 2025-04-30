package edu.connexion3a41.App;

public class User {
    private int id;
    private String name;
    private String surname;
    private String email;
    private String username;
    private String cin;
    private String password;
    private String role;
    private boolean isMfaEnabled;
    private String profilePicture; // New field

    // Constructor
    public User(int id, String name, String surname, String email, String username, String cin, String password, String role, boolean isMfaEnabled) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.username = username;
        this.cin = cin;
        this.password = password;
        this.role = role;
        this.isMfaEnabled = isMfaEnabled;
        this.profilePicture = null; // Default to null
    }

    // Constructor with profilePicture
    public User(int id, String name, String surname, String email, String username, String cin, String password, String role, boolean isMfaEnabled, String profilePicture) {
        this(id, name, surname, email, username, cin, password, role, isMfaEnabled);
        this.profilePicture = profilePicture;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isMfaEnabled() { return isMfaEnabled; }
    public void setMfaEnabled(boolean mfaEnabled) { isMfaEnabled = mfaEnabled; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}