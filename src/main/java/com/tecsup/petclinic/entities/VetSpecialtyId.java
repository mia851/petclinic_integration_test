package com.tecsup.petclinic.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clave compuesta de vet_specialties.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VetSpecialtyId implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer vetId;

	private Integer specialtyId;
}
