package se.duggals.dfrm.model;

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
import jakarta.validation.constraints.Size;

/**
 * Interest entity för DFRM-systemet
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Entity
@Table(name = "interests")
public class Interest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Namn är obligatoriskt")
    @Size(max = 255, message = "Namn får inte vara längre än 255 tecken")
    @Column(name = "name")
    private String name;
    
    @NotBlank(message = "E-post är obligatoriskt")
    @Email(message = "Ogiltig e-postadress")
    @Column(nullable = false)
    private String email;
    
    @Size(max = 20, message = "Telefonnummer får inte vara längre än 20 tecken")
    private String phone;
    
    @NotNull(message = "Lägenhet är obligatoriskt")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;
    
    @NotNull(message = "Status är obligatoriskt")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterestStatus status = InterestStatus.PENDING;
    
    @Column(name = "viewing_date")
    private LocalDate viewingDate;
    
    @Column(name = "viewing_time")
    private String viewingTime;
    
    @Column(name = "viewing_confirmed")
    private Boolean viewingConfirmed = false;
    
    @Column(name = "viewing_email_sent")
    private Boolean viewingEmailSent = false;
    
    @Column(name = "viewing_email_sent_date")
    private LocalDateTime viewingEmailSentDate;
    
    @Column(name = "notes")
    private String notes;
    
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
    public Interest() {
        // Tom konstruktor för JPA
    }
    
    public Interest(String name, String email, Apartment apartment) {
        this.name = name;
        this.email = email;
        this.apartment = apartment;
    }
    
    public Interest(String name, String email, String phone,
                   Apartment apartment, String notes) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.apartment = apartment;
        this.notes = notes;
    }
    
    // Getters och Setters (manuella, ingen Lombok)
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
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
    
    public Apartment getApartment() {
        return apartment;
    }
    
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }
    
    public InterestStatus getStatus() {
        return status;
    }
    
    public void setStatus(InterestStatus status) {
        this.status = status;
    }
    
    public LocalDate getViewingDate() {
        return viewingDate;
    }
    
    public void setViewingDate(LocalDate viewingDate) {
        this.viewingDate = viewingDate;
    }
    
    public String getViewingTime() {
        return viewingTime;
    }
    
    public void setViewingTime(String viewingTime) {
        this.viewingTime = viewingTime;
    }
    
    public Boolean getViewingConfirmed() {
        return viewingConfirmed;
    }
    
    public void setViewingConfirmed(Boolean viewingConfirmed) {
        this.viewingConfirmed = viewingConfirmed;
    }
    
    public Boolean getViewingEmailSent() {
        return viewingEmailSent;
    }
    
    public void setViewingEmailSent(Boolean viewingEmailSent) {
        this.viewingEmailSent = viewingEmailSent;
    }
    
    public LocalDateTime getViewingEmailSentDate() {
        return viewingEmailSentDate;
    }
    
    public void setViewingEmailSentDate(LocalDateTime viewingEmailSentDate) {
        this.viewingEmailSentDate = viewingEmailSentDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
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
        return name;
    }
    
    @JsonIgnore
    public boolean isPending() {
        return status == InterestStatus.PENDING;
    }
    
    @JsonIgnore
    public boolean isConfirmed() {
        return status == InterestStatus.CONFIRMED;
    }
    
    @JsonIgnore
    public boolean isRejected() {
        return status == InterestStatus.REJECTED;
    }
    
    @JsonIgnore
    public boolean hasViewingScheduled() {
        return viewingDate != null;
    }
    
    @JsonIgnore
    public String getApartmentAddress() {
        return apartment != null ? apartment.getDisplayName() : "Okänd adress";
    }
    
    @JsonIgnore
    public String getViewingDateTime() {
        if (viewingDate != null && viewingTime != null) {
            return viewingDate.toString() + " " + viewingTime;
        }
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interest interest = (Interest) o;
        return id != null && id.equals(interest.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Interest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                '}';
    }
    
    /**
     * Interest status enum
     */
    public enum InterestStatus {
        PENDING,
        CONFIRMED,
        REJECTED
    }
} 