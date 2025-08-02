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

import se.duggals.dfrm.model.Apartment;
import se.duggals.dfrm.model.Key;
import se.duggals.dfrm.model.Tenant;
import se.duggals.dfrm.repository.KeyRepository;

@Service
@Transactional
public class KeyService {

    @Autowired
    private KeyRepository keyRepository;

    /**
     * Skapa en ny nyckel
     */
    public Key createKey(Key key) {
        // Validera att nyckeln inte redan finns
        if (keyRepository.findBySeriesAndNumberAndCopy(
                key.getSeries(), key.getNumber(), key.getCopy()).isPresent()) {
            throw new IllegalArgumentException("En nyckel med denna serie, nummer och kopia finns redan");
        }

        // Sätt standardvärden
        if (key.getType() == null) {
            key.setType(Key.KeyType.APARTMENT);
        }
        if (key.getIsActive() == null) {
            key.setIsActive(true);
        }

        return keyRepository.save(key);
    }

    /**
     * Hämta nyckel efter ID
     */
    public Optional<Key> getKeyById(UUID id) {
        return keyRepository.findById(id);
    }

    /**
     * Hämta nyckel efter serie, nummer och kopia
     */
    public Optional<Key> getKeyBySeriesNumberCopy(String series, String number, String copy) {
        return keyRepository.findBySeriesAndNumberAndCopy(series, number, copy);
    }

    /**
     * Hämta alla aktiva nycklar
     */
    public List<Key> getAllActiveKeys() {
        return keyRepository.findByIsActiveTrue();
    }

    /**
     * Hämta nycklar efter typ
     */
    public List<Key> getKeysByType(Key.KeyType type) {
        return keyRepository.findByType(type);
    }

    /**
     * Hämta aktiva nycklar efter typ
     */
    public List<Key> getActiveKeysByType(Key.KeyType type) {
        return keyRepository.findByTypeAndIsActiveTrue(type);
    }

    /**
     * Hämta nycklar för specifik lägenhet
     */
    public List<Key> getKeysByApartment(Apartment apartment) {
        return keyRepository.findByApartment(apartment);
    }

    /**
     * Hämta aktiva nycklar för specifik lägenhet
     */
    public List<Key> getActiveKeysByApartment(Apartment apartment) {
        return keyRepository.findByApartmentAndIsActiveTrue(apartment);
    }

    /**
     * Hämta nycklar för specifik lägenhet och typ
     */
    public List<Key> getKeysByApartmentAndType(Apartment apartment, Key.KeyType type) {
        return keyRepository.findByApartmentAndType(apartment, type);
    }

    /**
     * Hämta nycklar som är utlånade till hyresgäst
     */
    public List<Key> getKeysByTenant(Tenant tenant) {
        return keyRepository.findByTenant(tenant);
    }

    /**
     * Hämta aktiva nycklar som är utlånade till hyresgäst
     */
    public List<Key> getActiveKeysByTenant(Tenant tenant) {
        return keyRepository.findByTenantAndIsActiveTrue(tenant);
    }

    /**
     * Hämta nycklar av specifik typ som är utlånade till hyresgäst
     */
    public List<Key> getKeysByTenantAndType(Tenant tenant, Key.KeyType type) {
        return keyRepository.findByTenantAndType(tenant, type);
    }

    /**
     * Hämta oanvända nycklar
     */
    public List<Key> getUnassignedKeys() {
        return keyRepository.findUnassignedKeys();
    }

    /**
     * Hämta oanvända nycklar av specifik typ
     */
    public List<Key> getUnassignedKeysByType(Key.KeyType type) {
        return keyRepository.findUnassignedKeysByType(type);
    }

    /**
     * Hämta oanvända nycklar för specifik lägenhet
     */
    public List<Key> getUnassignedKeysByApartment(Apartment apartment) {
        return keyRepository.findUnassignedKeysByApartment(apartment);
    }

