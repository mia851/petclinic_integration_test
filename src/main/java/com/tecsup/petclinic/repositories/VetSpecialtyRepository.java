package com.tecsup.petclinic.repositories;

import com.tecsup.petclinic.entities.VetSpecialty;
import com.tecsup.petclinic.entities.VetSpecialtyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface VetSpecialtyRepository extends JpaRepository<VetSpecialty, VetSpecialtyId> {

	List<VetSpecialty> findByVetId(Integer vetId);

	List<VetSpecialty> findBySpecialtyId(Integer specialtyId);
}
