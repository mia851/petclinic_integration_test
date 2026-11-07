package com.tecsup.petclinic.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Relacion veterinario-especialidad (tabla vet_specialties).
 */
@NoArgsConstructor
@Entity(name = "vet_specialties")
@IdClass(VetSpecialtyId.class)
@Data
public class VetSpecialty {

	@Id
	@Column(name = "vet_id")
	private Integer vetId;

	@Id
	@Column(name = "specialty_id")
	private Integer specialtyId;

	@Column(name = "certification_date")
	private LocalDate certificationDate;

	@Column(name = "years_experience")
	private Integer yearsExperience;

	@Column(name = "is_primary")
	private Boolean primary;

	@Column(name = "notes")
	private String notes;
}
