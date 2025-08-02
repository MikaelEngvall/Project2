package se.duggals.dfrm.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import se.duggals.dfrm.model.Interest;

/**
 * Repository för Interest entity
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Repository
public interface InterestRepository extends JpaRepository<Interest, UUID> {
    
    /**
     * Hitta intresseanmälningar genom e-post
     */
    Optional<Interest> findByEmail(String email);
    
    /**
     * Hitta intresseanmälningar genom e-post (case-insensitive)
     */
    Optional<Interest> findByEmailIgnoreCase(String email);
    
    /**
     * Hitta intresseanmälningar genom status
     */
    List<Interest> findByStatus(Interest.InterestStatus status);
    
    /**
     * Hitta intresseanmälningar för specifik lägenhet
     */
    List<Interest> findByApartment(Apartment apartment);
    
    /**
     * Hitta intresseanmälningar för specifik lägenhet och status
     */
    List<Interest> findByApartmentAndStatus(Apartment apartment, Interest.InterestStatus status);
    
    /**
     * Hitta intresseanmälningar med visning på specifikt datum
     */
    List<Interest> findByViewingDate(LocalDate viewingDate);
    
    /**
     * Hitta intresseanmälningar med visning efter datum
     */
    List<Interest> findByViewingDateAfter(LocalDate date);
    
    /**
     * Hitta intresseanmälningar med visning före datum
     */
    List<Interest> findByViewingDateBefore(LocalDate date);
    
    /**
     * Hitta intresseanmälningar med bekräftad visning
     */
    List<Interest> findByViewingConfirmedTrue();
    
    /**
     * Hitta intresseanmälningar med bekräftad visning för specifik lägenhet
     */
    List<Interest> findByApartmentAndViewingConfirmedTrue(Apartment apartment);
    
    /**
     * Hitta intresseanmälningar där e-post skickats
     */
    List<Interest> findByViewingEmailSentTrue();
    
    /**
     * Hitta intresseanmälningar där e-post skickats efter datum
     */
    List<Interest> findByViewingEmailSentDateAfter(LocalDateTime date);
    
    /**
     * Sök intresseanmälningar genom namn eller e-post
     */
    @Query("SELECT i FROM Interest i WHERE " +
           "LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Interest> searchInterests(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Hitta intresseanmälningar med visning under specifik period
     */
    @Query("SELECT i FROM Interest i WHERE i.viewingDate BETWEEN :startDate AND :endDate")
    List<Interest> findInterestsByViewingPeriod(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * Hitta intresseanmälningar skapade under specifik period
     */
    @Query("SELECT i FROM Interest i WHERE i.createdAt BETWEEN :startDateTime AND :endDateTime")
    List<Interest> findInterestsByCreationPeriod(@Param("startDateTime") LocalDateTime startDateTime, 
                                                @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Hitta intresseanmälningar som inte har fått e-post skickat
     */
    @Query("SELECT i FROM Interest i WHERE i.viewingEmailSent = false OR i.viewingEmailSent IS NULL")
    List<Interest> findInterestsWithoutEmailSent();
    
    /**
     * Hitta intresseanmälningar som har visning schemalagd men inte bekräftad
     */
    @Query("SELECT i FROM Interest i WHERE i.viewingDate IS NOT NULL AND i.viewingConfirmed = false")
    List<Interest> findInterestsWithScheduledButNotConfirmedViewing();
    
    /**
     * Hitta intresseanmälningar som har visning schemalagd för idag
     */
    @Query("SELECT i FROM Interest i WHERE i.viewingDate = :today")
    List<Interest> findInterestsWithViewingToday(@Param("today") LocalDate today);
    
    /**
     * Hitta intresseanmälningar som har visning schemalagd för imorgon
     */
    @Query("SELECT i FROM Interest i WHERE i.viewingDate = :tomorrow")
    List<Interest> findInterestsWithViewingTomorrow(@Param("tomorrow") LocalDate tomorrow);
    
    /**
     * Räkna intresseanmälningar per status
     */
    long countByStatus(Interest.InterestStatus status);
    
    /**
     * Räkna intresseanmälningar per lägenhet
     */
    long countByApartment(Apartment apartment);
    
    /**
     * Räkna intresseanmälningar per lägenhet och status
     */
    long countByApartmentAndStatus(Apartment apartment, Interest.InterestStatus status);
    
    /**
     * Räkna intresseanmälningar med visning på specifikt datum
     */
    long countByViewingDate(LocalDate viewingDate);
    
    /**
     * Räkna intresseanmälningar med bekräftad visning
     */
    long countByViewingConfirmedTrue();
    
    /**
     * Räkna intresseanmälningar där e-post skickats
     */
    long countByViewingEmailSentTrue();
    
    /**
     * Hitta intresseanmälningar skapade efter datum
     */
    List<Interest> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Hitta intresseanmälningar uppdaterade efter datum
     */
    List<Interest> findByUpdatedAtAfter(LocalDateTime date);
    
    /**
     * Hitta senaste intresseanmälningar
     */
    @Query("SELECT i FROM Interest i ORDER BY i.createdAt DESC")
    Page<Interest> findLatestInterests(Pageable pageable);
    
    /**
     * Hitta intresseanmälningar med visning snart
     */
    @Query("SELECT i FROM Interest i WHERE i.viewingDate BETWEEN :startDate AND :endDate ORDER BY i.viewingDate ASC")
    List<Interest> findInterestsWithUpcomingViewings(@Param("startDate") LocalDate startDate, 
                                                    @Param("endDate") LocalDate endDate);
    
    /**
     * Hitta intresseanmälningar som behöver e-post skickas
     */
    @Query("SELECT i FROM Interest i WHERE i.viewingDate IS NOT NULL AND (i.viewingEmailSent = false OR i.viewingEmailSent IS NULL)")
    List<Interest> findInterestsNeedingEmailNotification();
} 