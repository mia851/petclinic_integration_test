package com.tecsup.petclinic.webs;

import com.tecsup.petclinic.entities.Owner;
import com.tecsup.petclinic.repositories.OwnerRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/owners")
public class OwnerController {

	private final OwnerRepository ownerRepository;

	public OwnerController(OwnerRepository ownerRepository) {
		this.ownerRepository = ownerRepository;
	}

	@GetMapping
	public List<Owner> findAll(@RequestParam(required = false) String lastName) {
		if (lastName != null) {
			return ownerRepository.findByLastName(lastName);
		}
		return ownerRepository.findAll(Sort.by("id"));
	}

	@GetMapping("/{id}")
	public ResponseEntity<Owner> findById(@PathVariable Long id) {
		return ownerRepository.findById(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<Owner> create(@RequestBody Owner owner) {
		if (owner.getLastName() == null || owner.getLastName().isBlank()) {
			return ResponseEntity.badRequest().build();
		}
		owner.setId(null);
		return ResponseEntity.status(HttpStatus.CREATED).body(ownerRepository.save(owner));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Owner> update(@PathVariable Long id, @RequestBody Owner data) {
		Optional<Owner> found = ownerRepository.findById(id);
		if (found.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		Owner owner = found.get();
		owner.setFirstName(data.getFirstName());
		owner.setLastName(data.getLastName());
		owner.setAddress(data.getAddress());
		owner.setCity(data.getCity());
		owner.setTelephone(data.getTelephone());
		return ResponseEntity.ok(ownerRepository.save(owner));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<String> delete(@PathVariable Long id) {
		if (!ownerRepository.existsById(id)) {
			return ResponseEntity.notFound().build();
		}
		ownerRepository.deleteById(id);
		return ResponseEntity.ok("Delete ID :" + id);
	}
}
