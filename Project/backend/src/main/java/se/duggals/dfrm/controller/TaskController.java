package se.duggals.dfrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.duggals.dfrm.model.Task;
import se.duggals.dfrm.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * Skapa en ny uppgift
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        try {
            Task createdTask = taskService.createTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Hämta uppgift efter ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable UUID id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta alla uppgifter
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter efter status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        List<Task> tasks = taskService.getTasksByStatus(status);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter efter prioritet
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(@PathVariable Task.TaskPriority priority) {
        List<Task> tasks = taskService.getTasksByPriority(priority);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Sök uppgifter
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Task>> searchTasks(@RequestParam String searchTerm, Pageable pageable) {
        Page<Task> tasks = taskService.searchTasks(searchTerm, pageable);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Uppdatera uppgift
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable UUID id, @RequestBody Task updatedTask) {
        try {
            Task task = taskService.updateTask(id, updatedTask);
            return ResponseEntity.ok(task);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Markera uppgift som slutförd
     */
    @PostMapping("/{taskId}/complete")
    public ResponseEntity<Void> completeTask(@PathVariable UUID taskId) {
        try {
            taskService.completeTask(taskId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Pausa uppgift
     */
    @PostMapping("/{taskId}/pause")
    public ResponseEntity<Void> pauseTask(@PathVariable UUID taskId) {
        try {
            taskService.pauseTask(taskId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Återuppta uppgift
     */
    @PostMapping("/{taskId}/resume")
    public ResponseEntity<Void> resumeTask(@PathVariable UUID taskId) {
        try {
            taskService.resumeTask(taskId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Avbryt uppgift
     */
    @PostMapping("/{taskId}/cancel")
    public ResponseEntity<Void> cancelTask(@PathVariable UUID taskId) {
        try {
            taskService.cancelTask(taskId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ta bort uppgift
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hämta väntande uppgifter
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Task>> getPendingTasks() {
        List<Task> tasks = taskService.getPendingTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta slutförda uppgifter
     */
    @GetMapping("/completed")
    public ResponseEntity<List<Task>> getCompletedTasks() {
        List<Task> tasks = taskService.getCompletedTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta försenade uppgifter
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks() {
        List<Task> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter med hög prioritet
     */
    @GetMapping("/high-priority")
    public ResponseEntity<List<Task>> getHighPriorityTasks() {
        List<Task> tasks = taskService.getHighPriorityTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter som förfaller idag
     */
    @GetMapping("/due-today")
    public ResponseEntity<List<Task>> getTasksDueToday() {
        List<Task> tasks = taskService.getTasksDueToday();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter som förfaller denna vecka
     */
    @GetMapping("/due-this-week")
    public ResponseEntity<List<Task>> getTasksDueThisWeek() {
        List<Task> tasks = taskService.getTasksDueThisWeek();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter som förfaller denna månad
     */
    @GetMapping("/due-this-month")
    public ResponseEntity<List<Task>> getTasksDueThisMonth() {
        List<Task> tasks = taskService.getTasksDueThisMonth();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter som skapades efter ett visst datum
     */
    @GetMapping("/created-after")
    public ResponseEntity<List<Task>> getTasksCreatedAfter(@RequestParam LocalDateTime date) {
        List<Task> tasks = taskService.getTasksCreatedAfter(date);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter som slutfördes efter ett visst datum
     */
    @GetMapping("/completed-after")
    public ResponseEntity<List<Task>> getTasksCompletedAfter(@RequestParam LocalDateTime date) {
        List<Task> tasks = taskService.getTasksCompletedAfter(date);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter som skickade email
     */
    @GetMapping("/email-sent")
    public ResponseEntity<List<Task>> getTasksWithEmailSent() {
        List<Task> tasks = taskService.getTasksWithEmailSent();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Hämta uppgifter som inte skickade email
     */
    @GetMapping("/email-not-sent")
    public ResponseEntity<List<Task>> getTasksWithoutEmailSent() {
        List<Task> tasks = taskService.getTasksWithoutEmailSent();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Räkna totalt antal uppgifter
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAllTasks() {
        long count = taskService.countAllTasks();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna uppgifter efter status
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countTasksByStatus(@PathVariable Task.TaskStatus status) {
        long count = taskService.countTasksByStatus(status);
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna uppgifter efter prioritet
     */
    @GetMapping("/count/priority/{priority}")
    public ResponseEntity<Long> countTasksByPriority(@PathVariable Task.TaskPriority priority) {
        long count = taskService.countTasksByPriority(priority);
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna väntande uppgifter
     */
    @GetMapping("/count/pending")
    public ResponseEntity<Long> countPendingTasks() {
        long count = taskService.countPendingTasks();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna slutförda uppgifter
     */
    @GetMapping("/count/completed")
    public ResponseEntity<Long> countCompletedTasks() {
        long count = taskService.countCompletedTasks();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna försenade uppgifter
     */
    @GetMapping("/count/overdue")
    public ResponseEntity<Long> countOverdueTasks() {
        long count = taskService.countOverdueTasks();
        return ResponseEntity.ok(count);
    }
} 