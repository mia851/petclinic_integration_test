package com.tecsup.petclinic.repositories;

import com.tecsup.petclinic.entities.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDate;
import java.util.List;

@RepositoryRestResource(exported = false)
public interface VisitRepository extends JpaRepository<Visit, Long> {

	List<Visit> findByPetId(Integer petId);

	List<Visit> findByVetId(Integer vetId);

	List<Visit> findByVisitDateBetween(LocalDate from, LocalDate to);

	List<Visit> findByVetIdAndVisitDateBetween(Integer vetId, LocalDate from, LocalDate to);
}
