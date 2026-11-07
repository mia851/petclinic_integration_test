package com.tecsup.petclinic.repositories;

import com.tecsup.petclinic.entities.PetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface PetTypeRepository extends JpaRepository<PetType, Integer> {

	List<PetType> findByActive(Boolean active);
}
