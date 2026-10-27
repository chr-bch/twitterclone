package de.cb.twitterclone.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByExternalId(String externalId);

    Optional<AppUser> findByUsername(String username);
}
