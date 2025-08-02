package se.duggals.dfrm.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.duggals.dfrm.model.Apartment;

/**
 * Repository för Apartment entity
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    
    /**
     * Hitta lägenheter genom gata och nummer
     */
    Optional<Apartment> findByStreetAndNumber(String street, String number);
    
    /**
     * Hitta lägenheter genom gata, nummer och lägenhetsnummer
     */
    Optional<Apartment> findByStreetAndNumberAndApartmentNumber(String street, String number, String apartmentNumber);
    
    /**
     * Hitta lediga lägenheter
     */
    List<Apartment> findByOccupiedFalse();
    
    /**
     * Hitta upptagna lägenheter
     */
    List<Apartment> findByOccupiedTrue();
    
    /**
     * Hitta lägenheter genom område
     */
    List<Apartment> findByArea(String area);
    
    /**
     * Hitta lägenheter genom postnummer
     */
    List<Apartment> findByPostalCode(String postalCode);
    
    /**
     * Hitta lägenheter med specifik storlek
     */
    List<Apartment> findBySize(Integer size);
    
    /**
     * Hitta lägenheter med storlek större än eller lika med
     */
    List<Apartment> findBySizeGreaterThanEqual(Integer size);
    
    /**
     * Hitta lägenheter med specifik våning
     */
    List<Apartment> findByFloor(Integer floor);
    
    /**
     * Hitta lägenheter med specifikt antal rum
     */
    List<Apartment> findByRooms(Integer rooms);
    
    /**
     * Hitta lägenheter med månadsavgift mindre än eller lika med
     */
    List<Apartment> findByMonthlyRentLessThanEqual(BigDecimal maxRent);
    
    /**
     * Hitta lägenheter med månadsavgift mellan
     */
    List<Apartment> findByMonthlyRentBetween(BigDecimal minRent, BigDecimal maxRent);
    
    /**
     * Sök lägenheter genom adress
     */
    @Query("SELECT a FROM Apartment a WHERE " +
           "LOWER(a.street) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.number) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.apartmentNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.area) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Apartment> searchApartments(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Hitta lediga lägenheter i specifikt område
     */
    List<Apartment> findByAreaAndOccupiedFalse(String area);
    
    /**
     * Hitta lägenheter med specifik storlek och lediga
     */
    List<Apartment> findBySizeAndOccupiedFalse(Integer size);
    
    /**
     * Hitta lägenheter med specifikt antal rum och lediga
     */
    List<Apartment> findByRoomsAndOccupiedFalse(Integer rooms);
    
    /**
     * Hitta lägenheter med månadsavgift inom intervall och lediga
     */
    List<Apartment> findByMonthlyRentBetweenAndOccupiedFalse(BigDecimal minRent, BigDecimal maxRent);
    
    /**
     * Räkna lediga lägenheter
     */
    long countByOccupiedFalse();
    
    /**
     * Räkna upptagna lägenheter
     */
    long countByOccupiedTrue();
    
    /**
     * Räkna lägenheter per område
     */
    long countByArea(String area);
    
    /**
     * Räkna lägenheter per postnummer
     */
    long countByPostalCode(String postalCode);
    
    /**
     * Hitta lägenheter skapade efter datum
     */
    List<Apartment> findByCreatedAtAfter(java.time.LocalDateTime date);
    
    /**
     * Hitta lägenheter uppdaterade efter datum
     */
    List<Apartment> findByUpdatedAtAfter(java.time.LocalDateTime date);
    
    /**
     * Hitta lägenheter med högst månadsavgift
     */
    @Query("SELECT a FROM Apartment a ORDER BY a.monthlyRent DESC")
    Page<Apartment> findMostExpensiveApartments(Pageable pageable);
    
    /**
     * Hitta lägenheter med lägst månadsavgift
     */
    @Query("SELECT a FROM Apartment a ORDER BY a.monthlyRent ASC")
    Page<Apartment> findCheapestApartments(Pageable pageable);
    
    /**
     * Hitta största lägenheter
     */
    @Query("SELECT a FROM Apartment a ORDER BY a.size DESC")
    Page<Apartment> findLargestApartments(Pageable pageable);
} 