package se.duggals.dfrm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.duggals.dfrm.model.Interest;
import se.duggals.dfrm.service.InterestService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/interests")
@CrossOrigin(origins = "*")
public class InterestController {

    @Autowired
    private InterestService interestService;

    /**
     * Skapa en ny intresseanmälan
     */
    @PostMapping
    public ResponseEntity<Interest> createInterest(@RequestBody Interest interest) {
        try {
            Interest createdInterest = interestService.createInterest(interest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdInterest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Hämta intresseanmälan efter ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Interest> getInterestById(@PathVariable UUID id) {
        return interestService.getInterestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta intresseanmälan efter email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Interest> getInterestByEmail(@PathVariable String email) {
        return interestService.getInterestByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta alla intresseanmälningar
     */
    @GetMapping
    public ResponseEntity<List<Interest>> getAllInterests() {
        List<Interest> interests = interestService.getAllInterests();
        return ResponseEntity.ok(interests);
    }

    /**
     * Hämta intresseanmälningar efter status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Interest>> getInterestsByStatus(@PathVariable Interest.InterestStatus status) {
        List<Interest> interests = interestService.getInterestsByStatus(status);
        return ResponseEntity.ok(interests);
    }

    /**
     * Sök intresseanmälningar
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Interest>> searchInterests(@RequestParam String searchTerm, Pageable pageable) {
        Page<Interest> interests = interestService.searchInterests(searchTerm, pageable);
        return ResponseEntity.ok(interests);
    }

    /**
     * Uppdatera intresseanmälan
     */
    @PutMapping("/{id}")
    public ResponseEntity<Interest> updateInterest(@PathVariable UUID id, @RequestBody Interest updatedInterest) {
        try {
            Interest interest = interestService.updateInterest(id, updatedInterest);
            return ResponseEntity.ok(interest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Boka visning
     */
    @PostMapping("/{interestId}/schedule-viewing")
    public ResponseEntity<Void> scheduleViewing(
            @PathVariable UUID interestId,
            @RequestParam LocalDate viewingDate,
            @RequestParam String viewingTime) {
        try {
            interestService.scheduleViewing(interestId, viewingDate, viewingTime);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Bekräfta visning
     */
    @PostMapping("/{interestId}/confirm-viewing")
    public ResponseEntity<Void> confirmViewing(@PathVariable UUID interestId) {
        try {
            interestService.confirmViewing(interestId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Avboka visning
     */
    @PostMapping("/{interestId}/cancel-viewing")
    public ResponseEntity<Void> cancelViewing(@PathVariable UUID interestId) {
        try {
            interestService.cancelViewing(interestId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Avvisa intresseanmälan
     */
    @PostMapping("/{interestId}/reject")
    public ResponseEntity<Void> rejectInterest(
            @PathVariable UUID interestId,
            @RequestParam String reason) {
        try {
            interestService.rejectInterest(interestId, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ta bort intresseanmälan
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterest(@PathVariable UUID id) {
        try {
            interestService.deleteInterest(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hämta väntande intresseanmälningar
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Interest>> getPendingInterests() {
        List<Interest> interests = interestService.getPendingInterests();
        return ResponseEntity.ok(interests);
    }

    /**
     * Hämta bekräftade intresseanmälningar
     */
    @GetMapping("/confirmed")
    public ResponseEntity<List<Interest>> getConfirmedInterests() {
        List<Interest> interests = interestService.getConfirmedInterests();
        return ResponseEntity.ok(interests);
    }

    /**
     * Hämta avvisade intresseanmälningar
     */
    @GetMapping("/rejected")
    public ResponseEntity<List<Interest>> getRejectedInterests() {
        List<Interest> interests = interestService.getRejectedInterests();
        return ResponseEntity.ok(interests);
    }

    /**
     * Hämta intresseanmälningar som skapades efter ett visst datum
     */
    @GetMapping("/created-after")
    public ResponseEntity<List<Interest>> getInterestsCreatedAfter(@RequestParam LocalDateTime date) {
        List<Interest> interests = interestService.getInterestsCreatedAfter(date);
        return ResponseEntity.ok(interests);
    }

    /**
     * Hämta intresseanmälningar med visning efter ett visst datum
     */
    @GetMapping("/viewing-after")
    public ResponseEntity<List<Interest>> getInterestsWithViewingAfter(@RequestParam LocalDate date) {
        List<Interest> interests = interestService.getInterestsWithViewingAfter(date);
        return ResponseEntity.ok(interests);
    }

    /**
     * Hämta intresseanmälningar som skickade email
     */
    @GetMapping("/email-sent")
    public ResponseEntity<List<Interest>> getInterestsWithEmailSent() {
        List<Interest> interests = interestService.getInterestsWithEmailSent();
        return ResponseEntity.ok(interests);
    }

    /**
     * Hämta intresseanmälningar som inte skickade email
     */
    @GetMapping("/email-not-sent")
    public ResponseEntity<List<Interest>> getInterestsWithoutEmailSent() {
        List<Interest> interests = interestService.getInterestsWithoutEmailSent();
        return ResponseEntity.ok(interests);
    }

    /**
     * Räkna totalt antal intresseanmälningar
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAllInterests() {
        long count = interestService.countAllInterests();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna intresseanmälningar efter status
     */
    @GetMapping("/count/status/{status}")
    public ResponseEntity<Long> countInterestsByStatus(@PathVariable Interest.InterestStatus status) {
        long count = interestService.countInterestsByStatus(status);
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna väntande intresseanmälningar
     */
    @GetMapping("/count/pending")
    public ResponseEntity<Long> countPendingInterests() {
        long count = interestService.countPendingInterests();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna bekräftade intresseanmälningar
     */
    @GetMapping("/count/confirmed")
    public ResponseEntity<Long> countConfirmedInterests() {
        long count = interestService.countConfirmedInterests();
        return ResponseEntity.ok(count);
    }
} 