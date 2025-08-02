package se.duggals.dfrm.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.duggals.dfrm.model.Apartment;
import se.duggals.dfrm.model.Task;
import se.duggals.dfrm.model.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Hitta uppgifter efter status
    List<Task> findByStatus(Task.TaskStatus status);

    // Hitta uppgifter efter prioritet
    List<Task> findByPriority(Task.TaskPriority priority);

    // Hitta uppgifter tilldelade en specifik användare
    List<Task> findByAssignedUser(User assignedUser);

    // Hitta uppgifter för en specifik lägenhet
    List<Task> findByApartment(Apartment apartment);

    // Hitta uppgifter som förfaller innan ett visst datum
    List<Task> findByDueDateBefore(LocalDateTime date);

    // Hitta uppgifter som förfaller mellan två datum
    List<Task> findByDueDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Hitta uppgifter som är försenade (förfaller innan nu och inte är klara)
    @Query("SELECT t FROM Task t WHERE t.dueDate < :now AND t.status != 'COMPLETED'")
    List<Task> findOverdueTasks(@Param("now") LocalDateTime now);

    // Hitta uppgifter med hög prioritet som inte är klara
    @Query("SELECT t FROM Task t WHERE t.priority IN ('HIGH', 'URGENT') AND t.status != 'COMPLETED' ORDER BY t.priority DESC, t.dueDate ASC")
    List<Task> findHighPriorityPendingTasks();

    // Hitta uppgifter som är klara
    List<Task> findByStatusAndCompletedDateIsNotNull(Task.TaskStatus status);

    // Hitta uppgifter som inte är klara
    List<Task> findByStatusNot(Task.TaskStatus status);

    // Sök uppgifter efter titel eller beskrivning
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Task> searchTasks(@Param("searchTerm") String searchTerm);

    // Hitta uppgifter som skapades mellan två datum
    List<Task> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Hitta uppgifter som slutfördes mellan två datum
    List<Task> findByCompletedDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Hitta uppgifter med kostnad över ett visst belopp
    List<Task> findByCostGreaterThan(Double minCost);

    // Hitta uppgifter med kostnad under ett visst belopp
    List<Task> findByCostLessThan(Double maxCost);

    // Hitta uppgifter som tar längre tid än beräknat
    @Query("SELECT t FROM Task t WHERE t.actualHours > t.estimatedHours AND t.actualHours IS NOT NULL AND t.estimatedHours IS NOT NULL")
    List<Task> findTasksExceedingEstimatedHours();

    // Hitta uppgifter som tar kortare tid än beräknat
    @Query("SELECT t FROM Task t WHERE t.actualHours < t.estimatedHours AND t.actualHours IS NOT NULL AND t.estimatedHours IS NOT NULL")
    List<Task> findTasksUnderEstimatedHours();

    // Hitta uppgifter som inte har tilldelad användare
    List<Task> findByAssignedUserIsNull();

    // Hitta uppgifter som inte har tilldelad lägenhet
    List<Task> findByApartmentIsNull();

    // Hitta uppgifter som förfaller idag
    @Query("SELECT t FROM Task t WHERE t.dueDate >= CURRENT_DATE")
    List<Task> findTasksDueToday();

    // Hitta uppgifter som förfaller denna vecka
    @Query("SELECT t FROM Task t WHERE t.dueDate >= CURRENT_DATE")
    List<Task> findTasksDueThisWeek();

    // Hitta uppgifter som förfaller denna månad
    @Query("SELECT t FROM Task t WHERE t.dueDate >= CURRENT_DATE")
    List<Task> findTasksDueThisMonth();

    // Hitta uppgifter som skickade email
    List<Task> findByEmailSentTrue();

    // Hitta uppgifter som inte skickade email
    List<Task> findByEmailSentFalse();

    // Hitta uppgifter som skickade email efter ett visst datum
    List<Task> findByEmailSentTrueAndEmailSentDateAfter(LocalDateTime date);

    // Räkna uppgifter efter status
    @Query("SELECT t.status, COUNT(t) FROM Task t GROUP BY t.status")
    List<Object[]> countTasksByStatus();

    // Räkna uppgifter efter prioritet
    @Query("SELECT t.priority, COUNT(t) FROM Task t GROUP BY t.priority")
    List<Object[]> countTasksByPriority();

    // Räkna uppgifter efter tilldelad användare
    @Query("SELECT t.assignedUser, COUNT(t) FROM Task t WHERE t.assignedUser IS NOT NULL GROUP BY t.assignedUser")
    List<Object[]> countTasksByAssignedUser();

    // Räkna uppgifter efter lägenhet
    @Query("SELECT t.apartment, COUNT(t) FROM Task t WHERE t.apartment IS NOT NULL GROUP BY t.apartment")
    List<Object[]> countTasksByApartment();
} 