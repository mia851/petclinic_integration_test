package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.entities.PetType;
import com.tecsup.petclinic.repositories.PetRepository;
import com.tecsup.petclinic.repositories.PetTypeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/types")
public class TypeController {

	private final PetTypeRepository typeRepository;

	private final PetRepository petRepository;

	public TypeController(PetTypeRepository typeRepository, PetRepository petRepository) {
		this.typeRepository = typeRepository;
		this.petRepository = petRepository;
	}

	@GetMapping
	public List<PetType> findAll(@RequestParam(required = false) Boolean active) {
		if (active != null) {
			return typeRepository.findByActive(active);
		}
		return typeRepository.findAll(Sort.by("id"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<PetType> findById(@PathVariable Integer id) {
		return typeRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<PetType> create(@RequestBody PetType type) {
		if (type.getName() == null || type.getName().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		type.setId(null);
		if (type.getActive() == null) {
			type.setActive(true);
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(typeRepository.save(type));
	}

	/**
	 * Cantidad de mascotas por tipo (solo tipos activos).
	 */
	@GetMapping("/report/pet-count")
	public List<Map<String, Object>> petCountByType() {
		List<Map<String, Object>> report = new ArrayList<>();
		for (PetType type : typeRepository.findByActive(true)) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("typeId", type.getId());
			row.put("name", type.getName());
			row.put("petCount", petRepository.findByTypeId(type.getId()).size());
			report.add(row);
		}
		return report;
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Integer id) {
		if (!typeRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		try {
			typeRepository.deleteById(id);
			typeRepository.flush();
		} catch (DataIntegrityViolationException e) {
			// el tipo todavia tiene mascotas asociadas
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
		return ResponseEntity.ok("Delete ID :" + id);
	}
}
