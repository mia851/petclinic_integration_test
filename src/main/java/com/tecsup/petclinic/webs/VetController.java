package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.entities.Vet;
import com.tecsup.petclinic.repositories.VetRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/vets")
public class VetController {

	private final VetRepository vetRepository;

	public VetController(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	@GetMapping
	public List<Vet> findAll(@RequestParam(required = false) Boolean active) {
		if (active != null) {
			return vetRepository.findByActive(active);
		}
		return vetRepository.findAll(Sort.by("id"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Vet> findById(@PathVariable Integer id) {
		return vetRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Vet> create(@RequestBody Vet vet) {
		if (vet.getLastName() == null || vet.getLastName().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		vet.setId(null);
		if (vet.getActive() == null) {
			vet.setActive(true);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(vetRepository.save(vet));
	}

	@PutMapping("/{id}/deactivate")
	public ResponseEntity<Vet> deactivate(@PathVariable Integer id) {
		return changeActive(id, false);
	}

	@PutMapping("/{id}/reactivate")
	public ResponseEntity<Vet> reactivate(@PathVariable Integer id) {
		return changeActive(id, true);
	}

	private ResponseEntity<Vet> changeActive(Integer id, boolean active) {
		Optional<Vet> found = vetRepository.findById(id);
		if (found.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Vet vet = found.get();
		vet.setActive(active);
		return ResponseEntity.ok(vetRepository.save(vet));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Integer id) {
		if (!vetRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		vetRepository.deleteById(id);
		return ResponseEntity.ok("Delete ID :" + id);
	}
}
