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
 * Pruebas de integracion (sin mocks) de VisitController: controlador -> repositorio -> H2.
 */
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class VisitControllerTest {

	private static final ObjectMapper om = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	private Map<String, Object> visitBody(int petId, Integer vetId, String date, String description, double cost) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("petId", petId);
		body.put("vetId", vetId);
		body.put("visitDate", date);
		body.put("description", description);
		body.put("cost", cost);
		return body;
	}

	private Integer createVisit(Map<String, Object> body) throws Exception {
		String response = mockMvc.perform(post("/visits")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.parse(response).read("$.id");
	}

	// ---------- Listado y consulta ----------

	@Test
	public void testFindAllVisits() throws Exception {
		mockMvc.perform(get("/visits"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))))
				.andExpect(jsonPath("$[0].id", is(1)));
	}

	@Test
	public void testFindVisitOK() throws Exception {
		mockMvc.perform(get("/visits/1"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.description", is("rabies shot")))
				.andExpect(jsonPath("$.petId", is(7)))
				.andExpect(jsonPath("$.vetId", is(2)))
				.andExpect(jsonPath("$.visitDate", is("2010-03-04")));
	}

	@Test
	public void testFindVisitKO() throws Exception {
		mockMvc.perform(get("/visits/666"))
				.andExpect(status().isNotFound());
	}

	// ---------- Creacion, actualizacion y borrado ----------

	@Test
	public void testCreateVisit() throws Exception {
		String response = mockMvc.perform(post("/visits")
						.content(om.writeValueAsString(visitBody(9, 1, "2025-05-10", "annual checkup", 80.0)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.petId", is(9)))
				.andExpect(jsonPath("$.vetId", is(1)))
				.andExpect(jsonPath("$.visitDate", is("2025-05-10")))
				.andExpect(jsonPath("$.description", is("annual checkup")))
				.andReturn().getResponse().getContentAsString();

		Integer id = JsonPath.parse(response).read("$.id");
		mockMvc.perform(delete("/visits/" + id)).andExpect(status().isOk());
	}

	@Test
	public void testUpdateVisit() throws Exception {
		Integer id = createVisit(visitBody(9, 1, "2025-06-01", "initial", 50.0));

		mockMvc.perform(put("/visits/" + id)
						.content(om.writeValueAsString(visitBody(9, 2, "2025-06-01", "updated description", 99.0)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk());

		mockMvc.perform(get("/visits/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id)))
				.andExpect(jsonPath("$.description", is("updated description")))
				.andExpect(jsonPath("$.vetId", is(2)));

		mockMvc.perform(delete("/visits/" + id)).andExpect(status().isOk());
	}

	@Test
	public void testDeleteVisit() throws Exception {
		Integer id = createVisit(visitBody(9, 1, "2025-07-01", "to delete", 10.0));

		mockMvc.perform(delete("/visits/" + id))
				.andExpect(status().isOk());

		mockMvc.perform(get("/visits/" + id))
				.andExpect(status().isNotFound());
	}

	// ---------- Consultas por relacion ----------

	@Test
	public void testFindByPetId() throws Exception {
		// Samantha (pet 7) tiene 2 visitas: ids 1 y 4
		mockMvc.perform(get("/visits").param("petId", "7"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].petId", everyItem(is(7))));
	}

	@Test
	public void testFindByVetId() throws Exception {
		// vet 2 (Helen) atendio las visitas 1 y 2
		mockMvc.perform(get("/visits").param("vetId", "2"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
				.andExpect(jsonPath("$[*].vetId", everyItem(is(2))));
	}

	@Test
	public void testCalculateTotalCostByPet() throws Exception {
		// pet 7: 45.00 (rabies shot) + 275.00 (spayed) = 320.00
		mockMvc.perform(get("/visits/pet/7/total-cost"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("320")));
	}

	// ---------- Consultas por fecha ----------

	@Test
	public void testFindByDateBetween() throws Exception {
		// visitas del 2024: ids 5 y 6
		mockMvc.perform(get("/visits")
						.param("from", "2024-01-01")
						.param("to", "2024-12-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[*].id", containsInAnyOrder(5, 6)));
	}

	@Test
	public void testFindByVetIdAndDateBetween() throws Exception {
		// vet 4 (Rafael) tiene la visita 6 (2024-02-20)
		mockMvc.perform(get("/visits")
						.param("vetId", "4")
						.param("from", "2024-01-01")
						.param("to", "2024-12-31"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(6)))
				.andExpect(jsonPath("$[0].description", is("dental cleaning")));
	}

	@Test
	public void testCountVisitsByPet() throws Exception {
		mockMvc.perform(get("/visits/pet/7/count"))
				.andExpect(status().isOk())
				.andExpect(content().string("2"));
	}
}
