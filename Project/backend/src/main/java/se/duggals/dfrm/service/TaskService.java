package se.duggals.dfrm.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.duggals.dfrm.model.Task;
import se.duggals.dfrm.repository.TaskRepository;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Skapa en ny uppgift
     */
    public Task createTask(Task task) {
        // Sätt standardvärden
        if (task.getStatus() == null) {
            task.setStatus(Task.TaskStatus.PENDING);
        }
        if (task.getPriority() == null) {
            task.setPriority(Task.TaskPriority.MEDIUM);
        }

        return taskRepository.save(task);
    }

    /**
     * Hämta uppgift efter ID
     */
    public Optional<Task> getTaskById(UUID id) {
        return taskRepository.findById(id);
    }

    /**
     * Hämta alla uppgifter
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Hämta uppgifter efter status
     */
    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    /**
     * Hämta uppgifter efter prioritet
     */
    public List<Task> getTasksByPriority(Task.TaskPriority priority) {
        return taskRepository.findByPriority(priority);
    }

    /**
     * Sök uppgifter
     */
    public Page<Task> searchTasks(String searchTerm, Pageable pageable) {
        // TaskRepository har bara List<Task> searchTasks(String searchTerm)
        // Vi behöver implementera paginering manuellt
        List<Task> allTasks = taskRepository.searchTasks(searchTerm);
        // Enkel implementation - i produktion skulle man använda en mer sofistikerad approach
        return new org.springframework.data.domain.PageImpl<>(allTasks, pageable, allTasks.size());
    }

    /**
     * Uppdatera uppgift
     */
    public Task updateTask(UUID id, Task updatedTask) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uppgift hittades inte"));

        // Uppdatera fält
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setEstimatedHours(updatedTask.getEstimatedHours());
        existingTask.setActualHours(updatedTask.getActualHours());
        existingTask.setCost(updatedTask.getCost());
        existingTask.setAssignedUser(updatedTask.getAssignedUser());
        existingTask.setApartment(updatedTask.getApartment());

        return taskRepository.save(existingTask);
    }

    /**
     * Slutför uppgift
     */
    public void completeTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uppgift hittades inte"));

        task.setStatus(Task.TaskStatus.COMPLETED);
        task.setCompletedDate(LocalDateTime.now());
        taskRepository.save(task);
    }

    /**
     * Pausa uppgift
     */
    public void pauseTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uppgift hittades inte"));

        task.setStatus(Task.TaskStatus.ON_HOLD);
        taskRepository.save(task);
    }

    /**
     * Återuppta uppgift
     */
    public void resumeTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uppgift hittades inte"));

        task.setStatus(Task.TaskStatus.IN_PROGRESS);
        taskRepository.save(task);
    }

    /**
     * Avbryt uppgift
     */
    public void cancelTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uppgift hittades inte"));

        task.setStatus(Task.TaskStatus.CANCELLED);
        taskRepository.save(task);
    }

    /**
     * Ta bort uppgift (soft delete)
     */
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Uppgift hittades inte"));

        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    /**
     * Hämta väntande uppgifter
     */
    public List<Task> getPendingTasks() {
        return taskRepository.findByStatus(Task.TaskStatus.PENDING);
    }

    /**
     * Hämta slutförda uppgifter
     */
    public List<Task> getCompletedTasks() {
        return taskRepository.findByStatus(Task.TaskStatus.COMPLETED);
    }

    /**
     * Hämta försenade uppgifter
     */
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now());
    }

    /**
     * Hämta uppgifter med hög prioritet
     */
    public List<Task> getHighPriorityTasks() {
        return taskRepository.findHighPriorityPendingTasks();
    }

    /**
     * Hämta uppgifter som förfaller idag
     */
    public List<Task> getTasksDueToday() {
        return taskRepository.findTasksDueToday();
    }

    /**
     * Hämta uppgifter som förfaller denna vecka
     */
    public List<Task> getTasksDueThisWeek() {
        return taskRepository.findTasksDueThisWeek();
    }

    /**
     * Hämta uppgifter som förfaller denna månad
     */
    public List<Task> getTasksDueThisMonth() {
        return taskRepository.findTasksDueThisMonth();
    }

    /**
     * Hämta uppgifter som skapades efter ett visst datum
     */
    public List<Task> getTasksCreatedAfter(LocalDateTime date) {
        // Använd findByCreatedAtBetween med startDate som date och endDate som nu
        return taskRepository.findByCreatedAtBetween(date, LocalDateTime.now());
    }

    /**
     * Hämta uppgifter som slutfördes efter ett visst datum
     */
    public List<Task> getTasksCompletedAfter(LocalDateTime date) {
        // Använd findByCompletedDateBetween med startDate som date och endDate som nu
        return taskRepository.findByCompletedDateBetween(date, LocalDateTime.now());
    }

    /**
     * Hämta uppgifter som skickade email
     */
    public List<Task> getTasksWithEmailSent() {
        return taskRepository.findByEmailSentTrue();
    }

    /**
     * Hämta uppgifter som inte skickade email
     */
    public List<Task> getTasksWithoutEmailSent() {
        return taskRepository.findByEmailSentFalse();
    }

    /**
     * Räkna totalt antal uppgifter
     */
    public long countAllTasks() {
        return taskRepository.count();
    }

    /**
     * Räkna uppgifter efter status
     */
    public long countTasksByStatus(Task.TaskStatus status) {
        return taskRepository.findByStatus(status).size();
    }

    /**
     * Räkna uppgifter efter prioritet
     */
    public long countTasksByPriority(Task.TaskPriority priority) {
        return taskRepository.findByPriority(priority).size();
    }

    /**
     * Räkna väntande uppgifter
     */
    public long countPendingTasks() {
        return taskRepository.findByStatus(Task.TaskStatus.PENDING).size();
    }

    /**
     * Räkna slutförda uppgifter
     */
    public long countCompletedTasks() {
        return taskRepository.findByStatus(Task.TaskStatus.COMPLETED).size();
    }

    /**
     * Räkna försenade uppgifter
     */
    public long countOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDateTime.now()).size();
    }
} 