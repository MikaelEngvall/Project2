package se.duggals.dfrm.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.duggals.dfrm.model.Tenant;
import se.duggals.dfrm.service.TenantService;

@RestController
@RequestMapping("/api/tenants")
@CrossOrigin(origins = "*")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    /**
     * Skapa en ny hyresgäst
     */
    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant) {
        try {
            Tenant createdTenant = tenantService.createTenant(tenant);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Hämta hyresgäst efter ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tenant> getTenantById(@PathVariable UUID id) {
        return tenantService.getTenantById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta hyresgäst efter email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Tenant> getTenantByEmail(@PathVariable String email) {
        return tenantService.getTenantByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta alla hyresgäster
     */
    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        List<Tenant> tenants = tenantService.getAllTenants();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Hämta hyresgäster efter status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Tenant>> getTenantsByStatus(@PathVariable Tenant.TenantStatus status) {
        List<Tenant> tenants = tenantService.getTenantsByStatus(status);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Sök hyresgäster
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Tenant>> searchTenants(@RequestParam String searchTerm, Pageable pageable) {
        Page<Tenant> tenants = tenantService.searchTenants(searchTerm, pageable);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Uppdatera hyresgäst
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable UUID id, @RequestBody Tenant updatedTenant) {
        try {
            Tenant tenant = tenantService.updateTenant(id, updatedTenant);
            return ResponseEntity.ok(tenant);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Registrera inflyttning
     */
    @PostMapping("/{tenantId}/move-in")
    public ResponseEntity<Void> registerMoveIn(
            @PathVariable UUID tenantId,
            @RequestParam UUID apartmentId,
            @RequestParam LocalDate moveInDate) {
        try {
            tenantService.registerMoveIn(tenantId, apartmentId, moveInDate);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Registrera utflyttning
     */
    @PostMapping("/{tenantId}/move-out")
    public ResponseEntity<Void> registerMoveOut(
            @PathVariable UUID tenantId,
            @RequestParam LocalDate moveOutDate,
            @RequestParam String reason) {
        try {
            tenantService.registerMoveOut(tenantId, moveOutDate, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Avsluta hyreskontrakt
     */
    @PostMapping("/{tenantId}/terminate")
    public ResponseEntity<Void> terminateContract(
            @PathVariable UUID tenantId,
            @RequestParam String reason) {
        try {
            tenantService.terminateContract(tenantId, reason);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ta bort hyresgäst
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        try {
            tenantService.deleteTenant(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hämta aktiva hyresgäster
     */
    @GetMapping("/active")
    public ResponseEntity<List<Tenant>> getActiveTenants() {
        List<Tenant> tenants = tenantService.getActiveTenants();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Hämta avslutade hyresgäster
     */
    @GetMapping("/terminated")
    public ResponseEntity<List<Tenant>> getTerminatedTenants() {
        List<Tenant> tenants = tenantService.getTerminatedTenants();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Hämta hyresgäster som flyttar in efter ett visst datum
     */
    @GetMapping("/moving-in-after")
    public ResponseEntity<List<Tenant>> getTenantsMovingInAfter(@RequestParam LocalDate date) {
        List<Tenant> tenants = tenantService.getTenantsMovingInAfter(date);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Hämta hyresgäster som flyttar ut efter ett visst datum
     */
    @GetMapping("/moving-out-after")
    public ResponseEntity<List<Tenant>> getTenantsMovingOutAfter(@RequestParam LocalDate date) {
        List<Tenant> tenants = tenantService.getTenantsMovingOutAfter(date);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Hämta hyresgäster med hyra över ett visst belopp
     */
    @GetMapping("/rent-above")
    public ResponseEntity<List<Tenant>> getTenantsWithRentAbove(@RequestParam BigDecimal minRent) {
        List<Tenant> tenants = tenantService.getTenantsWithRentAbove(minRent);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Hämta hyresgäster med hyra under ett visst belopp
     */
    @GetMapping("/rent-below")
    public ResponseEntity<List<Tenant>> getTenantsWithRentBelow(@RequestParam BigDecimal maxRent) {
        List<Tenant> tenants = tenantService.getTenantsWithRentBelow(maxRent);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Räkna totalt antal hyresgäster
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAllTenants() {
        long count = tenantService.countAllTenants();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna aktiva hyresgäster
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveTenants() {
        long count = tenantService.countActiveTenants();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna avslutade hyresgäster
     */
    @GetMapping("/count/terminated")
    public ResponseEntity<Long> countTerminatedTenants() {
        long count = tenantService.countTerminatedTenants();
        return ResponseEntity.ok(count);
    }

    /**
     * Hämta hyresgäster som skapades efter ett visst datum
     */
    @GetMapping("/created-after")
    public ResponseEntity<List<Tenant>> getTenantsCreatedAfter(@RequestParam LocalDateTime date) {
        List<Tenant> tenants = tenantService.getTenantsCreatedAfter(date);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Hämta hyresgäster som avslutades under en specifik period
     */
    @GetMapping("/terminated-between")
    public ResponseEntity<List<Tenant>> getTenantsTerminatedBetween(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        List<Tenant> tenants = tenantService.getTenantsTerminatedBetween(startDate, endDate);
        return ResponseEntity.ok(tenants);
    }
} 