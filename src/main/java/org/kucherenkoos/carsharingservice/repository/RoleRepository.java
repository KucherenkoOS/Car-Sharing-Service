package org.kucherenkoos.carsharingservice.repository;

import java.util.Optional;
import org.kucherenkoos.carsharingservice.model.Role;
import org.kucherenkoos.carsharingservice.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findByName(RoleName name);
}
