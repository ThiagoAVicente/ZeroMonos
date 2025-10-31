package org.hw1.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MunicipalityRepository extends JpaRepository<Municipality, Long> {
    Optional<Municipality> findByName(String name);
}
