package se.duggals.dfrm.repository;

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
import se.duggals.dfrm.model.Key;
import se.duggals.dfrm.model.Tenant;

/**
 * Repository för Key entity
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Repository
public interface KeyRepository extends JpaRepository<Key, UUID> {
    
    /**
     * Hitta nyckel genom serie, nummer och kopia
     */
    Optional<Key> findBySeriesAndNumberAndCopy(String series, String number, String copy);
    
    /**
     * Hitta aktiva nycklar
     */
    List<Key> findByIsActiveTrue();
    
    /**
     * Hitta nycklar efter typ
     */
    List<Key> findByType(Key.KeyType type);
    
    /**
     * Hitta aktiva nycklar efter typ
     */
    List<Key> findByTypeAndIsActiveTrue(Key.KeyType type);
    
    /**
     * Hitta nycklar för specifik lägenhet
     */
    List<Key> findByApartment(Apartment apartment);
    
    /**
     * Hitta aktiva nycklar för specifik lägenhet
     */
    List<Key> findByApartmentAndIsActiveTrue(Apartment apartment);
    
    /**
     * Hitta nycklar för specifik lägenhet och typ
     */
    List<Key> findByApartmentAndType(Apartment apartment, Key.KeyType type);
    
    /**
     * Hitta nycklar som är utlånade till hyresgäst
     */
    List<Key> findByTenant(Tenant tenant);
    
    /**
     * Hitta aktiva nycklar som är utlånade till hyresgäst
     */
    List<Key> findByTenantAndIsActiveTrue(Tenant tenant);
    
    /**
     * Hitta nycklar av specifik typ som är utlånade till hyresgäst
     */
    List<Key> findByTenantAndType(Tenant tenant, Key.KeyType type);
    
    /**
     * Hitta oanvända nycklar (inte utlånade)
     */
    @Query("SELECT k FROM Key k WHERE k.tenant IS NULL AND k.isActive = true")
    List<Key> findUnassignedKeys();
    
    /**
     * Hitta oanvända nycklar av specifik typ
     */
    @Query("SELECT k FROM Key k WHERE k.tenant IS NULL AND k.type = :type AND k.isActive = true")
    List<Key> findUnassignedKeysByType(@Param("type") Key.KeyType type);
    
    /**
     * Hitta oanvända nycklar för specifik lägenhet
     */
    @Query("SELECT k FROM Key k WHERE k.tenant IS NULL AND k.apartment = :apartment AND k.isActive = true")
    List<Key> findUnassignedKeysByApartment(@Param("apartment") Apartment apartment);
    
    /**
     * Sök nycklar genom serie, nummer eller kopia
     */
    @Query("SELECT k FROM Key k WHERE " +
           "LOWER(k.series) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(k.number) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(k.copy) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Key> searchKeys(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Hitta nycklar skapade under specifik period
     */
    @Query("SELECT k FROM Key k WHERE k.createdAt BETWEEN :startDateTime AND :endDateTime")
    List<Key> findKeysByCreationPeriod(@Param("startDateTime") LocalDateTime startDateTime, 
                                      @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Hitta nycklar som lånades ut under specifik period
     */
    @Query("SELECT k FROM Key k WHERE k.updatedAt BETWEEN :startDateTime AND :endDateTime AND k.tenant IS NOT NULL")
    List<Key> findKeysAssignedInPeriod(@Param("startDateTime") LocalDateTime startDateTime, 
                                      @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Hitta nycklar för lägenheter som är lediga
     */
    @Query("SELECT k FROM Key k WHERE k.apartment IN " +
           "(SELECT a FROM Apartment a WHERE a.tenant IS NULL) AND k.isActive = true")
    List<Key> findKeysForVacantApartments();
    
    /**
     * Hitta nycklar för lägenheter som är upptagna
     */
    @Query("SELECT k FROM Key k WHERE k.apartment IN " +
           "(SELECT a FROM Apartment a WHERE a.tenant IS NOT NULL) AND k.isActive = true")
    List<Key> findKeysForOccupiedApartments();
    
    /**
     * Hitta huvudnycklar (master keys)
     */
    @Query("SELECT k FROM Key k WHERE k.type = 'MASTER' AND k.isActive = true")
    List<Key> findMasterKeys();
    
    /**
     * Hitta nycklar som behöver returneras (hyresgäst har flyttat ut)
     */
    @Query("SELECT k FROM Key k WHERE k.tenant IN " +
           "(SELECT t FROM Tenant t WHERE t.status = 'TERMINATED') AND k.isActive = true")
    List<Key> findKeysNeedingReturn();
    
    /**
     * Räkna nycklar per typ
     */
    long countByType(Key.KeyType type);
    
    /**
     * Räkna aktiva nycklar per typ
     */
    long countByTypeAndIsActiveTrue(Key.KeyType type);
    
    /**
     * Räkna nycklar per lägenhet
     */
    long countByApartment(Apartment apartment);
    
    /**
     * Räkna nycklar per hyresgäst
     */
    long countByTenant(Tenant tenant);
    
    /**
     * Räkna oanvända nycklar
     */
    @Query("SELECT COUNT(k) FROM Key k WHERE k.tenant IS NULL AND k.isActive = true")
    long countUnassignedKeys();
    
    /**
     * Räkna oanvända nycklar per typ
     */
    @Query("SELECT COUNT(k) FROM Key k WHERE k.tenant IS NULL AND k.type = :type AND k.isActive = true")
    long countUnassignedKeysByType(@Param("type") Key.KeyType type);
    
    /**
     * Hitta nycklar skapade efter datum
     */
    List<Key> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Hitta nycklar uppdaterade efter datum
     */
    List<Key> findByUpdatedAtAfter(LocalDateTime date);
    
    /**
     * Hitta senaste nycklar
     */
    @Query("SELECT k FROM Key k ORDER BY k.createdAt DESC")
    Page<Key> findLatestKeys(Pageable pageable);
    
    /**
     * Hitta nycklar som behöver returneras snart
     */
    @Query("SELECT k FROM Key k WHERE k.tenant IN " +
           "(SELECT t FROM Tenant t WHERE t.moveOutDate BETWEEN :startDate AND :endDate) AND k.isActive = true")
    List<Key> findKeysNeedingReturnSoon(@Param("startDate") java.time.LocalDate startDate, 
                                       @Param("endDate") java.time.LocalDate endDate);
    
    /**
     * Hitta nycklar för specifik serie
     */
    List<Key> findBySeries(String series);
    
    /**
     * Hitta nycklar för specifikt nummer
     */
    List<Key> findByNumber(String number);
    
    /**
     * Hitta nycklar för specifik kopia
     */
    List<Key> findByCopy(String copy);
    
    /**
     * Hitta nycklar med specifika anteckningar
     */
    @Query("SELECT k FROM Key k WHERE LOWER(k.notes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Key> findByNotesContaining(@Param("searchTerm") String searchTerm);
} 