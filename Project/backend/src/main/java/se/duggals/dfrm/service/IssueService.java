package se.duggals.dfrm.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.duggals.dfrm.model.Issue;
import se.duggals.dfrm.repository.IssueRepository;

@Service
@Transactional
public class IssueService {

    @Autowired
    private IssueRepository issueRepository;

    /**
     * Skapa en ny felanmälan
     */
    public Issue createIssue(Issue issue) {
        // Sätt standardvärden
        if (issue.getStatus() == null) {
            issue.setStatus(Issue.IssueStatus.NEW);
        }
        if (issue.getPriority() == null) {
            issue.setPriority(Issue.IssuePriority.MEDIUM);
        }

        return issueRepository.save(issue);
    }

    /**
     * Hämta felanmälan efter ID
     */
    public Optional<Issue> getIssueById(UUID id) {
        return issueRepository.findById(id);
    }

    /**
     * Hämta felanmälan efter email
     */
    public Optional<Issue> getIssueByEmail(String email) {
        return issueRepository.findByEmail(email);
    }

    /**
     * Hämta alla felanmälningar
     */
    public List<Issue> getAllIssues() {
        return issueRepository.findAll();
    }

    /**
     * Hämta felanmälningar efter status
     */
    public List<Issue> getIssuesByStatus(Issue.IssueStatus status) {
        return issueRepository.findByStatus(status);
    }

    /**
     * Hämta felanmälningar efter prioritet
     */
    public List<Issue> getIssuesByPriority(Issue.IssuePriority priority) {
        return issueRepository.findByPriority(priority);
    }

    /**
     * Sök felanmälningar
     */
    public Page<Issue> searchIssues(String searchTerm, Pageable pageable) {
        return issueRepository.searchIssues(searchTerm, pageable);
    }

    /**
     * Uppdatera felanmälan
     */
    public Issue updateIssue(UUID id, Issue updatedIssue) {
        Issue existingIssue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Felanmälan hittades inte"));

        // Uppdatera fält
        existingIssue.setFirstName(updatedIssue.getFirstName());
        existingIssue.setLastName(updatedIssue.getLastName());
        existingIssue.setEmail(updatedIssue.getEmail());
        existingIssue.setPhone(updatedIssue.getPhone());
        existingIssue.setSubject(updatedIssue.getSubject());
        existingIssue.setDescription(updatedIssue.getDescription());
        existingIssue.setPriority(updatedIssue.getPriority());
        existingIssue.setStatus(updatedIssue.getStatus());
        existingIssue.setApartment(updatedIssue.getApartment());

        return issueRepository.save(existingIssue);
    }

    /**
     * Godkänn felanmälan
     */
    public void approveIssue(UUID id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Felanmälan hittades inte"));

        issue.setStatus(Issue.IssueStatus.APPROVED);
        issue.setApprovedDate(LocalDate.now());
        issueRepository.save(issue);
    }

    /**
     * Avvisa felanmälan
     */
    public void rejectIssue(UUID id, String reason) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Felanmälan hittades inte"));

        issue.setStatus(Issue.IssueStatus.REJECTED);
        issue.setRejectedDate(LocalDate.now());
        issue.setRejectionReason(reason);
        issueRepository.save(issue);
    }

    /**
     * Ta bort felanmälan (soft delete)
     */
    public void deleteIssue(UUID id) {
        Issue issue = issueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Felanmälan hittades inte"));

        issueRepository.delete(issue);
    }

    /**
     * Hämta nya felanmälningar
     */
    public List<Issue> getNewIssues() {
        return issueRepository.findByStatus(Issue.IssueStatus.NEW);
    }

    /**
     * Hämta godkända felanmälningar
     */
    public List<Issue> getApprovedIssues() {
        return issueRepository.findByStatusAndApproved(Issue.IssueStatus.APPROVED);
    }

    /**
     * Hämta avvisade felanmälningar
     */
    public List<Issue> getRejectedIssues() {
        return issueRepository.findByStatusAndRejected(Issue.IssueStatus.REJECTED);
    }

    /**
     * Hämta felanmälningar med hög prioritet
     */
    public List<Issue> getHighPriorityIssues() {
        return issueRepository.findHighPriorityIssuesSorted();
    }

    /**
     * Hämta felanmälningar som skapades efter ett visst datum
     */
    public List<Issue> getIssuesCreatedAfter(LocalDateTime date) {
        return issueRepository.findByCreatedAtAfter(date);
    }

    /**
     * Hämta felanmälningar som godkändes efter ett visst datum
     */
    public List<Issue> getIssuesApprovedAfter(LocalDate date) {
        return issueRepository.findByApprovedDateAfter(date);
    }

    /**
     * Hämta felanmälningar som avvisades efter ett visst datum
     */
    public List<Issue> getIssuesRejectedAfter(LocalDate date) {
        return issueRepository.findByRejectedDateAfter(date);
    }

    /**
     * Hämta felanmälningar som skickade email
     */
    public List<Issue> getIssuesWithEmailSent() {
        return issueRepository.findByEmailSentTrue();
    }

    /**
     * Hämta felanmälningar som inte skickade email
     */
    public List<Issue> getIssuesWithoutEmailSent() {
        return issueRepository.findIssuesWithoutEmailSent();
    }

    /**
     * Räkna totalt antal felanmälningar
     */
    public long countAllIssues() {
        return issueRepository.count();
    }

    /**
     * Räkna felanmälningar efter status
     */
    public long countIssuesByStatus(Issue.IssueStatus status) {
        return issueRepository.countByStatus(status);
    }

    /**
     * Räkna felanmälningar efter prioritet
     */
    public long countIssuesByPriority(Issue.IssuePriority priority) {
        return issueRepository.countByPriority(priority);
    }

    /**
     * Räkna nya felanmälningar
     */
    public long countNewIssues() {
        return issueRepository.countByStatus(Issue.IssueStatus.NEW);
    }

    /**
     * Räkna godkända felanmälningar
     */
    public long countApprovedIssues() {
        return issueRepository.countByStatus(Issue.IssueStatus.APPROVED);
    }

    /**
     * Räkna avvisade felanmälningar
     */
    public long countRejectedIssues() {
        return issueRepository.countByStatus(Issue.IssueStatus.REJECTED);
    }
} 