package se.duggals.dfrm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Apartment entity för DFRM-systemet
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Entity
@Table(name = "apartments")
public class Apartment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
        @NotBlank(message = "Gata är obligatoriskt")
    @Size(max = 255, message = "Gata får inte vara längre än 255 tecken")
    @Column(nullable = false)
    private String street;

    @NotBlank(message = "Nummer är obligatoriskt")
    @Size(max = 255, message = "Nummer får inte vara längre än 255 tecken")
    @Column(nullable = false)
    private String number;

    @NotBlank(message = "Lägenhetsnummer är obligatoriskt")
    @Size(max = 255, message = "Lägenhetsnummer får inte vara längre än 255 tecken")
    @Column(name = "apartment_number", nullable = false)
    private String apartmentNumber;

    @Positive(message = "Storlek måste vara positiv")
    private Integer size;

    @NotNull(message = "Våning är obligatoriskt")
    @Positive(message = "Våning måste vara positiv")
    @Column(nullable = false)
    private Integer floor;
    
    @NotNull(message = "Område är obligatoriskt")
    @Column(name = "area", nullable = false)
    private Double area;
    
    @NotNull(message = "Antal rum är obligatoriskt")
    @Positive(message = "Antal rum måste vara positiv")
    @Column(nullable = false)
    private Integer rooms;
    
    @Positive(message = "Månadsavgift måste vara positiv")
    @Column(name = "monthly_rent", precision = 10, scale = 2)
    private BigDecimal monthlyRent;
    
    @Size(max = 10, message = "Postnummer får inte vara längre än 10 tecken")
    @Column(name = "postal_code")
    private String postalCode;
    
    @NotNull(message = "Upptagen status är obligatoriskt")
    @Column(nullable = false)
    private Boolean occupied = false;
    
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
    public Apartment() {
        // Tom konstruktor för JPA
    }
    
    public Apartment(String street, String number, String apartmentNumber) {
        this.street = street;
        this.number = number;
        this.apartmentNumber = apartmentNumber;
    }
    
    public Apartment(String street, String number, String apartmentNumber, 
                    Integer size, Integer floor, Double area, Integer rooms, 
                    BigDecimal monthlyRent, String postalCode) {
        this.street = street;
        this.number = number;
        this.apartmentNumber = apartmentNumber;
        this.size = size;
        this.floor = floor;
        this.area = area;
        this.rooms = rooms;
        this.monthlyRent = monthlyRent;
        this.postalCode = postalCode;
    }
    
    // Getters och Setters (manuella, ingen Lombok)
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getStreet() {
        return street;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getApartmentNumber() {
        return apartmentNumber;
    }
    
    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public Integer getFloor() {
        return floor;
    }
    
    public void setFloor(Integer floor) {
        this.floor = floor;
    }
    
        public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }
    
    public Integer getRooms() {
        return rooms;
    }
    
    public void setRooms(Integer rooms) {
        this.rooms = rooms;
    }
    
    public BigDecimal getMonthlyRent() {
        return monthlyRent;
    }
    
    public void setMonthlyRent(BigDecimal monthlyRent) {
        this.monthlyRent = monthlyRent;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public Boolean getOccupied() {
        return occupied;
    }
    
    public void setOccupied(Boolean occupied) {
        this.occupied = occupied;
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
    public String getFullAddress() {
        StringBuilder address = new StringBuilder(street + " " + number);
        if (apartmentNumber != null && !apartmentNumber.isEmpty()) {
            address.append(" ").append(apartmentNumber);
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            address.append(", ").append(postalCode);
        }
        return address.toString();
    }
    
    @JsonIgnore
    public String getDisplayName() {
        StringBuilder name = new StringBuilder(street + " " + number);
        if (apartmentNumber != null && !apartmentNumber.isEmpty()) {
            name.append(" ").append(apartmentNumber);
        }
        return name.toString();
    }
    
    @JsonIgnore
    public boolean isAvailable() {
        return !occupied;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Apartment apartment = (Apartment) o;
        return id != null && id.equals(apartment.id);
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Apartment{" +
                "id=" + id +
                ", street='" + street + '\'' +
                ", number='" + number + '\'' +
                ", apartmentNumber='" + apartmentNumber + '\'' +
                ", occupied=" + occupied +
                '}';
    }
} 