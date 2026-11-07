package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.entities.Specialty;
import com.tecsup.petclinic.repositories.SpecialtyRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/specialties")
public class SpecialtyController {

	private final SpecialtyRepository specialtyRepository;

	public SpecialtyController(SpecialtyRepository specialtyRepository) {
		this.specialtyRepository = specialtyRepository;
	}

	@GetMapping
	public List<Specialty> findAll(@RequestParam(required = false) String name,
								   @RequestParam(required = false) String office) {
		if (name != null) {
			return specialtyRepository.findByName(name);
		}
		if (office != null) {
			return specialtyRepository.findByOffice(office);
		}
		return specialtyRepository.findAll(Sort.by("id"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Specialty> findById(@PathVariable Integer id) {
		return specialtyRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Specialty> create(@RequestBody Specialty specialty) {
		Integer open = specialty.getOpenHour();
		Integer close = specialty.getCloseHour();
		if (specialty.getName() == null || specialty.getName().isBlank() || open == null || close == null) {
			return ResponseEntity.badRequest().build();
		}
		// horas validas: 0 a 23
		if (open < 0 || open > 23 || close < 0 || close > 23) {
			return ResponseEntity.badRequest().build();
		}
		// la hora de apertura debe ser menor a la de cierre
		if (open >= close) {
			return ResponseEntity.badRequest().build();
		}
		specialty.setId(null);
		return ResponseEntity.status(HttpStatus.CREATED).body(specialtyRepository.save(specialty));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Integer id) {
		if (!specialtyRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		specialtyRepository.deleteById(id);
		return ResponseEntity.ok("Delete ID :" + id);
	}
}
