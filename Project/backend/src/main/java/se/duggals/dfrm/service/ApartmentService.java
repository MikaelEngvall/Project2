package se.duggals.dfrm.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import se.duggals.dfrm.model.Apartment;
import se.duggals.dfrm.repository.ApartmentRepository;

@Service
@Transactional
public class ApartmentService {

    @Autowired
    private ApartmentRepository apartmentRepository;

    /**
     * Skapa en ny lägenhet
     */
    public Apartment createApartment(Apartment apartment) {
        // Validera att lägenheten inte redan finns
        if (apartmentRepository.findByStreetAndNumberAndApartmentNumber(
                apartment.getStreet(), apartment.getNumber(), apartment.getApartmentNumber()).isPresent()) {
            throw new IllegalArgumentException("En lägenhet med denna adress finns redan");
        }

        return apartmentRepository.save(apartment);
    }

    /**
     * Hämta lägenhet efter ID
     */
    public Optional<Apartment> getApartmentById(UUID id) {
        return apartmentRepository.findById(id);
    }

    /**
     * Hämta lägenhet efter adress
     */
    public Optional<Apartment> getApartmentByAddress(String street, String number, String apartmentNumber) {
        return apartmentRepository.findByStreetAndNumberAndApartmentNumber(street, number, apartmentNumber);
    }

    /**
     * Hämta alla lägenheter
     */
    public List<Apartment> getAllApartments() {
        return apartmentRepository.findAll();
    }

    /**
     * Hämta lediga lägenheter
     */
    public List<Apartment> getAvailableApartments() {
        return apartmentRepository.findByOccupiedFalse();
    }

    /**
     * Hämta upptagna lägenheter
     */
    public List<Apartment> getOccupiedApartments() {
        return apartmentRepository.findByOccupiedTrue();
    }

    /**
     * Hämta lägenheter efter område
     */
    public List<Apartment> getApartmentsByArea(String area) {
        return apartmentRepository.findByArea(area);
    }

    /**
     * Hämta lägenheter efter postnummer
     */
    public List<Apartment> getApartmentsByPostalCode(String postalCode) {
        return apartmentRepository.findByPostalCode(postalCode);
    }

    /**
     * Sök lägenheter
     */
    public Page<Apartment> searchApartments(String searchTerm, Pageable pageable) {
        return apartmentRepository.searchApartments(searchTerm, pageable);
    }

    /**
     * Uppdatera lägenhet
     */
    public Apartment updateApartment(UUID id, Apartment updatedApartment) {
        Apartment existingApartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lägenhet hittades inte"));

        // Uppdatera fält
        existingApartment.setStreet(updatedApartment.getStreet());
        existingApartment.setNumber(updatedApartment.getNumber());
        existingApartment.setApartmentNumber(updatedApartment.getApartmentNumber());
        existingApartment.setSize(updatedApartment.getSize());
        existingApartment.setFloor(updatedApartment.getFloor());
        existingApartment.setArea(updatedApartment.getArea());
        existingApartment.setRooms(updatedApartment.getRooms());
        existingApartment.setMonthlyRent(updatedApartment.getMonthlyRent());
        existingApartment.setPostalCode(updatedApartment.getPostalCode());
        existingApartment.setOccupied(updatedApartment.getOccupied());

        return apartmentRepository.save(existingApartment);
    }

    /**
     * Markera lägenhet som upptagen
     */
    public void markApartmentAsOccupied(UUID id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lägenhet hittades inte"));

        apartment.setOccupied(true);
        apartmentRepository.save(apartment);
    }

    /**
     * Markera lägenhet som ledig
     */
    public void markApartmentAsAvailable(UUID id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lägenhet hittades inte"));

        apartment.setOccupied(false);
        apartmentRepository.save(apartment);
    }

    /**
     * Ta bort lägenhet (soft delete)
     */
    public void deleteApartment(UUID id) {
        Apartment apartment = apartmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lägenhet hittades inte"));

        apartmentRepository.delete(apartment);
    }

    /**
     * Hämta lägenheter med hyra under ett visst belopp
     */
    public List<Apartment> getApartmentsWithRentBelow(BigDecimal maxRent) {
        return apartmentRepository.findByMonthlyRentLessThanEqual(maxRent);
    }

    /**
     * Hämta lägenheter med hyra över ett visst belopp
     */
    public List<Apartment> getApartmentsWithRentAbove(BigDecimal minRent) {
        return apartmentRepository.findByMonthlyRentBetween(minRent, new BigDecimal("999999"));
    }

    /**
     * Hämta lägenheter med specifik storlek
     */
    public List<Apartment> getApartmentsBySize(Integer size) {
        return apartmentRepository.findBySize(size);
    }

    /**
     * Hämta lägenheter med specifikt antal rum
     */
    public List<Apartment> getApartmentsByRooms(Integer rooms) {
        return apartmentRepository.findByRooms(rooms);
    }

    /**
     * Hämta lägenheter på specifik våning
     */
    public List<Apartment> getApartmentsByFloor(Integer floor) {
        return apartmentRepository.findByFloor(floor);
    }

    /**
     * Räkna totalt antal lägenheter
     */
    public long countAllApartments() {
        return apartmentRepository.count();
    }

    /**
     * Räkna lediga lägenheter
     */
    public long countAvailableApartments() {
        return apartmentRepository.countByOccupiedFalse();
    }

    /**
     * Räkna upptagna lägenheter
     */
    public long countOccupiedApartments() {
        return apartmentRepository.countByOccupiedTrue();
    }

    /**
     * Hämta lediga lägenheter i specifikt område
     */
    public List<Apartment> getAvailableApartmentsByArea(Double area) {
        return apartmentRepository.findByAreaAndOccupiedFalse(area);
    }

    /**
     * Hämta lediga lägenheter med specifik storlek
     */
    public List<Apartment> getAvailableApartmentsBySize(Integer size) {
        return apartmentRepository.findBySizeAndOccupiedFalse(size);
    }
} 