    /**
     * Sök nycklar
     */
    public Page<Key> searchKeys(String searchTerm, Pageable pageable) {
        return keyRepository.searchKeys(searchTerm, pageable);
    }

    /**
     * Uppdatera nyckel
     */
    public Key updateKey(UUID id, Key updatedKey) {
        Key existingKey = keyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nyckel hittades inte"));

        // Uppdatera fält
        existingKey.setSeries(updatedKey.getSeries());
        existingKey.setNumber(updatedKey.getNumber());
        existingKey.setCopy(updatedKey.getCopy());
        existingKey.setType(updatedKey.getType());
        existingKey.setApartment(updatedKey.getApartment());
        existingKey.setTenant(updatedKey.getTenant());
        existingKey.setIsActive(updatedKey.getIsActive());
        existingKey.setNotes(updatedKey.getNotes());

        return keyRepository.save(existingKey);
    }

    /**
     * Låna ut nyckel till hyresgäst
     */
    public Key assignKeyToTenant(UUID keyId, UUID tenantId) {
        Key key = keyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("Nyckel hittades inte"));

        if (key.getTenant() != null) {
            throw new IllegalArgumentException("Nyckeln är redan utlånad");
        }

        // Här skulle vi normalt hämta Tenant från TenantService
        // För nu sätter vi bara tenant ID
        key.setTenant(new Tenant()); // Placeholder - i verkligheten skulle vi hämta från TenantService
        key.getTenant().setId(tenantId);

