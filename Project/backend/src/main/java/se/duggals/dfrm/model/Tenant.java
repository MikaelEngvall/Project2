package se.duggals.dfrm.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Tenant entity för DFRM-systemet
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Entity
@Table(name = "tenants")
public class Tenant {
    
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
    @Column(nullable = false)
    private String email;
    
    @Size(max = 20, message = "Telefonnummer får inte vara längre än 20 tecken")
    private String phone;
    
    @Size(max = 20, message = "Personnummer får inte vara längre än 20 tecken")
    @Column(name = "personal_number")
    private String personalNumber;
    
    @NotNull(message = "Lägenhet är obligatoriskt")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;
    
    @NotNull(message = "Flytt-in datum är obligatoriskt")
    @Column(name = "move_in_date", nullable = false)
    private LocalDate moveInDate;
    
    @Column(name = "move_out_date")
    private LocalDate moveOutDate;
    
    @Positive(message = "Månadsavgift måste vara positiv")
    @Column(name = "monthly_rent", precision = 10, scale = 2)
    private BigDecimal monthlyRent;
    
    @NotNull(message = "Status är obligatoriskt")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.ACTIVE;
    
    @Column(name = "termination_reason")
    private String terminationReason;
    
    @Column(name = "termination_date")
    private LocalDate terminationDate;
    
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
    public Tenant() {
        // Tom konstruktor för JPA
    }
    
    public Tenant(String firstName, String lastName, String email, Apartment apartment, LocalDate moveInDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.apartment = apartment;
        this.moveInDate = moveInDate;
    }
    
    public Tenant(String firstName, String lastName, String email, String phone, String personalNumber,
                  Apartment apartment, LocalDate moveInDate, BigDecimal monthlyRent) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.personalNumber = personalNumber;
        this.apartment = apartment;
        this.moveInDate = moveInDate;
        this.monthlyRent = monthlyRent;
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
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPersonalNumber() {
        return personalNumber;
    }
    
    public void setPersonalNumber(String personalNumber) {
        this.personalNumber = personalNumber;
    }
    
    public Apartment getApartment() {
        return apartment;
    }
    
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }
    
    public LocalDate getMoveInDate() {
        return moveInDate;
    }
    
    public void setMoveInDate(LocalDate moveInDate) {
        this.moveInDate = moveInDate;
    }
    
    public LocalDate getMoveOutDate() {
        return moveOutDate;
    }
    
    public void setMoveOutDate(LocalDate moveOutDate) {
        this.moveOutDate = moveOutDate;
    }
    
    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }
    
    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }
    
    public TenantStatus getStatus() {
        return status;
    }
    
    public void setStatus(TenantStatus status) {
        this.status = status;
    }
    
    public String getTerminationReason() {
        return terminationReason;
    }
    
    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }
    
    public LocalDate getTerminationDate() {
        return terminationDate;
    }
    
    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
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
    public boolean isActive() {
        return status == TenantStatus.ACTIVE;
    }
    
    @JsonIgnore
    public boolean isTerminated() {
        return status == TenantStatus.TERMINATED || status == TenantStatus.TERMINATED_NOT_MOVED_OUT;
    }
    
    @JsonIgnore
    public boolean hasMovedOut() {
        return moveOutDate != null;
    }
    
    @JsonIgnore
    public String getApartmentAddress() {
        return apartment != null ? apartment.getDisplayName() : "Okänd adress";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tenant tenant = (Tenant) o;
        return id != null && id.equals(tenant.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Tenant{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }
    
    /**
     * Tenant status enum
     */
    public enum TenantStatus {
        ACTIVE,
        TERMINATED,
        TERMINATED_NOT_MOVED_OUT
    }
} 