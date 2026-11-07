package com.tecsup.petclinic.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Especialidad (tabla specialties).
 */
@NoArgsConstructor
@Entity(name = "specialties")
@Data
public class Specialty {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "office")
	private String office;

	@JsonProperty("h_open")
	@Column(name = "h_open")
	private Integer openHour;

	@JsonProperty("h_close")
	@Column(name = "h_close")
	private Integer closeHour;

}
