package org.kucherenkoos.carsharingservice.repository;

import java.util.Optional;
import org.kucherenkoos.carsharingservice.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);
}
