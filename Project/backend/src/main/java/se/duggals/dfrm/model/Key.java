package se.duggals.dfrm.model;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Key entity för DFRM-systemet
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Entity
@Table(name = "keys")
public class Key {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Serie är obligatoriskt")
    @Size(max = 50, message = "Serie får inte vara längre än 50 tecken")
    @Column(name = "series", nullable = false)
    private String series;
    
    @NotBlank(message = "Nummer är obligatoriskt")
    @Size(max = 50, message = "Nummer får inte vara längre än 50 tecken")
    @Column(name = "number", nullable = false)
    private String number;
    
    @NotBlank(message = "Kopia är obligatoriskt")
    @Size(max = 10, message = "Kopia får inte vara längre än 10 tecken")
    @Column(name = "copy", nullable = false)
    private String copy;
    
    @NotNull(message = "Typ är obligatoriskt")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private KeyType type = KeyType.APARTMENT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
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
    public Key() {
        // Tom konstruktor för JPA
    }
    
    public Key(String series, String number, String copy, KeyType type) {
        this.series = series;
        this.number = number;
        this.copy = copy;
        this.type = type;
    }
    
    public Key(String series, String number, String copy, KeyType type, Apartment apartment) {
        this.series = series;
        this.number = number;
        this.copy = copy;
        this.type = type;
        this.apartment = apartment;
    }
    
    public Key(String series, String number, String copy, KeyType type, Apartment apartment, Tenant tenant) {
        this.series = series;
        this.number = number;
        this.copy = copy;
        this.type = type;
        this.apartment = apartment;
        this.tenant = tenant;
    }
    
    // Getters och Setters (manuella, ingen Lombok)
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getSeries() {
        return series;
    }
    
    public void setSeries(String series) {
        this.series = series;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getCopy() {
        return copy;
    }
    
    public void setCopy(String copy) {
        this.copy = copy;
    }
    
    public KeyType getType() {
        return type;
    }
    
    public void setType(KeyType type) {
        this.type = type;
    }
    
    public Apartment getApartment() {
        return apartment;
    }
    
    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }
    
    public Tenant getTenant() {
        return tenant;
    }
    
    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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
    public String getFullKeyIdentifier() {
        return series + "-" + number + "-" + copy;
    }
    
    @JsonIgnore
    public String getDisplayName() {
        String base = getFullKeyIdentifier();
        if (apartment != null) {
            base += " (" + apartment.getDisplayName() + ")";
        }
        return base + " - " + type.getDisplayName();
    }
    
    @JsonIgnore
    public boolean isAssigned() {
        return tenant != null;
    }
    
    @JsonIgnore
    public boolean isApartmentKey() {
        return type == KeyType.APARTMENT;
    }
    
    @JsonIgnore
    public boolean isMasterKey() {
        return type == KeyType.MASTER;
    }
    
    @JsonIgnore
    public boolean isGarageKey() {
        return type == KeyType.GARAGE;
    }
    
    @JsonIgnore
    public boolean isStorageKey() {
        return type == KeyType.STORAGE;
    }
    
    @JsonIgnore
    public boolean isLaundryKey() {
        return type == KeyType.LAUNDRY;
    }
    
    @JsonIgnore
    public boolean isMailboxKey() {
        return type == KeyType.MAILBOX;
    }
    
    @JsonIgnore
    public boolean isOtherKey() {
        return type == KeyType.OTHER;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return id != null && id.equals(key.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Key{" +
                "id=" + id +
                ", series='" + series + '\'' +
                ", number='" + number + '\'' +
                ", copy='" + copy + '\'' +
                ", type=" + type +
                ", isActive=" + isActive +
                '}';
    }
    
    /**
     * Key type enum
     */
    public enum KeyType {
        APARTMENT("Lägenhet"),
        MASTER("Huvudnyckel"),
        GARAGE("Garage"),
        STORAGE("Förråd"),
        LAUNDRY("Tvätt"),
        MAILBOX("Post"),
        OTHER("Övrigt");
        
        private final String displayName;
        
        KeyType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
} 