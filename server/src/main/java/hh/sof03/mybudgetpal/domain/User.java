package hh.sof03.mybudgetpal.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.Set;

@Entity(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @NotBlank(message = "Username is mandatory")
  @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
  @Column(name = "username", nullable = false, unique = true)
  private String username;

  @NotBlank(message = "First name is mandatory")
  @Pattern(regexp = "^[a-zA-Z]+$", message = "First name can only contain letters")
  @Column(name = "first_name", nullable = false)
  private String firstName;

  @NotBlank(message = "Last name is mandatory")
  @Pattern(regexp = "^[a-zA-Z]+$", message = "Last name can only contain letters")
  @Column(name = "last_name", nullable = false)
  private String lastName;

  @NotBlank(message = "Email is mandatory")
  @Pattern(regexp = ".+@.+\\..+", message = "Email address must be valid")
  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @NotBlank(message = "Password is mandatory")
  @Column(name = "password", nullable = false)
  private String passwordHash;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "roles", nullable = false)
  private Set<String> roles;

  @Column(name = "enabled")
  private boolean enabled;

  private String passwordResetToken;

  private LocalDateTime passwordResetTokenExpiryDate;

  private String emailVerificationToken;

  private LocalDateTime emailVerifcationTokenExpiryDate;

  public User() {
  }

  public User(String username, String firstName, String lastName, String email, String passwordHash) {
    super();
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.passwordHash = passwordHash;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getPasswordResetToken() {
    return passwordResetToken;
  }

  public void setPasswordResetToken(String passwordResetToken) {
    this.passwordResetToken = passwordResetToken;
  }

  public LocalDateTime getPasswordResetTokenExpiryDate() {
    return passwordResetTokenExpiryDate;
  }

  public void setPasswordResetTokenExpiryDate(LocalDateTime passwordResetTokenExpiryDate) {
    this.passwordResetTokenExpiryDate = passwordResetTokenExpiryDate;
  }

  public String getEmailVerificationToken() {
    return emailVerificationToken;
  }

  public void setEmailVerificationToken(String emailVerificationToken) {
    this.emailVerificationToken = emailVerificationToken;
  }

  public LocalDateTime getEmailVerificationTokenExpiryDate() {
    return emailVerifcationTokenExpiryDate;
  }

  public void setEmailVerificationTokenExpiryDate(LocalDateTime emailVerifcationTokenExpiryDate) {
    this.emailVerifcationTokenExpiryDate = emailVerifcationTokenExpiryDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;

    return id != null ? id.equals(user.id) : user.id == null;
  }

}