        return keyRepository.save(key);
    }

    /**
     * Returnera nyckel från hyresgäst
     */
    public Key returnKeyFromTenant(UUID keyId) {
        Key key = keyRepository.findById(keyId)
                .orElseThrow(() -> new IllegalArgumentException("Nyckel hittades inte"));

        if (key.getTenant() == null) {
            throw new IllegalArgumentException("Nyckeln är inte utlånad");
        }

        key.setTenant(null);
        return keyRepository.save(key);
    }

    /**
     * Inaktivera nyckel
     */
    public void deactivateKey(UUID id) {
        Key key = keyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nyckel hittades inte"));

        key.setIsActive(false);
        keyRepository.save(key);
    }

    /**
     * Ta bort nyckel (soft delete)
     */
    public void deleteKey(UUID id) {
        Key key = keyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nyckel hittades inte"));

        key.setIsActive(false);
        keyRepository.save(key);
    }

    /**
     * Hämta huvudnycklar
     */
    public List<Key> getMasterKeys() {
        return keyRepository.findMasterKeys();
    }

    /**
     * Hämta nycklar som behöver returneras
     */
    public List<Key> getKeysNeedingReturn() {
        return keyRepository.findKeysNeedingReturn();
    }

    /**
     * Hämta nycklar som behöver returneras snart
     */
    public List<Key> getKeysNeedingReturnSoon(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        return keyRepository.findKeysNeedingReturnSoon(startDate, endDate);
    }

    /**
     * Hämta nycklar för lägenheter som är lediga
     */
    public List<Key> getKeysForVacantApartments() {
        return keyRepository.findKeysForVacantApartments();
    }

    /**
     * Hämta nycklar för lägenheter som är upptagna
     */
    public List<Key> getKeysForOccupiedApartments() {
        return keyRepository.findKeysForOccupiedApartments();
    }

    /**
     * Hämta nycklar skapade under specifik period
     */
    public List<Key> getKeysByCreationPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return keyRepository.findKeysByCreationPeriod(startDateTime, endDateTime);
    }

    /**
     * Hämta nycklar som lånades ut under specifik period
     */
    public List<Key> getKeysAssignedInPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return keyRepository.findKeysAssignedInPeriod(startDateTime, endDateTime);
    }

    /**
     * Räkna nycklar per typ
     */
    public long countKeysByType(Key.KeyType type) {
        return keyRepository.countByType(type);
    }

    /**
     * Räkna aktiva nycklar per typ
     */
    public long countActiveKeysByType(Key.KeyType type) {
        return keyRepository.countByTypeAndIsActiveTrue(type);
    }

    /**
     * Räkna nycklar per lägenhet
     */
    public long countKeysByApartment(Apartment apartment) {
        return keyRepository.countByApartment(apartment);
    }

    /**
     * Räkna nycklar per hyresgäst
     */
    public long countKeysByTenant(Tenant tenant) {
        return keyRepository.countByTenant(tenant);
    }

    /**
     * Räkna oanvända nycklar
     */
    public long countUnassignedKeys() {
        return keyRepository.countUnassignedKeys();
    }

    /**
     * Räkna oanvända nycklar per typ
     */
    public long countUnassignedKeysByType(Key.KeyType type) {
        return keyRepository.countUnassignedKeysByType(type);
    }

    /**
     * Hämta nycklar skapade efter datum
     */
    public List<Key> getKeysCreatedAfter(LocalDateTime date) {
        return keyRepository.findByCreatedAtAfter(date);
    }

    /**
     * Hämta nycklar uppdaterade efter datum
     */
    public List<Key> getKeysUpdatedAfter(LocalDateTime date) {
        return keyRepository.findByUpdatedAtAfter(date);
    }

    /**
     * Hämta senaste nycklar
     */
    public Page<Key> getLatestKeys(Pageable pageable) {
        return keyRepository.findLatestKeys(pageable);
    }

    /**
     * Hämta nycklar för specifik serie
     */
    public List<Key> getKeysBySeries(String series) {
        return keyRepository.findBySeries(series);
    }

    /**
     * Hämta nycklar för specifikt nummer
     */
    public List<Key> getKeysByNumber(String number) {
        return keyRepository.findByNumber(number);
    }

    /**
     * Hämta nycklar för specifik kopia
     */
    public List<Key> getKeysByCopy(String copy) {
        return keyRepository.findByCopy(copy);
    }

    /**
     * Hämta nycklar med specifika anteckningar
     */
    public List<Key> getKeysByNotesContaining(String searchTerm) {
        return keyRepository.findByNotesContaining(searchTerm);
    }

    /**
     * Kontrollera om nyckel finns
     */
    public boolean keyExists(String series, String number, String copy) {
        return keyRepository.findBySeriesAndNumberAndCopy(series, number, copy).isPresent();
    }

    /**
     * Hämta alla nycklar för en hyresgäst (alla typer)
     */
    public List<Key> getAllKeysForTenant(Tenant tenant) {
        return keyRepository.findByTenantAndIsActiveTrue(tenant);
    }

    /**
     * Hämta lägenhetsnycklar för en hyresgäst
     */
    public List<Key> getApartmentKeysForTenant(Tenant tenant) {
        return keyRepository.findByTenantAndType(tenant, Key.KeyType.APARTMENT);
    }

    /**
     * Hämta postnycklar för en hyresgäst
     */
    public List<Key> getMailboxKeysForTenant(Tenant tenant) {
        return keyRepository.findByTenantAndType(tenant, Key.KeyType.MAILBOX);
    }

    /**
     * Hämta tvättnycklar för en hyresgäst
     */
    public List<Key> getLaundryKeysForTenant(Tenant tenant) {
        return keyRepository.findByTenantAndType(tenant, Key.KeyType.LAUNDRY);
    }

    /**
     * Hämta garagenycklar för en hyresgäst
     */
    public List<Key> getGarageKeysForTenant(Tenant tenant) {
        return keyRepository.findByTenantAndType(tenant, Key.KeyType.GARAGE);
    }

    /**
     * Hämta förrådsnycklar för en hyresgäst
     */
    public List<Key> getStorageKeysForTenant(Tenant tenant) {
        return keyRepository.findByTenantAndType(tenant, Key.KeyType.STORAGE);
    }
} 