package se.duggals.dfrm.controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import se.duggals.dfrm.model.Apartment;
import se.duggals.dfrm.service.ApartmentService;

@RestController
@RequestMapping("/api/apartments")
@CrossOrigin(origins = "*")
public class ApartmentController {

    @Autowired
    private ApartmentService apartmentService;

    /**
     * Skapa en ny lägenhet
     */
    @PostMapping
    public ResponseEntity<Apartment> createApartment(@RequestBody Apartment apartment) {
        try {
            Apartment createdApartment = apartmentService.createApartment(apartment);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdApartment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Hämta lägenhet efter ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Apartment> getApartmentById(@PathVariable UUID id) {
        return apartmentService.getApartmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta lägenhet efter adress
     */
    @GetMapping("/address")
    public ResponseEntity<Apartment> getApartmentByAddress(
            @RequestParam String street,
            @RequestParam String number,
            @RequestParam String apartmentNumber) {
        return apartmentService.getApartmentByAddress(street, number, apartmentNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Hämta alla lägenheter
     */
    @GetMapping
    public ResponseEntity<List<Apartment>> getAllApartments() {
        List<Apartment> apartments = apartmentService.getAllApartments();
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta lediga lägenheter
     */
    @GetMapping("/available")
    public ResponseEntity<List<Apartment>> getAvailableApartments() {
        List<Apartment> apartments = apartmentService.getAvailableApartments();
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta upptagna lägenheter
     */
    @GetMapping("/occupied")
    public ResponseEntity<List<Apartment>> getOccupiedApartments() {
        List<Apartment> apartments = apartmentService.getOccupiedApartments();
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta lägenheter efter område
     */
    @GetMapping("/area/{area}")
    public ResponseEntity<List<Apartment>> getApartmentsByArea(@PathVariable String area) {
        List<Apartment> apartments = apartmentService.getApartmentsByArea(area);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta lägenheter efter postnummer
     */
    @GetMapping("/postal-code/{postalCode}")
    public ResponseEntity<List<Apartment>> getApartmentsByPostalCode(@PathVariable String postalCode) {
        List<Apartment> apartments = apartmentService.getApartmentsByPostalCode(postalCode);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Sök lägenheter
     */
    @GetMapping("/search")
    public ResponseEntity<Page<Apartment>> searchApartments(@RequestParam String searchTerm, Pageable pageable) {
        Page<Apartment> apartments = apartmentService.searchApartments(searchTerm, pageable);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Uppdatera lägenhet
     */
    @PutMapping("/{id}")
    public ResponseEntity<Apartment> updateApartment(@PathVariable UUID id, @RequestBody Apartment updatedApartment) {
        try {
            Apartment apartment = apartmentService.updateApartment(id, updatedApartment);
            return ResponseEntity.ok(apartment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Markera lägenhet som upptagen
     */
    @PatchMapping("/{id}/mark-occupied")
    public ResponseEntity<Void> markApartmentAsOccupied(@PathVariable UUID id) {
        try {
            apartmentService.markApartmentAsOccupied(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Markera lägenhet som ledig
     */
    @PatchMapping("/{id}/mark-available")
    public ResponseEntity<Void> markApartmentAsAvailable(@PathVariable UUID id) {
        try {
            apartmentService.markApartmentAsAvailable(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Ta bort lägenhet
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartment(@PathVariable UUID id) {
        try {
            apartmentService.deleteApartment(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Hämta lägenheter med hyra under ett visst belopp
     */
    @GetMapping("/rent-below")
    public ResponseEntity<List<Apartment>> getApartmentsWithRentBelow(@RequestParam BigDecimal maxRent) {
        List<Apartment> apartments = apartmentService.getApartmentsWithRentBelow(maxRent);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta lägenheter med hyra över ett visst belopp
     */
    @GetMapping("/rent-above")
    public ResponseEntity<List<Apartment>> getApartmentsWithRentAbove(@RequestParam BigDecimal minRent) {
        List<Apartment> apartments = apartmentService.getApartmentsWithRentAbove(minRent);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta lägenheter med specifik storlek
     */
    @GetMapping("/size/{size}")
    public ResponseEntity<List<Apartment>> getApartmentsBySize(@PathVariable Integer size) {
        List<Apartment> apartments = apartmentService.getApartmentsBySize(size);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta lägenheter med specifikt antal rum
     */
    @GetMapping("/rooms/{rooms}")
    public ResponseEntity<List<Apartment>> getApartmentsByRooms(@PathVariable Integer rooms) {
        List<Apartment> apartments = apartmentService.getApartmentsByRooms(rooms);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta lägenheter på specifik våning
     */
    @GetMapping("/floor/{floor}")
    public ResponseEntity<List<Apartment>> getApartmentsByFloor(@PathVariable Integer floor) {
        List<Apartment> apartments = apartmentService.getApartmentsByFloor(floor);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Räkna totalt antal lägenheter
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countAllApartments() {
        long count = apartmentService.countAllApartments();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna lediga lägenheter
     */
    @GetMapping("/count/available")
    public ResponseEntity<Long> countAvailableApartments() {
        long count = apartmentService.countAvailableApartments();
        return ResponseEntity.ok(count);
    }

    /**
     * Räkna upptagna lägenheter
     */
    @GetMapping("/count/occupied")
    public ResponseEntity<Long> countOccupiedApartments() {
        long count = apartmentService.countOccupiedApartments();
        return ResponseEntity.ok(count);
    }

    /**
     * Hämta lediga lägenheter i specifikt område
     */
    @GetMapping("/available/area/{area}")
    public ResponseEntity<List<Apartment>> getAvailableApartmentsByArea(@PathVariable String area) {
        List<Apartment> apartments = apartmentService.getAvailableApartmentsByArea(area);
        return ResponseEntity.ok(apartments);
    }

    /**
     * Hämta lediga lägenheter med specifik storlek
     */
    @GetMapping("/available/size/{size}")
    public ResponseEntity<List<Apartment>> getAvailableApartmentsBySize(@PathVariable Integer size) {
        List<Apartment> apartments = apartmentService.getAvailableApartmentsBySize(size);
        return ResponseEntity.ok(apartments);
    }
} 