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

import se.duggals.dfrm.model.User;
import se.duggals.dfrm.repository.UserRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Skapa en ny användare
     */
    public User createUser(User user) {
        // Validera att email inte redan finns
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("En användare med denna email finns redan");
        }

        // Sätt standardvärden
        if (user.getRole() == null) {
            user.setRole(User.UserRole.USER);
        }
        if (user.getPreferredLanguage() == null) {
            user.setPreferredLanguage("sv");
        }
        user.setActive(true);

        return userRepository.save(user);
    }

    /**
     * Hämta användare efter ID
     */
    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    /**
     * Hämta användare efter email
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Hämta alla aktiva användare
     */
    public List<User> getAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    /**
     * Hämta användare efter roll
     */
    public List<User> getUsersByRole(User.UserRole role) {
        return userRepository.findByRole(role);
    }

    /**
     * Sök användare
     */
    public Page<User> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable);
    }

    /**
     * Uppdatera användare
     */
    public User updateUser(UUID id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Användare hittades inte"));

        // Uppdatera fält
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setRole(updatedUser.getRole());
        existingUser.setPreferredLanguage(updatedUser.getPreferredLanguage());
        existingUser.setActive(updatedUser.getActive());
        existingUser.setPermissions(updatedUser.getPermissions());

        return userRepository.save(existingUser);
    }

    /**
     * Aktivera användare
     */
    public void activateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Användare hittades inte"));

        user.setActive(true);
        userRepository.save(user);
    }

    /**
     * Inaktivera användare
     */
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Användare hittades inte"));

        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Ta bort användare (soft delete)
     */
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Användare hittades inte"));

        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Kontrollera om användare har specifik behörighet
     */
    public boolean hasPermission(UUID userId, String permission) {
        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null || !user.getActive()) {
            return false;
        }

        // SUPERADMIN har alla behörigheter
        if (user.getRole() == User.UserRole.SUPERADMIN) {
            return true;
        }

        // Kontrollera specifika behörigheter
        return user.getPermissions() != null && user.getPermissions().contains(permission);
    }

    /**
     * Hämta användare som skapades efter ett visst datum
     */
    public List<User> getUsersCreatedAfter(LocalDateTime date) {
        return userRepository.findByCreatedAtAfter(date);
    }

    /**
     * Räkna användare efter roll
     */
    public long countUsersByRole(User.UserRole role) {
        return userRepository.countByRole(role);
    }

    /**
     * Räkna aktiva användare
     */
    public long countActiveUsers() {
        return userRepository.countByActiveTrue();
    }

    /**
     * Kontrollera om email redan finns
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Hämta användare efter språk
     */
    public List<User> getUsersByLanguage(String language) {
        return userRepository.findByPreferredLanguage(language);
    }
} 