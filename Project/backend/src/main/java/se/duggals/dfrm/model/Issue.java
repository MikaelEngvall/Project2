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
 * Issue entity för DFRM-systemet
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Entity
@Table(name = "issues")
public class Issue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Rapportörs namn är obligatoriskt")
    @Size(max = 255, message = "Rapportörs namn får inte vara längre än 255 tecken")
    @Column(name = "reporter_name")
    private String reporterName;
    
    @NotBlank(message = "E-post är obligatoriskt")
    @Email(message = "Ogiltig e-postadress")
    @Column(name = "reporter_email")
    private String reporterEmail;
    
    @Size(max = 20, message = "Telefonnummer får inte vara längre än 20 tecken")
    private String phone;
    
    @NotNull(message = "Lägenhet är obligatoriskt")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;
    
    @NotBlank(message = "Titel är obligatoriskt")
    @Size(max = 255, message = "Titel får inte vara längre än 255 tecken")
    @Column(name = "title", nullable = false)
    private String title;
    
    @NotBlank(message = "Beskrivning är obligatoriskt")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;
    
    @NotNull(message = "Prioritet är obligatoriskt")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssuePriority priority = IssuePriority.MEDIUM;
    
    @NotNull(message = "Status är obligatoriskt")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueStatus status = IssueStatus.NEW;
    
    @Column(name = "approved_date")
    private LocalDate approvedDate;
    
    @Column(name = "rejected_date")
    private LocalDate rejectedDate;
    
    @Column(name = "rejection_reason")
    private String rejectionReason;
    
    @Column(name = "email_sent")
    private Boolean emailSent = false;
    
    @Column(name = "email_sent_date")
    private LocalDateTime emailSentDate;
    
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
    public Issue() {
        // Tom konstruktor för JPA
    }
    
        public Issue(String reporterName, String reporterEmail, Apartment apartment, 
                 String title, String description) {
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.apartment = apartment;
        this.title = title;
        this.description = description;
    }

    public Issue(String reporterName, String reporterEmail, String phone,
                 Apartment apartment, String title, String description, IssuePriority priority) {
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.phone = phone;
        this.apartment = apartment;
        this.title = title;
        this.description = description;
        this.priority = priority;
    }
    
    // Getters och Setters (manuella, ingen Lombok)
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getReporterName() {
        return reporterName;
    }
    
    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
    
    public String getReporterEmail() {
        return reporterEmail;
    }
    
    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public IssuePriority getPriority() {
        return priority;
    }
    
    public void setPriority(IssuePriority priority) {
        this.priority = priority;
    }
    
    public IssueStatus getStatus() {
        return status;
    }
    
    public void setStatus(IssueStatus status) {
        this.status = status;
    }
    
    public LocalDate getApprovedDate() {
        return approvedDate;
    }
    
    public void setApprovedDate(LocalDate approvedDate) {
        this.approvedDate = approvedDate;
    }
    
    public LocalDate getRejectedDate() {
        return rejectedDate;
    }
    
    public void setRejectedDate(LocalDate rejectedDate) {
        this.rejectedDate = rejectedDate;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    public Boolean getEmailSent() {
        return emailSent;
    }
    
    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }
    
    public LocalDateTime getEmailSentDate() {
        return emailSentDate;
    }
    
    public void setEmailSentDate(LocalDateTime emailSentDate) {
        this.emailSentDate = emailSentDate;
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
        return reporterName;
    }
    
    @JsonIgnore
    public boolean isNew() {
        return status == IssueStatus.NEW;
    }
    
    @JsonIgnore
    public boolean isApproved() {
        return status == IssueStatus.APPROVED;
    }
    
    @JsonIgnore
    public boolean isRejected() {
        return status == IssueStatus.REJECTED;
    }
    
    @JsonIgnore
    public boolean isHighPriority() {
        return priority == IssuePriority.HIGH;
    }
    
    @JsonIgnore
    public boolean isUrgent() {
        return priority == IssuePriority.URGENT;
    }
    
    @JsonIgnore
    public String getApartmentAddress() {
        return apartment != null ? apartment.getDisplayName() : "Okänd adress";
    }
    
    @JsonIgnore
    public String getPriorityColor() {
        switch (priority) {
            case URGENT:
                return "red";
            case HIGH:
                return "orange";
            case MEDIUM:
                return "yellow";
            case LOW:
                return "green";
            default:
                return "gray";
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Issue issue = (Issue) o;
        return id != null && id.equals(issue.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Issue{" +
                "id=" + id +
                ", reporterName='" + reporterName + '\'' +
                ", title='" + title + '\'' +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
    
    /**
     * Issue priority enum
     */
    public enum IssuePriority {
        LOW,
        MEDIUM,
        HIGH,
        URGENT
    }
    
    /**
     * Issue status enum
     */
    public enum IssueStatus {
        NEW,
        APPROVED,
        REJECTED
    }
} 