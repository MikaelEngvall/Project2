package se.duggals.dfrm.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.duggals.dfrm.model.Key;
import se.duggals.dfrm.service.KeyService;

/**
 * REST Controller för Key entity
 * 
 * @author Duggals Fastigheter
 * @version 4.1.0
 * @since 2024-12-19
 */
@RestController
@RequestMapping("/api/keys")
public class KeyController {

    @Autowired
    private KeyService keyService;

    /**
     * Skapa en ny nyckel
     */
    @PostMapping
    public ResponseEntity<Key> createKey(@RequestBody Key key) {
        try {
            Key createdKey = keyService.createKey(key);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdKey);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Hämta nyckel efter ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Key> getKeyById(@PathVariable UUID id) {
        return keyService.getKeyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta nyckel efter serie, nummer och kopia
     */
    @GetMapping("/search")
    public ResponseEntity<Key> getKeyBySeriesNumberCopy(
            @RequestParam String series,
            @RequestParam String number,
            @RequestParam String copy) {
        return keyService.getKeyBySeriesNumberCopy(series, number, copy)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta alla aktiva nycklar
     */
    @GetMapping("/active")
    public ResponseEntity<List<Key>> getAllActiveKeys() {
        List<Key> keys = keyService.getAllActiveKeys();
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar efter typ
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Key>> getKeysByType(@PathVariable Key.KeyType type) {
        List<Key> keys = keyService.getKeysByType(type);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta aktiva nycklar efter typ
     */
    @GetMapping("/type/{type}/active")
    public ResponseEntity<List<Key>> getActiveKeysByType(@PathVariable Key.KeyType type) {
        List<Key> keys = keyService.getActiveKeysByType(type);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar för specifik lägenhet
     */
    @GetMapping("/apartment/{apartmentId}")
    public ResponseEntity<List<Key>> getKeysByApartment(@PathVariable UUID apartmentId) {
        // Här skulle vi normalt hämta Apartment från ApartmentService
        // För nu skapar vi en placeholder
        se.duggals.dfrm.model.Apartment apartment = new se.duggals.dfrm.model.Apartment();
        apartment.setId(apartmentId);
        
        List<Key> keys = keyService.getKeysByApartment(apartment);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta aktiva nycklar för specifik lägenhet
     */
    @GetMapping("/apartment/{apartmentId}/active")
    public ResponseEntity<List<Key>> getActiveKeysByApartment(@PathVariable UUID apartmentId) {
        se.duggals.dfrm.model.Apartment apartment = new se.duggals.dfrm.model.Apartment();
        apartment.setId(apartmentId);
        
        List<Key> keys = keyService.getActiveKeysByApartment(apartment);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar för specifik lägenhet och typ
     */
    @GetMapping("/apartment/{apartmentId}/type/{type}")
    public ResponseEntity<List<Key>> getKeysByApartmentAndType(
            @PathVariable UUID apartmentId,
            @PathVariable Key.KeyType type) {
        se.duggals.dfrm.model.Apartment apartment = new se.duggals.dfrm.model.Apartment();
        apartment.setId(apartmentId);
        
        List<Key> keys = keyService.getKeysByApartmentAndType(apartment, type);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar som är utlånade till hyresgäst
     */
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<Key>> getKeysByTenant(@PathVariable UUID tenantId) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getKeysByTenant(tenant);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta aktiva nycklar som är utlånade till hyresgäst
     */
    @GetMapping("/tenant/{tenantId}/active")
    public ResponseEntity<List<Key>> getActiveKeysByTenant(@PathVariable UUID tenantId) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getActiveKeysByTenant(tenant);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar av specifik typ som är utlånade till hyresgäst
     */
    @GetMapping("/tenant/{tenantId}/type/{type}")
    public ResponseEntity<List<Key>> getKeysByTenantAndType(
            @PathVariable UUID tenantId,
            @PathVariable Key.KeyType type) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getKeysByTenantAndType(tenant, type);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta oanvända nycklar
     */
    @GetMapping("/unassigned")
    public ResponseEntity<List<Key>> getUnassignedKeys() {
        List<Key> keys = keyService.getUnassignedKeys();
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta oanvända nycklar av specifik typ
     */
    @GetMapping("/unassigned/type/{type}")
    public ResponseEntity<List<Key>> getUnassignedKeysByType(@PathVariable Key.KeyType type) {
        List<Key> keys = keyService.getUnassignedKeysByType(type);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta oanvända nycklar för specifik lägenhet
     */
    @GetMapping("/unassigned/apartment/{apartmentId}")
    public ResponseEntity<List<Key>> getUnassignedKeysByApartment(@PathVariable UUID apartmentId) {
        se.duggals.dfrm.model.Apartment apartment = new se.duggals.dfrm.model.Apartment();
        apartment.setId(apartmentId);
        
        List<Key> keys = keyService.getUnassignedKeysByApartment(apartment);
        return ResponseEntity.ok(keys);
    }

    /**
     * Sök nycklar
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Key>> searchKeys(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<Key> keys = keyService.searchKeys(searchTerm, pageable);
        return ResponseEntity.ok(keys);
    }

    /**
     * Uppdatera nyckel
     */
    @PutMapping("/{id}")
    public ResponseEntity<Key> updateKey(@PathVariable UUID id, @RequestBody Key updatedKey) {
        try {
            Key key = keyService.updateKey(id, updatedKey);
            return ResponseEntity.ok(key);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Låna ut nyckel till hyresgäst
     */
    @PostMapping("/{keyId}/assign/{tenantId}")
    public ResponseEntity<Key> assignKeyToTenant(
            @PathVariable UUID keyId,
            @PathVariable UUID tenantId) {
        try {
            Key key = keyService.assignKeyToTenant(keyId, tenantId);
            return ResponseEntity.ok(key);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Returnera nyckel från hyresgäst
     */
    @PostMapping("/{keyId}/return")
    public ResponseEntity<Key> returnKeyFromTenant(@PathVariable UUID keyId) {
        try {
            Key key = keyService.returnKeyFromTenant(keyId);
            return ResponseEntity.ok(key);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Inaktivera nyckel
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateKey(@PathVariable UUID id) {
        try {
            keyService.deactivateKey(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ta bort nyckel
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKey(@PathVariable UUID id) {
        try {
            keyService.deleteKey(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hämta huvudnycklar
     */
    @GetMapping("/master")
    public ResponseEntity<List<Key>> getMasterKeys() {
        List<Key> keys = keyService.getMasterKeys();
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar som behöver returneras
     */
    @GetMapping("/needing-return")
    public ResponseEntity<List<Key>> getKeysNeedingReturn() {
        List<Key> keys = keyService.getKeysNeedingReturn();
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar som behöver returneras snart
     */
    @GetMapping("/needing-return-soon")
    public ResponseEntity<List<Key>> getKeysNeedingReturnSoon(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        java.time.LocalDate start = java.time.LocalDate.parse(startDate);
        java.time.LocalDate end = java.time.LocalDate.parse(endDate);
        
        List<Key> keys = keyService.getKeysNeedingReturnSoon(start, end);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar för lägenheter som är lediga
     */
    @GetMapping("/vacant-apartments")
    public ResponseEntity<List<Key>> getKeysForVacantApartments() {
        List<Key> keys = keyService.getKeysForVacantApartments();
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar för lägenheter som är upptagna
     */
    @GetMapping("/occupied-apartments")
    public ResponseEntity<List<Key>> getKeysForOccupiedApartments() {
        List<Key> keys = keyService.getKeysForOccupiedApartments();
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar skapade under specifik period
     */
    @GetMapping("/created-between")
    public ResponseEntity<List<Key>> getKeysByCreationPeriod(
            @RequestParam String startDateTime,
            @RequestParam String endDateTime) {
        LocalDateTime start = LocalDateTime.parse(startDateTime);
        LocalDateTime end = LocalDateTime.parse(endDateTime);
        
        List<Key> keys = keyService.getKeysByCreationPeriod(start, end);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar som lånades ut under specifik period
     */
    @GetMapping("/assigned-between")
    public ResponseEntity<List<Key>> getKeysAssignedInPeriod(
            @RequestParam String startDateTime,
            @RequestParam String endDateTime) {
        LocalDateTime start = LocalDateTime.parse(startDateTime);
        LocalDateTime end = LocalDateTime.parse(endDateTime);
        
        List<Key> keys = keyService.getKeysAssignedInPeriod(start, end);
        return ResponseEntity.ok(keys);
    }

    /**
     * Räkna nycklar per typ
     */
    @GetMapping("/count/type/{type}")
    public ResponseEntity<Long> countKeysByType(@PathVariable Key.KeyType type) {
        long count = keyService.countKeysByType(type);
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna aktiva nycklar per typ
     */
    @GetMapping("/count/type/{type}/active")
    public ResponseEntity<Long> countActiveKeysByType(@PathVariable Key.KeyType type) {
        long count = keyService.countActiveKeysByType(type);
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna oanvända nycklar
     */
    @GetMapping("/count/unassigned")
    public ResponseEntity<Long> countUnassignedKeys() {
        long count = keyService.countUnassignedKeys();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna oanvända nycklar per typ
     */
    @GetMapping("/count/unassigned/type/{type}")
    public ResponseEntity<Long> countUnassignedKeysByType(@PathVariable Key.KeyType type) {
        long count = keyService.countUnassignedKeysByType(type);
        return ResponseEntity.ok(count);
    }

    /**
     * Hämta nycklar skapade efter datum
     */
    @GetMapping("/created-after")
    public ResponseEntity<List<Key>> getKeysCreatedAfter(@RequestParam String dateTime) {
        LocalDateTime date = LocalDateTime.parse(dateTime);
        List<Key> keys = keyService.getKeysCreatedAfter(date);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar uppdaterade efter datum
     */
    @GetMapping("/updated-after")
    public ResponseEntity<List<Key>> getKeysUpdatedAfter(@RequestParam String dateTime) {
        LocalDateTime date = LocalDateTime.parse(dateTime);
        List<Key> keys = keyService.getKeysUpdatedAfter(date);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta senaste nycklar
     */
    @GetMapping("/latest")
    public ResponseEntity<Page<Key>> getLatestKeys(Pageable pageable) {
        Page<Key> keys = keyService.getLatestKeys(pageable);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar för specifik serie
     */
    @GetMapping("/series/{series}")
    public ResponseEntity<List<Key>> getKeysBySeries(@PathVariable String series) {
        List<Key> keys = keyService.getKeysBySeries(series);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar för specifikt nummer
     */
    @GetMapping("/number/{number}")
    public ResponseEntity<List<Key>> getKeysByNumber(@PathVariable String number) {
        List<Key> keys = keyService.getKeysByNumber(number);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar för specifik kopia
     */
    @GetMapping("/copy/{copy}")
    public ResponseEntity<List<Key>> getKeysByCopy(@PathVariable String copy) {
        List<Key> keys = keyService.getKeysByCopy(copy);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta nycklar med specifika anteckningar
     */
    @GetMapping("/notes")
    public ResponseEntity<List<Key>> getKeysByNotesContaining(@RequestParam String searchTerm) {
        List<Key> keys = keyService.getKeysByNotesContaining(searchTerm);
        return ResponseEntity.ok(keys);
    }

    /**
     * Kontrollera om nyckel finns
     */
    @GetMapping("/exists")
    public ResponseEntity<Boolean> keyExists(
            @RequestParam String series,
            @RequestParam String number,
            @RequestParam String copy) {
        boolean exists = keyService.keyExists(series, number, copy);
        return ResponseEntity.ok(exists);
    }

    /**
     * Hämta alla nycklar för en hyresgäst (alla typer)
     */
    @GetMapping("/tenant/{tenantId}/all")
    public ResponseEntity<List<Key>> getAllKeysForTenant(@PathVariable UUID tenantId) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getAllKeysForTenant(tenant);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta lägenhetsnycklar för en hyresgäst
     */
    @GetMapping("/tenant/{tenantId}/apartment")
    public ResponseEntity<List<Key>> getApartmentKeysForTenant(@PathVariable UUID tenantId) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getApartmentKeysForTenant(tenant);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta postnycklar för en hyresgäst
     */
    @GetMapping("/tenant/{tenantId}/mailbox")
    public ResponseEntity<List<Key>> getMailboxKeysForTenant(@PathVariable UUID tenantId) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getMailboxKeysForTenant(tenant);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta tvättnycklar för en hyresgäst
     */
    @GetMapping("/tenant/{tenantId}/laundry")
    public ResponseEntity<List<Key>> getLaundryKeysForTenant(@PathVariable UUID tenantId) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getLaundryKeysForTenant(tenant);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta garagenycklar för en hyresgäst
     */
    @GetMapping("/tenant/{tenantId}/garage")
    public ResponseEntity<List<Key>> getGarageKeysForTenant(@PathVariable UUID tenantId) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getGarageKeysForTenant(tenant);
        return ResponseEntity.ok(keys);
    }

    /**
     * Hämta förrådsnycklar för en hyresgäst
     */
    @GetMapping("/tenant/{tenantId}/storage")
    public ResponseEntity<List<Key>> getStorageKeysForTenant(@PathVariable UUID tenantId) {
        se.duggals.dfrm.model.Tenant tenant = new se.duggals.dfrm.model.Tenant();
        tenant.setId(tenantId);
        
        List<Key> keys = keyService.getStorageKeysForTenant(tenant);
        return ResponseEntity.ok(keys);
    }
} 