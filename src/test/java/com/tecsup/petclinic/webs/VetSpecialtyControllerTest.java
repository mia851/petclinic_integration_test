package com.tecsup.petclinic.webs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integracion (sin mocks) de VetSpecialtyController: controlador -> repositorio -> H2.
 */
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class VetSpecialtyControllerTest {

	private static final ObjectMapper om = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	private Map<String, Object> assignBody(int vetId, int specialtyId, int years, boolean primary) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("vetId", vetId);
		body.put("specialtyId", specialtyId);
		body.put("certificationDate", "2020-01-31");
		body.put("yearsExperience", years);
		body.put("primary", primary);
		body.put("notes", "asignacion de prueba");
		return body;
	}

	private void assign(Map<String, Object> body) throws Exception {
		mockMvc.perform(post("/vet-specialties")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	private void remove(int vetId, int specId) throws Exception {
		mockMvc.perform(delete("/vet-specialties/" + vetId + "/" + specId))
				.andExpect(status().isOk());
	}

	// ---------- Asignacion ----------

	@Test
	public void testAssignSpecialtyToVet() throws Exception {
		// vet 1 (James) no tiene especialidades en data.sql
		mockMvc.perform(post("/vet-specialties")
						.content(om.writeValueAsString(assignBody(1, 3, 4, false)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.vetId", is(1)))
				.andExpect(jsonPath("$.specialtyId", is(3)))
				.andExpect(jsonPath("$.yearsExperience", is(4)))
				.andExpect(jsonPath("$.certificationDate", is("2020-01-31")));

		remove(1, 3);
	}

	@Test
	public void testRemoveSpecialtyFromVet() throws Exception {
		assign(assignBody(1, 2, 3, false));

		mockMvc.perform(delete("/vet-specialties/1/2"))
				.andExpect(status().isOk());

		// ya no existe: borrar de nuevo -> 404
		mockMvc.perform(delete("/vet-specialties/1/2"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testAssignDuplicate_ShouldFail() throws Exception {
		assign(assignBody(1, 1, 2, false));

		mockMvc.perform(post("/vet-specialties")
						.content(om.writeValueAsString(assignBody(1, 1, 2, false)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isConflict());

		remove(1, 1);
	}

	// ---------- Consultas ----------

	@Test
	public void testFindSpecialtiesByVet() throws Exception {
		// vet 3 (Linda): surgery (2) y dentistry (3)
		mockMvc.perform(get("/vets/3/specialties"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].name", containsInAnyOrder("surgery", "dentistry")));
	}

	@Test
	public void testFindVetsBySpecialty() throws Exception {
		// radiology (1): vets 2 y 5
		mockMvc.perform(get("/specialties/1/vets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].id", containsInAnyOrder(2, 5)));
	}

	@Test
	public void testFindSpecialtiesByVet_Empty() throws Exception {
		// vet 6 (Sharon) no tiene especialidades asignadas
		mockMvc.perform(get("/vets/6/specialties"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	// ---------- Especialidad principal ----------

	@Test
	public void testSetPrimarySpecialty() throws Exception {
		// vet 3: dentistry (3) pasa a ser la principal
		mockMvc.perform(put("/vet-specialties/3/3/primary"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.vetId", is(3)))
				.andExpect(jsonPath("$.specialtyId", is(3)))
				.andExpect(jsonPath("$.primary", is(true)));

		// se restaura el estado original (surgery principal)
		mockMvc.perform(put("/vet-specialties/3/2/primary")).andExpect(status().isOk());
	}

	@Test
	public void testSetPrimarySpecialty_OnlyOnePrimary() throws Exception {
		// vet 3 tiene surgery (2, principal) y dentistry (3). Se marca dentistry como principal.
		mockMvc.perform(put("/vet-specialties/3/3/primary"))
				.andExpect(status().isOk());

		// solo una asignacion del vet 3 queda en primary = true, y es la (3,3)
		mockMvc.perform(get("/vet-specialties").param("vetId", "3"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[?(@.primary == true)]", hasSize(1)))
				.andExpect(jsonPath("$[?(@.specialtyId == 2)].primary", contains(false)))
				.andExpect(jsonPath("$[?(@.specialtyId == 3)].primary", contains(true)));

		// restaurar: surgery vuelve a ser la principal
		mockMvc.perform(put("/vet-specialties/3/2/primary")).andExpect(status().isOk());
	}

	@Test
	public void testSetPrimarySpecialty_NotAssigned() throws Exception {
		// vet 1 no tiene la especialidad 3 asignada
		mockMvc.perform(put("/vet-specialties/1/3/primary"))
				.andExpect(status().isNotFound());
	}

	// ---------- Experiencia y certificaciones ----------

	@Test
	public void testFindVetsByMinExperience() throws Exception {
		// radiology (1): vet 2 (8 anios) y vet 5 (7 anios)
		mockMvc.perform(get("/specialties/1/vets").param("minExperience", "7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].id", containsInAnyOrder(2, 5)));

		mockMvc.perform(get("/specialties/1/vets").param("minExperience", "8"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(2)));
	}

	@Test
	public void testUpdateYearsExperience() throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("yearsExperience", 12);

		mockMvc.perform(put("/vet-specialties/4/2/experience")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.yearsExperience", is(12)));

		// se restaura el valor original (9 anios)
		body.put("yearsExperience", 9);
		mockMvc.perform(put("/vet-specialties/4/2/experience")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	public void testUpdateCertificationDate() throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("certificationDate", "2023-10-15");

		mockMvc.perform(put("/vet-specialties/5/1/certification")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.certificationDate", is("2023-10-15")));

		// se restaura la fecha original
		body.put("certificationDate", "2016-02-28");
		mockMvc.perform(put("/vet-specialties/5/1/certification")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
}
