package se.duggals.dfrm.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * User entity för DFRM-systemet
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Förnamn är obligatoriskt")
    @Size(max = 100, message = "Förnamn får inte vara längre än 100 tecken")
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank(message = "Efternamn är obligatoriskt")
    @Size(max = 100, message = "Efternamn får inte vara längre än 100 tecken")
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @NotBlank(message = "E-post är obligatoriskt")
    @Email(message = "Ogiltig e-postadress")
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotNull(message = "Roll är obligatoriskt")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;
    
    @NotBlank(message = "Språk är obligatoriskt")
    @Size(max = 10, message = "Språkkod får inte vara längre än 10 tecken")
    @Column(name = "preferred_language", nullable = false)
    private String preferredLanguage = "sv";
    
    @NotNull(message = "Aktiv status är obligatoriskt")
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @Column(columnDefinition = "jsonb")
    private String permissions;
    
    @Size(max = 20, message = "Telefonnummer får inte vara längre än 20 tecken")
    private String phone;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // JPA lifecycle hooks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructors
    public User() {
        // Tom konstruktor för JPA
    }
    
    public User(String firstName, String lastName, String email, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }
    
    public User(String firstName, String lastName, String email, UserRole role, 
                String preferredLanguage, Boolean active, String permissions, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.preferredLanguage = preferredLanguage;
        this.active = active;
        this.permissions = permissions;
        this.phone = phone;
    }
    
    // Getters och Setters (manuella, ingen Lombok)
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public String getPreferredLanguage() {
        return preferredLanguage;
    }
    
    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public String getPermissions() {
        return permissions;
    }
    
    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Hjälpmetoder
    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @JsonIgnore
    public boolean hasPermission(String permission) {
        if (role == UserRole.SUPERADMIN) {
            return true;
        }
        return permissions != null && permissions.contains(permission);
    }
    
    @JsonIgnore
    public boolean hasRole(UserRole role) {
        return this.role == role;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
    
    /**
     * User roles enum
     */
    public enum UserRole {
        USER,
        ADMIN,
        SUPERADMIN
    }
} 