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

import se.duggals.dfrm.model.Apartment;
import se.duggals.dfrm.model.Interest;
import se.duggals.dfrm.repository.ApartmentRepository;
import se.duggals.dfrm.repository.InterestRepository;

@Service
@Transactional
public class InterestService {

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    /**
     * Skapa en ny intresseanmälan
     */
    public Interest createInterest(Interest interest) {
        // Validera att lägenheten finns
        if (interest.getApartment() != null) {
            Apartment apartment = apartmentRepository.findById(interest.getApartment().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Lägenhet hittades inte"));
            interest.setApartment(apartment);
        }

        // Sätt standardvärden
        if (interest.getStatus() == null) {
            interest.setStatus(Interest.InterestStatus.PENDING);
        }

        return interestRepository.save(interest);
    }

    /**
     * Hämta intresseanmälan efter ID
     */
    public Optional<Interest> getInterestById(UUID id) {
        return interestRepository.findById(id);
    }

    /**
     * Hämta intresseanmälan efter email
     */
    public Optional<Interest> getInterestByEmail(String email) {
        return interestRepository.findByEmail(email);
    }

    /**
     * Hämta alla intresseanmälningar
     */
    public List<Interest> getAllInterests() {
        return interestRepository.findAll();
    }

    /**
     * Hämta intresseanmälningar efter status
     */
    public List<Interest> getInterestsByStatus(Interest.InterestStatus status) {
        return interestRepository.findByStatus(status);
    }

    /**
     * Hämta intresseanmälningar för en specifik lägenhet
     */
    public List<Interest> getInterestsByApartment(Apartment apartment) {
        return interestRepository.findByApartment(apartment);
    }

    /**
     * Sök intresseanmälningar
     */
    public Page<Interest> searchInterests(String searchTerm, Pageable pageable) {
        return interestRepository.searchInterests(searchTerm, pageable);
    }

    /**
     * Uppdatera intresseanmälan
     */
    public Interest updateInterest(UUID id, Interest updatedInterest) {
        Interest existingInterest = interestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Intresseanmälan hittades inte"));

        // Uppdatera fält
        existingInterest.setFirstName(updatedInterest.getFirstName());
        existingInterest.setLastName(updatedInterest.getLastName());
        existingInterest.setEmail(updatedInterest.getEmail());
        existingInterest.setPhone(updatedInterest.getPhone());
        existingInterest.setStatus(updatedInterest.getStatus());
        existingInterest.setViewingDate(updatedInterest.getViewingDate());
        existingInterest.setViewingTime(updatedInterest.getViewingTime());
        existingInterest.setViewingConfirmed(updatedInterest.getViewingConfirmed());
        existingInterest.setViewingEmailSent(updatedInterest.getViewingEmailSent());
        existingInterest.setViewingEmailSentDate(updatedInterest.getViewingEmailSentDate());
        existingInterest.setNotes(updatedInterest.getNotes());

        return interestRepository.save(existingInterest);
    }

    /**
     * Boka visning
     */
    public void scheduleViewing(UUID interestId, LocalDate viewingDate, String viewingTime) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new IllegalArgumentException("Intresseanmälan hittades inte"));

        interest.setViewingDate(viewingDate);
        interest.setViewingTime(viewingTime);
        interest.setStatus(Interest.InterestStatus.PENDING);
        interest.setViewingConfirmed(false);

        interestRepository.save(interest);
    }

    /**
     * Bekräfta visning
     */
    public void confirmViewing(UUID interestId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new IllegalArgumentException("Intresseanmälan hittades inte"));

        interest.setViewingConfirmed(true);
        interest.setStatus(Interest.InterestStatus.CONFIRMED);

        interestRepository.save(interest);
    }

    /**
     * Avboka visning
     */
    public void cancelViewing(UUID interestId) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new IllegalArgumentException("Intresseanmälan hittades inte"));

        interest.setViewingDate(null);
        interest.setViewingTime(null);
        interest.setViewingConfirmed(false);
        interest.setStatus(Interest.InterestStatus.PENDING);

        interestRepository.save(interest);
    }

    /**
     * Markera som avvisad
     */
    public void rejectInterest(UUID interestId, String reason) {
        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new IllegalArgumentException("Intresseanmälan hittades inte"));

        interest.setStatus(Interest.InterestStatus.REJECTED);
        interest.setNotes(reason);

        interestRepository.save(interest);
    }

    /**
     * Ta bort intresseanmälan (soft delete)
     */
    public void deleteInterest(UUID id) {
        Interest interest = interestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Intresseanmälan hittades inte"));

        interestRepository.delete(interest);
    }

    /**
     * Hämta väntande intresseanmälningar
     */
    public List<Interest> getPendingInterests() {
        return interestRepository.findByStatus(Interest.InterestStatus.PENDING);
    }

    /**
     * Hämta bekräftade intresseanmälningar
     */
    public List<Interest> getConfirmedInterests() {
        return interestRepository.findByStatus(Interest.InterestStatus.CONFIRMED);
    }

    /**
     * Hämta avvisade intresseanmälningar
     */
    public List<Interest> getRejectedInterests() {
        return interestRepository.findByStatus(Interest.InterestStatus.REJECTED);
    }

    /**
     * Hämta intresseanmälningar som skapades efter ett visst datum
     */
    public List<Interest> getInterestsCreatedAfter(LocalDateTime date) {
        return interestRepository.findByCreatedAtAfter(date);
    }

    /**
     * Hämta intresseanmälningar med visning efter ett visst datum
     */
    public List<Interest> getInterestsWithViewingAfter(LocalDate date) {
        return interestRepository.findByViewingDateAfter(date);
    }

    /**
     * Hämta intresseanmälningar som skickade email
     */
    public List<Interest> getInterestsWithEmailSent() {
        return interestRepository.findByViewingEmailSentTrue();
    }

    /**
     * Hämta intresseanmälningar som inte skickade email
     */
    public List<Interest> getInterestsWithoutEmailSent() {
        return interestRepository.findInterestsWithoutEmailSent();
    }

    /**
     * Räkna totalt antal intresseanmälningar
     */
    public long countAllInterests() {
        return interestRepository.count();
    }

    /**
     * Räkna intresseanmälningar efter status
     */
    public long countInterestsByStatus(Interest.InterestStatus status) {
        return interestRepository.countByStatus(status);
    }

    /**
     * Räkna väntande intresseanmälningar
     */
    public long countPendingInterests() {
        return interestRepository.countByStatus(Interest.InterestStatus.PENDING);
    }

    /**
     * Räkna bekräftade intresseanmälningar
     */
    public long countConfirmedInterests() {
        return interestRepository.countByStatus(Interest.InterestStatus.CONFIRMED);
    }

} 