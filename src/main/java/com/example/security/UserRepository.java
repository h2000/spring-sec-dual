package com.example.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Findet einen User anhand des Usernamens
     */
    Optional<User> findByUsername(String username);

    /**
     * Findet einen User anhand der E-Mail-Adresse
     */
    Optional<User> findByEmail(String email);

    /**
     * Findet einen User anhand des Usernamens oder der E-Mail-Adresse
     */
    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    /**
     * Prüft ob ein Username bereits existiert
     */
    boolean existsByUsername(String username);

    /**
     * Prüft ob eine E-Mail bereits existiert
     */
    boolean existsByEmail(String email);

    /**
     * Findet alle aktiven User
     */
    List<User> findByEnabledTrue();

    /**
     * Findet User nach Authentifizierungsmethode
     */
    List<User> findByAuthMethod(String authMethod);

    /**
     * Findet User, die sich seit einem bestimmten Datum eingeloggt haben
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since")
    List<User> findUsersLoggedInSince(@Param("since") LocalDateTime since);

    /**
     * Findet User mit einer bestimmten Rolle
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") User.Role role);

    /**
     * Zählt User nach Authentifizierungsmethode
     */
    @Query("SELECT u.authMethod, COUNT(u) FROM User u GROUP BY u.authMethod")
    List<Object[]> countUsersByAuthMethod();
}