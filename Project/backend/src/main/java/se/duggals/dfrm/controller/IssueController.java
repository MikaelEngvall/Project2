package se.duggals.dfrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.duggals.dfrm.model.Issue;
import se.duggals.dfrm.service.IssueService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
@CrossOrigin(origins = "*")
public class IssueController {

    @Autowired
    private IssueService issueService;

    /**
     * Skapa en ny felanmälan
     */
    @PostMapping
    public ResponseEntity<Issue> createIssue(@RequestBody Issue issue) {
        try {
            Issue createdIssue = issueService.createIssue(issue);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdIssue);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Hämta felanmälan efter ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Issue> getIssueById(@PathVariable UUID id) {
        return issueService.getIssueById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta felanmälan efter email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Issue> getIssueByEmail(@PathVariable String email) {
        return issueService.getIssueByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta alla felanmälningar
     */
    @GetMapping
    public ResponseEntity<List<Issue>> getAllIssues() {
        List<Issue> issues = issueService.getAllIssues();
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta felanmälningar efter status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Issue>> getIssuesByStatus(@PathVariable Issue.IssueStatus status) {
        List<Issue> issues = issueService.getIssuesByStatus(status);
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta felanmälningar efter prioritet
     */
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Issue>> getIssuesByPriority(@PathVariable Issue.IssuePriority priority) {
        List<Issue> issues = issueService.getIssuesByPriority(priority);
        return ResponseEntity.ok(issues);
    }

    /**
     * Sök felanmälningar
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Issue>> searchIssues(@RequestParam String searchTerm, Pageable pageable) {
        Page<Issue> issues = issueService.searchIssues(searchTerm, pageable);
        return ResponseEntity.ok(issues);
    }

    /**
     * Uppdatera felanmälan
     */
    @PutMapping("/{id}")
    public ResponseEntity<Issue> updateIssue(@PathVariable UUID id, @RequestBody Issue updatedIssue) {
        try {
            Issue issue = issueService.updateIssue(id, updatedIssue);
            return ResponseEntity.ok(issue);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Godkänn felanmälan
     */
    @PostMapping("/{issueId}/approve")
    public ResponseEntity<Void> approveIssue(@PathVariable UUID issueId) {
        try {
            issueService.approveIssue(issueId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Avvisa felanmälan
     */
    @PostMapping("/{issueId}/reject")
    public ResponseEntity<Void> rejectIssue(
            @PathVariable UUID issueId,
            @RequestParam String reason) {
        try {
            issueService.rejectIssue(issueId, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ta bort felanmälan
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIssue(@PathVariable UUID id) {
        try {
            issueService.deleteIssue(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hämta nya felanmälningar
     */
    @GetMapping("/new")
    public ResponseEntity<List<Issue>> getNewIssues() {
        List<Issue> issues = issueService.getNewIssues();
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta godkända felanmälningar
     */
    @GetMapping("/approved")
    public ResponseEntity<List<Issue>> getApprovedIssues() {
        List<Issue> issues = issueService.getApprovedIssues();
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta avvisade felanmälningar
     */
    @GetMapping("/rejected")
    public ResponseEntity<List<Issue>> getRejectedIssues() {
        List<Issue> issues = issueService.getRejectedIssues();
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta felanmälningar med hög prioritet
     */
    @GetMapping("/high-priority")
    public ResponseEntity<List<Issue>> getHighPriorityIssues() {
        List<Issue> issues = issueService.getHighPriorityIssues();
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta felanmälningar som skapades efter ett visst datum
     */
    @GetMapping("/created-after")
    public ResponseEntity<List<Issue>> getIssuesCreatedAfter(@RequestParam LocalDateTime date) {
        List<Issue> issues = issueService.getIssuesCreatedAfter(date);
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta felanmälningar som godkändes efter ett visst datum
     */
    @GetMapping("/approved-after")
    public ResponseEntity<List<Issue>> getIssuesApprovedAfter(@RequestParam String date) {
        List<Issue> issues = issueService.getIssuesApprovedAfter(java.time.LocalDate.parse(date));
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta felanmälningar som avvisades efter ett visst datum
     */
    @GetMapping("/rejected-after")
    public ResponseEntity<List<Issue>> getIssuesRejectedAfter(@RequestParam String date) {
        List<Issue> issues = issueService.getIssuesRejectedAfter(java.time.LocalDate.parse(date));
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta felanmälningar som skickade email
     */
    @GetMapping("/email-sent")
    public ResponseEntity<List<Issue>> getIssuesWithEmailSent() {
        List<Issue> issues = issueService.getIssuesWithEmailSent();
        return ResponseEntity.ok(issues);
    }

    /**
     * Hämta felanmälningar som inte skickade email
     */
    @GetMapping("/email-not-sent")
    public ResponseEntity<List<Issue>> getIssuesWithoutEmailSent() {
        List<Issue> issues = issueService.getIssuesWithoutEmailSent();
        return ResponseEntity.ok(issues);
    }

    /**
     * Räkna totalt antal felanmälningar
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAllIssues() {
        long count = issueService.countAllIssues();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna felanmälningar efter status
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countIssuesByStatus(@PathVariable Issue.IssueStatus status) {
        long count = issueService.countIssuesByStatus(status);
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna felanmälningar efter prioritet
     */
    @GetMapping("/count/priority/{priority}")
    public ResponseEntity<Long> countIssuesByPriority(@PathVariable Issue.IssuePriority priority) {
        long count = issueService.countIssuesByPriority(priority);
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna nya felanmälningar
     */
    @GetMapping("/count/new")
    public ResponseEntity<Long> countNewIssues() {
        long count = issueService.countNewIssues();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna godkända felanmälningar
     */
    @GetMapping("/count/approved")
    public ResponseEntity<Long> countApprovedIssues() {
        long count = issueService.countApprovedIssues();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna avvisade felanmälningar
     */
    @GetMapping("/count/rejected")
    public ResponseEntity<Long> countRejectedIssues() {
        long count = issueService.countRejectedIssues();
        return ResponseEntity.ok(count);
    }
} 