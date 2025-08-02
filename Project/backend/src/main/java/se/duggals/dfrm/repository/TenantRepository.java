package se.duggals.dfrm.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import se.duggals.dfrm.model.Tenant;

/**
 * Repository för Tenant entity
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    
    /**
     * Hitta hyresgäst genom e-post
     */
    Optional<Tenant> findByEmail(String email);
    
    /**
     * Hitta hyresgäst genom e-post (case-insensitive)
     */
    Optional<Tenant> findByEmailIgnoreCase(String email);
    
    /**
     * Hitta hyresgäst genom personnummer
     */
    Optional<Tenant> findByPersonalNumber(String personalNumber);
    
    /**
     * Hitta aktiva hyresgäster
     */
    List<Tenant> findByStatus(Tenant.TenantStatus status);
    
    /**
     * Hitta aktiva hyresgäster
     */
    List<Tenant> findByStatusAndMoveOutDateIsNull(Tenant.TenantStatus status);
    
    /**
     * Hitta hyresgäster för specifik lägenhet
     */
    List<Tenant> findByApartment(Apartment apartment);
    
    /**
     * Hitta aktiva hyresgäster för specifik lägenhet
     */
    List<Tenant> findByApartmentAndStatus(Apartment apartment, Tenant.TenantStatus status);
    
    /**
     * Hitta hyresgäster som flyttade in efter datum
     */
    List<Tenant> findByMoveInDateAfter(LocalDate date);
    
    /**
     * Hitta hyresgäster som flyttade ut efter datum
     */
    List<Tenant> findByMoveOutDateAfter(LocalDate date);
    
    /**
     * Hitta hyresgäster som flyttade in före datum
     */
    List<Tenant> findByMoveInDateBefore(LocalDate date);
    
    /**
     * Hitta hyresgäster med månadsavgift större än eller lika med
     */
    List<Tenant> findByMonthlyRentGreaterThanEqual(BigDecimal minRent);
    
    /**
     * Hitta hyresgäster med månadsavgift mellan
     */
    List<Tenant> findByMonthlyRentBetween(BigDecimal minRent, BigDecimal maxRent);
    
    /**
     * Sök hyresgäster genom namn eller e-post
     */
    @Query("SELECT t FROM Tenant t WHERE " +
           "LOWER(t.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Tenant> searchTenants(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Hitta hyresgäster som flyttade ut men inte har status TERMINATED_NOT_MOVED_OUT
     */
    @Query("SELECT t FROM Tenant t WHERE t.moveOutDate IS NOT NULL AND t.status != 'TERMINATED_NOT_MOVED_OUT'")
    List<Tenant> findTenantsWhoMovedOut();
    
    /**
     * Hitta hyresgäster som har flyttat ut men inte har status TERMINATED
     */
    @Query("SELECT t FROM Tenant t WHERE t.moveOutDate IS NOT NULL AND t.status = 'ACTIVE'")
    List<Tenant> findActiveTenantsWhoMovedOut();
    
    /**
     * Hitta hyresgäster som flyttade in under specifik period
     */
    @Query("SELECT t FROM Tenant t WHERE t.moveInDate BETWEEN :startDate AND :endDate")
    List<Tenant> findTenantsByMoveInPeriod(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Hitta hyresgäster som flyttade ut under specifik period
     */
    @Query("SELECT t FROM Tenant t WHERE t.moveOutDate BETWEEN :startDate AND :endDate")
    List<Tenant> findTenantsByMoveOutPeriod(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    /**
     * Hitta hyresgäster som avslutades under specifik period
     */
    @Query("SELECT t FROM Tenant t WHERE t.terminationDate BETWEEN :startDate AND :endDate")
    List<Tenant> findTenantsByTerminationPeriod(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * Räkna aktiva hyresgäster
     */
    long countByStatus(Tenant.TenantStatus status);
    
    /**
     * Räkna hyresgäster per lägenhet
     */
    long countByApartment(Apartment apartment);
    
    /**
     * Räkna hyresgäster som flyttade in under specifik period
     */
    long countByMoveInDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Räkna hyresgäster som flyttade ut under specifik period
     */
    long countByMoveOutDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Hitta hyresgäster skapade efter datum
     */
    List<Tenant> findByCreatedAtAfter(java.time.LocalDateTime date);
    
    /**
     * Hitta hyresgäster uppdaterade efter datum
     */
    List<Tenant> findByUpdatedAtAfter(java.time.LocalDateTime date);
    
    /**
     * Hitta hyresgäster med högst månadsavgift
     */
    @Query("SELECT t FROM Tenant t ORDER BY t.monthlyRent DESC")
    Page<Tenant> findTenantsWithHighestRent(Pageable pageable);
    
    /**
     * Hitta hyresgäster med lägst månadsavgift
     */
    @Query("SELECT t FROM Tenant t ORDER BY t.monthlyRent ASC")
    Page<Tenant> findTenantsWithLowestRent(Pageable pageable);
    
    /**
     * Hitta hyresgäster som har bott längst
     */
    @Query("SELECT t FROM Tenant t ORDER BY t.moveInDate ASC")
    Page<Tenant> findLongestTenants(Pageable pageable);
    
    /**
     * Hitta hyresgäster som flyttade in först
     */
    @Query("SELECT t FROM Tenant t WHERE t.moveInDate = (SELECT MIN(t2.moveInDate) FROM Tenant t2)")
    List<Tenant> findFirstTenants();
} 