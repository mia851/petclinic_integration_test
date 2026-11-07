package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.entities.Specialty;
import com.tecsup.petclinic.entities.Vet;
import com.tecsup.petclinic.entities.VetSpecialty;
import com.tecsup.petclinic.entities.VetSpecialtyId;
import com.tecsup.petclinic.repositories.SpecialtyRepository;
import com.tecsup.petclinic.repositories.VetRepository;
import com.tecsup.petclinic.repositories.VetSpecialtyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class VetSpecialtyController {

	private final VetSpecialtyRepository vetSpecialtyRepository;

	private final VetRepository vetRepository;

	private final SpecialtyRepository specialtyRepository;

	public VetSpecialtyController(VetSpecialtyRepository vetSpecialtyRepository,
								  VetRepository vetRepository,
								  SpecialtyRepository specialtyRepository) {
		this.vetSpecialtyRepository = vetSpecialtyRepository;
		this.vetRepository = vetRepository;
		this.specialtyRepository = specialtyRepository;
	}

	/** Asignar una especialidad a un veterinario. */
	@PostMapping("/vet-specialties")
	public ResponseEntity<VetSpecialty> assign(@RequestBody VetSpecialty body) {
		if (body.getVetId() == null || body.getSpecialtyId() == null) {
			return ResponseEntity.badRequest().build();
		}
		if (!vetRepository.existsById(body.getVetId())
				|| !specialtyRepository.existsById(body.getSpecialtyId())) {
			return ResponseEntity.notFound().build();
		}
		if (vetSpecialtyRepository.existsById(new VetSpecialtyId(body.getVetId(), body.getSpecialtyId()))) {
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		if (body.getYearsExperience() == null) {
			body.setYearsExperience(0);
		}
		if (body.getPrimary() == null) {
			body.setPrimary(false);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(vetSpecialtyRepository.save(body));
	}

	/** Asignaciones de un veterinario (con anios de experiencia, principal, etc.). */
	@GetMapping("/vet-specialties")
	public ResponseEntity<List<VetSpecialty>> findAssignments(@RequestParam Integer vetId) {
		return ResponseEntity.ok(vetSpecialtyRepository.findByVetId(vetId));
	}

	/** Quitar una especialidad a un veterinario. */
	@DeleteMapping("/vet-specialties/{vetId}/{specId}")
	public ResponseEntity<String> remove(@PathVariable Integer vetId, @PathVariable Integer specId) {
		VetSpecialtyId id = new VetSpecialtyId(vetId, specId);
		if (!vetSpecialtyRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		vetSpecialtyRepository.deleteById(id);
		return ResponseEntity.ok("Delete ID :" + vetId + "/" + specId);
	}

	/** Especialidades de un veterinario. */
	@GetMapping("/vets/{vetId}/specialties")
	public ResponseEntity<List<Specialty>> findSpecialtiesByVet(@PathVariable Integer vetId) {
		if (!vetRepository.existsById(vetId)) {
			return ResponseEntity.notFound().build();
		}
		List<Integer> ids = vetSpecialtyRepository.findByVetId(vetId).stream()
				.map(VetSpecialty::getSpecialtyId).toList();
		return ResponseEntity.ok(specialtyRepository.findAllById(ids));
	}

	/** Veterinarios de una especialidad (opcionalmente con experiencia minima). */
	@GetMapping("/specialties/{specId}/vets")
	public ResponseEntity<List<Vet>> findVetsBySpecialty(
			@PathVariable Integer specId,
			@RequestParam(required = false) Integer minExperience) {
		if (!specialtyRepository.existsById(specId)) {
			return ResponseEntity.notFound().build();
		}
		List<Integer> ids = vetSpecialtyRepository.findBySpecialtyId(specId).stream()
				.filter(vs -> minExperience == null
						|| (vs.getYearsExperience() != null && vs.getYearsExperience() >= minExperience))
				.map(VetSpecialty::getVetId).toList();
		return ResponseEntity.ok(vetRepository.findAllById(ids));
	}

	/** Marcar la especialidad principal (las demas del veterinario quedan en false). */
	@PutMapping("/vet-specialties/{vetId}/{specId}/primary")
	public ResponseEntity<VetSpecialty> setPrimary(@PathVariable Integer vetId, @PathVariable Integer specId) {
		Optional<VetSpecialty> target = vetSpecialtyRepository.findById(new VetSpecialtyId(vetId, specId));
		if (target.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		for (VetSpecialty other : vetSpecialtyRepository.findByVetId(vetId)) {
			boolean isTarget = other.getSpecialtyId().equals(specId);
			if (Boolean.TRUE.equals(other.getPrimary()) != isTarget) {
				other.setPrimary(isTarget);
				vetSpecialtyRepository.save(other);
			}
		}
		return ResponseEntity.ok(vetSpecialtyRepository.findById(new VetSpecialtyId(vetId, specId)).get());
	}

	/** Actualizar anios de experiencia. Cuerpo: {"yearsExperience": 10}. */
	@PutMapping("/vet-specialties/{vetId}/{specId}/experience")
	public ResponseEntity<VetSpecialty> updateExperience(@PathVariable Integer vetId,
														 @PathVariable Integer specId,
														 @RequestBody Map<String, Object> body) {
		Optional<VetSpecialty> found = vetSpecialtyRepository.findById(new VetSpecialtyId(vetId, specId));
		if (found.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Object years = body.get("yearsExperience");
		if (!(years instanceof Number) || ((Number) years).intValue() < 0) {
			return ResponseEntity.badRequest().build();
		}
		VetSpecialty vs = found.get();
		vs.setYearsExperience(((Number) years).intValue());
		return ResponseEntity.ok(vetSpecialtyRepository.save(vs));
	}

	/** Actualizar fecha de certificacion. Cuerpo: {"certificationDate": "2020-01-31"}. */
	@PutMapping("/vet-specialties/{vetId}/{specId}/certification")
	public ResponseEntity<VetSpecialty> updateCertification(@PathVariable Integer vetId,
															@PathVariable Integer specId,
															@RequestBody Map<String, Object> body) {
		Optional<VetSpecialty> found = vetSpecialtyRepository.findById(new VetSpecialtyId(vetId, specId));
		if (found.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		LocalDate date;
		try {
			date = LocalDate.parse(String.valueOf(body.get("certificationDate")));
		} catch (Exception e) {
			return ResponseEntity.badRequest().build();
		}
		VetSpecialty vs = found.get();
		vs.setCertificationDate(date);
		return ResponseEntity.ok(vetSpecialtyRepository.save(vs));
	}
}
