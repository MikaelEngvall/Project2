package se.duggals.dfrm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.duggals.dfrm.model.User;

/**
 * Repository för User entity
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    /**
     * Hitta användare genom e-post
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Hitta användare genom e-post (case-insensitive)
     */
    Optional<User> findByEmailIgnoreCase(String email);
    
    /**
     * Hitta aktiva användare
     */
    List<User> findByActiveTrue();
    
    /**
     * Hitta användare genom roll
     */
    List<User> findByRole(User.UserRole role);
    
    /**
     * Hitta aktiva användare genom roll
     */
    List<User> findByRoleAndActiveTrue(User.UserRole role);
    
    /**
     * Sök användare genom namn (förnamn eller efternamn)
     */
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<User> searchUsers(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Hitta användare med specifika behörigheter
     */
    @Query("SELECT u FROM User u WHERE u.permissions LIKE %:permission%")
    List<User> findByPermission(@Param("permission") String permission);
    
    /**
     * Hitta användare med specifikt språk
     */
    List<User> findByPreferredLanguage(String language);
    
    /**
     * Kontrollera om e-post redan finns
     */
    boolean existsByEmail(String email);
    
    /**
     * Kontrollera om e-post redan finns (case-insensitive)
     */
    boolean existsByEmailIgnoreCase(String email);
    
    /**
     * Räkna aktiva användare
     */
    long countByActiveTrue();
    
    /**
     * Räkna användare per roll
     */
    long countByRole(User.UserRole role);
    
    /**
     * Hitta användare skapade efter datum
     */
    List<User> findByCreatedAtAfter(java.time.LocalDateTime date);
    
    /**
     * Hitta användare uppdaterade efter datum
     */
    List<User> findByUpdatedAtAfter(java.time.LocalDateTime date);
} 