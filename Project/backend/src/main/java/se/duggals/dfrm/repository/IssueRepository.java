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
import se.duggals.dfrm.model.Issue;

/**
 * Repository för Issue entity
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, UUID> {
    
    /**
     * Hitta felanmälningar genom e-post
     */
    Optional<Issue> findByReporterEmail(String email);
    
    /**
     * Hitta felanmälningar genom e-post (case-insensitive)
     */
    Optional<Issue> findByReporterEmailIgnoreCase(String email);
    
    /**
     * Hitta felanmälningar genom status
     */
    List<Issue> findByStatus(Issue.IssueStatus status);
    
    /**
     * Hitta felanmälningar genom prioritet
     */
    List<Issue> findByPriority(Issue.IssuePriority priority);
    
    /**
     * Hitta felanmälningar för specifik lägenhet
     */
    List<Issue> findByApartment(Apartment apartment);
    
    /**
     * Hitta felanmälningar för specifik lägenhet och status
     */
    List<Issue> findByApartmentAndStatus(Apartment apartment, Issue.IssueStatus status);
    
    /**
     * Hitta felanmälningar för specifik lägenhet och prioritet
     */
    List<Issue> findByApartmentAndPriority(Apartment apartment, Issue.IssuePriority priority);
    
    
    
    /**
     * Hitta felanmälningar med hög prioritet
     */
    @Query("SELECT i FROM Issue i WHERE i.priority IN ('HIGH', 'URGENT')")
    List<Issue> findHighPriorityIssues();
    
    /**
     * Hitta felanmälningar som godkändes efter datum
     */
    List<Issue> findByApprovedDateAfter(LocalDate date);
    
    /**
     * Hitta felanmälningar som avvisades efter datum
     */
    List<Issue> findByRejectedDateAfter(LocalDate date);
    
    /**
     * Hitta felanmälningar där e-post skickats
     */
    List<Issue> findByEmailSentTrue();
    
    /**
     * Hitta felanmälningar där e-post skickats efter datum
     */
    List<Issue> findByEmailSentDateAfter(LocalDateTime date);
    
    /**
     * Sök felanmälningar genom namn, e-post eller ämne
     */
    @Query("SELECT i FROM Issue i WHERE " +
           "LOWER(i.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(i.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Issue> searchIssues(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    /**
     * Hitta felanmälningar skapade under specifik period
     */
    @Query("SELECT i FROM Issue i WHERE i.createdAt BETWEEN :startDateTime AND :endDateTime")
    List<Issue> findIssuesByCreationPeriod(@Param("startDateTime") LocalDateTime startDateTime, 
                                          @Param("endDateTime") LocalDateTime endDateTime);
    
    /**
     * Hitta felanmälningar som godkändes under specifik period
     */
    @Query("SELECT i FROM Issue i WHERE i.approvedDate BETWEEN :startDate AND :endDate")
    List<Issue> findIssuesByApprovalPeriod(@Param("startDate") LocalDate startDate, 
                                          @Param("endDate") LocalDate endDate);
    
    /**
     * Hitta felanmälningar som avvisades under specifik period
     */
    @Query("SELECT i FROM Issue i WHERE i.rejectedDate BETWEEN :startDate AND :endDate")
    List<Issue> findIssuesByRejectionPeriod(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    /**
     * Hitta felanmälningar som inte har fått e-post skickat
     */
    @Query("SELECT i FROM Issue i WHERE i.emailSent = false OR i.emailSent IS NULL")
    List<Issue> findIssuesWithoutEmailSent();
    
    /**
     * Hitta felanmälningar som behöver e-post skickas
     */
    @Query("SELECT i FROM Issue i WHERE (i.emailSent = false OR i.emailSent IS NULL) AND i.status != 'NEW'")
    List<Issue> findIssuesNeedingEmailNotification();
    
    /**
     * Hitta felanmälningar med högsta prioritet
     */
    @Query("SELECT i FROM Issue i WHERE i.priority = 'URGENT' ORDER BY i.createdAt ASC")
    List<Issue> findUrgentIssues();
    
    /**
     * Hitta felanmälningar med hög prioritet (sorterade)
     */
    @Query("SELECT i FROM Issue i WHERE i.priority IN ('HIGH', 'URGENT') ORDER BY i.priority DESC, i.createdAt ASC")
    List<Issue> findHighPriorityIssuesSorted();
    
    /**
     * Räkna felanmälningar per status
     */
    long countByStatus(Issue.IssueStatus status);
    
    /**
     * Räkna felanmälningar per prioritet
     */
    long countByPriority(Issue.IssuePriority priority);
    
    /**
     * Räkna felanmälningar per lägenhet
     */
    long countByApartment(Apartment apartment);
    
    /**
     * Räkna felanmälningar per lägenhet och status
     */
    long countByApartmentAndStatus(Apartment apartment, Issue.IssueStatus status);
    
    /**
     * Räkna felanmälningar per lägenhet och prioritet
     */
    long countByApartmentAndPriority(Apartment apartment, Issue.IssuePriority priority);
    
    /**
     * Räkna felanmälningar som godkändes under specifik period
     */
    long countByApprovedDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Räkna felanmälningar som avvisades under specifik period
     */
    long countByRejectedDateBetween(LocalDate startDate, LocalDate endDate);
    
    /**
     * Räkna felanmälningar där e-post skickats
     */
    long countByEmailSentTrue();
    
    /**
     * Hitta felanmälningar skapade efter datum
     */
    List<Issue> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Hitta felanmälningar uppdaterade efter datum
     */
    List<Issue> findByUpdatedAtAfter(LocalDateTime date);
    
    /**
     * Hitta senaste felanmälningar
     */
    @Query("SELECT i FROM Issue i ORDER BY i.createdAt DESC")
    Page<Issue> findLatestIssues(Pageable pageable);
    
    /**
     * Hitta felanmälningar med högsta prioritet först
     */
    @Query("SELECT i FROM Issue i ORDER BY " +
           "CASE i.priority " +
           "  WHEN 'URGENT' THEN 1 " +
           "  WHEN 'HIGH' THEN 2 " +
           "  WHEN 'MEDIUM' THEN 3 " +
           "  WHEN 'LOW' THEN 4 " +
           "END, i.createdAt ASC")
    Page<Issue> findIssuesByPriority(Pageable pageable);
    
    /**
     * Hitta felanmälningar som behöver godkännas
     */
    @Query("SELECT i FROM Issue i WHERE i.status = 'NEW' ORDER BY i.priority DESC, i.createdAt ASC")
    List<Issue> findIssuesNeedingApproval();
    
    /**
     * Hitta felanmälningar som behöver e-post skickas för godkännande/avvisning
     */
    @Query("SELECT i FROM Issue i WHERE (i.emailSent = false OR i.emailSent IS NULL) AND i.status IN ('APPROVED', 'REJECTED')")
    List<Issue> findIssuesNeedingApprovalRejectionEmail();
} 