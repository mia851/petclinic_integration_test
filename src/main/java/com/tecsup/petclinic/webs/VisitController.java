package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.entities.Visit;
import com.tecsup.petclinic.repositories.PetRepository;
import com.tecsup.petclinic.repositories.VisitRepository;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/visits")
public class VisitController {

	private final VisitRepository visitRepository;

	private final PetRepository petRepository;

	public VisitController(VisitRepository visitRepository, PetRepository petRepository) {
		this.visitRepository = visitRepository;
		this.petRepository = petRepository;
	}

	@GetMapping
	public List<Visit> findAll(
			@RequestParam(required = false) Integer petId,
			@RequestParam(required = false) Integer vetId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

		if (vetId != null && from != null && to != null) {
			return visitRepository.findByVetIdAndVisitDateBetween(vetId, from, to);
		}
		if (petId != null) {
			return visitRepository.findByPetId(petId);
		}
		if (vetId != null) {
			return visitRepository.findByVetId(vetId);
		}
		if (from != null && to != null) {
			return visitRepository.findByVisitDateBetween(from, to);
		}
		return visitRepository.findAll(Sort.by("id"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Visit> findById(@PathVariable Long id) {
		return visitRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Visit> create(@RequestBody Visit visit) {
		if (visit.getPetId() == null || visit.getVisitDate() == null
				|| !petRepository.existsById(visit.getPetId())) {
			return ResponseEntity.badRequest().build();
		}
		visit.setId(null);
		return ResponseEntity.status(HttpStatus.CREATED).body(visitRepository.save(visit));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Visit> update(@PathVariable Long id, @RequestBody Visit data) {
		Optional<Visit> found = visitRepository.findById(id);
		if (found.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Visit visit = found.get();
		visit.setDescription(data.getDescription());
		visit.setVisitDate(data.getVisitDate() != null ? data.getVisitDate() : visit.getVisitDate());
		visit.setVetId(data.getVetId());
		visit.setCost(data.getCost());
		return ResponseEntity.ok(visitRepository.save(visit));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id) {
		if (!visitRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		visitRepository.deleteById(id);
		return ResponseEntity.ok("Delete ID :" + id);
	}

	@GetMapping("/pet/{petId}/total-cost")
	public BigDecimal totalCostByPet(@PathVariable Integer petId) {
		return visitRepository.findByPetId(petId).stream()
				.map(Visit::getCost)
				.filter(c -> c != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@GetMapping("/pet/{petId}/count")
	public long countByPet(@PathVariable Integer petId) {
		return visitRepository.findByPetId(petId).size();
	}
}
