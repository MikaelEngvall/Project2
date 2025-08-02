package se.duggals.dfrm.service;

import java.math.BigDecimal;
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
import se.duggals.dfrm.model.Tenant;
import se.duggals.dfrm.repository.ApartmentRepository;
import se.duggals.dfrm.repository.TenantRepository;

@Service
@Transactional
public class TenantService {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private ApartmentRepository apartmentRepository;

    /**
     * Skapa en ny hyresgäst
     */
    public Tenant createTenant(Tenant tenant) {
        // Validera att email inte redan finns
        if (tenantRepository.findByEmail(tenant.getEmail()).isPresent()) {
            throw new IllegalArgumentException("En hyresgäst med denna email finns redan");
        }

        // Validera att lägenheten finns och är ledig
        if (tenant.getApartment() != null) {
            Apartment apartment = apartmentRepository.findById(tenant.getApartment().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Lägenhet hittades inte"));

            if (apartment.getOccupied()) {
                throw new IllegalArgumentException("Lägenheten är redan upptagen");
            }

            // Markera lägenheten som upptagen
            apartment.setOccupied(true);
            apartmentRepository.save(apartment);
        }

        return tenantRepository.save(tenant);
    }

    /**
     * Hämta hyresgäst efter ID
     */
    public Optional<Tenant> getTenantById(UUID id) {
        return tenantRepository.findById(id);
    }

    /**
     * Hämta hyresgäst efter email
     */
    public Optional<Tenant> getTenantByEmail(String email) {
        return tenantRepository.findByEmail(email);
    }

    /**
     * Hämta alla hyresgäster
     */
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    /**
     * Hämta hyresgäster efter status
     */
    public List<Tenant> getTenantsByStatus(Tenant.TenantStatus status) {
        return tenantRepository.findByStatus(status);
    }

    /**
     * Hämta hyresgäster för en specifik lägenhet
     */
    public List<Tenant> getTenantsByApartment(Apartment apartment) {
        return tenantRepository.findByApartment(apartment);
    }

    /**
     * Sök hyresgäster
     */
    public Page<Tenant> searchTenants(String searchTerm, Pageable pageable) {
        return tenantRepository.searchTenants(searchTerm, pageable);
    }

    /**
     * Uppdatera hyresgäst
     */
    public Tenant updateTenant(UUID id, Tenant updatedTenant) {
        Tenant existingTenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hyresgäst hittades inte"));

        // Uppdatera fält
        existingTenant.setFirstName(updatedTenant.getFirstName());
        existingTenant.setLastName(updatedTenant.getLastName());
        existingTenant.setEmail(updatedTenant.getEmail());
        existingTenant.setPhone(updatedTenant.getPhone());
        existingTenant.setPersonalNumber(updatedTenant.getPersonalNumber());
        existingTenant.setMonthlyRent(updatedTenant.getMonthlyRent());
        existingTenant.setStatus(updatedTenant.getStatus());
        existingTenant.setTerminationReason(updatedTenant.getTerminationReason());
        existingTenant.setTerminationDate(updatedTenant.getTerminationDate());

        return tenantRepository.save(existingTenant);
    }

    /**
     * Registrera inflyttning
     */
    public void registerMoveIn(UUID tenantId, UUID apartmentId, LocalDate moveInDate) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Hyresgäst hittades inte"));

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new IllegalArgumentException("Lägenhet hittades inte"));

        if (apartment.getOccupied()) {
            throw new IllegalArgumentException("Lägenheten är redan upptagen");
        }

        tenant.setApartment(apartment);
        tenant.setMoveInDate(moveInDate);
        tenant.setStatus(Tenant.TenantStatus.ACTIVE);

        apartment.setOccupied(true);
        apartmentRepository.save(apartment);

        tenantRepository.save(tenant);
    }

    /**
     * Registrera utflyttning
     */
    public void registerMoveOut(UUID tenantId, LocalDate moveOutDate, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Hyresgäst hittades inte"));

        if (tenant.getApartment() != null) {
            Apartment apartment = tenant.getApartment();
            apartment.setOccupied(false);
            apartmentRepository.save(apartment);
        }

        tenant.setMoveOutDate(moveOutDate);
        tenant.setStatus(Tenant.TenantStatus.TERMINATED);
        tenant.setTerminationReason(reason);
        tenant.setTerminationDate(LocalDate.now());

        tenantRepository.save(tenant);
    }

    /**
     * Avsluta hyreskontrakt
     */
    public void terminateContract(UUID tenantId, String reason) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Hyresgäst hittades inte"));

        tenant.setStatus(Tenant.TenantStatus.TERMINATED);
        tenant.setTerminationReason(reason);
        tenant.setTerminationDate(LocalDate.now());

        tenantRepository.save(tenant);
    }

    /**
     * Ta bort hyresgäst (soft delete)
     */
    public void deleteTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hyresgäst hittades inte"));

        // Frigör lägenheten om den är upptagen
        if (tenant.getApartment() != null && tenant.getApartment().getOccupied()) {
            Apartment apartment = tenant.getApartment();
            apartment.setOccupied(false);
            apartmentRepository.save(apartment);
        }

        tenantRepository.delete(tenant);
    }

    /**
     * Hämta aktiva hyresgäster
     */
    public List<Tenant> getActiveTenants() {
        return tenantRepository.findByStatus(Tenant.TenantStatus.ACTIVE);
    }

    /**
     * Hämta avslutade hyresgäster
     */
    public List<Tenant> getTerminatedTenants() {
        return tenantRepository.findByStatus(Tenant.TenantStatus.TERMINATED);
    }

    /**
     * Hämta hyresgäster som flyttar in efter ett visst datum
     */
    public List<Tenant> getTenantsMovingInAfter(LocalDate date) {
        return tenantRepository.findByMoveInDateAfter(date);
    }

    /**
     * Hämta hyresgäster som flyttar ut efter ett visst datum
     */
    public List<Tenant> getTenantsMovingOutAfter(LocalDate date) {
        return tenantRepository.findByMoveOutDateAfter(date);
    }

    /**
     * Hämta hyresgäster med hyra över ett visst belopp
     */
    public List<Tenant> getTenantsWithRentAbove(BigDecimal minRent) {
        return tenantRepository.findByMonthlyRentGreaterThanEqual(minRent);
    }

    /**
     * Hämta hyresgäster med hyra under ett visst belopp
     */
    public List<Tenant> getTenantsWithRentBelow(BigDecimal maxRent) {
        return tenantRepository.findByMonthlyRentBetween(BigDecimal.ZERO, maxRent);
    }

    /**
     * Räkna totalt antal hyresgäster
     */
    public long countAllTenants() {
        return tenantRepository.count();
    }

    /**
     * Räkna aktiva hyresgäster
     */
    public long countActiveTenants() {
        return tenantRepository.countByStatus(Tenant.TenantStatus.ACTIVE);
    }

    /**
     * Räkna avslutade hyresgäster
     */
    public long countTerminatedTenants() {
        return tenantRepository.countByStatus(Tenant.TenantStatus.TERMINATED);
    }

    /**
     * Hämta hyresgäster som skapades efter ett visst datum
     */
    public List<Tenant> getTenantsCreatedAfter(LocalDateTime date) {
        return tenantRepository.findByCreatedAtAfter(date);
    }

    /**
     * Hämta hyresgäster som avslutades under en specifik period
     */
    public List<Tenant> getTenantsTerminatedBetween(LocalDate startDate, LocalDate endDate) {
        return tenantRepository.findTenantsByTerminationPeriod(startDate, endDate);
    }
} 