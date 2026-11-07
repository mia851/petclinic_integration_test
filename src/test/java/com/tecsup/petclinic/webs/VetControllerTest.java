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
 * Pruebas de integracion (sin mocks) de VetController: controlador -> repositorio -> H2.
 */
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class VetControllerTest {

	private static final ObjectMapper om = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	private Integer createVet(String firstName, String lastName) throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("firstName", firstName);
		body.put("lastName", lastName);
		body.put("email", firstName.toLowerCase() + "@petclinic.com");
		body.put("phone", "999000111");

		String response = mockMvc.perform(post("/vets")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.parse(response).read("$.id");
	}

	@Test
	public void testFindAllVets() throws Exception {
		mockMvc.perform(get("/vets"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(6))))
				.andExpect(jsonPath("$[0].id", is(1)));
	}

	@Test
	public void testFindVetOK() throws Exception {
		mockMvc.perform(get("/vets/1"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.firstName", is("James")))
				.andExpect(jsonPath("$.lastName", is("Carter")))
				.andExpect(jsonPath("$.active", is(true)));
	}

	@Test
	public void testFindVetKO() throws Exception {
		mockMvc.perform(get("/vets/666"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testCreateVet() throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("firstName", "Mia");
		body.put("lastName", "Castillo");
		body.put("email", "mia.castillo@petclinic.com");
		body.put("phone", "987654321");

		String response = mockMvc.perform(post("/vets")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.firstName", is("Mia")))
				.andExpect(jsonPath("$.lastName", is("Castillo")))
				.andExpect(jsonPath("$.email", is("mia.castillo@petclinic.com")))
				.andExpect(jsonPath("$.active", is(true)))
				.andReturn().getResponse().getContentAsString();

		Integer id = JsonPath.parse(response).read("$.id");
		mockMvc.perform(delete("/vets/" + id)).andExpect(status().isOk());
	}

	@Test
	public void testDeactivateVet() throws Exception {
		Integer id = createVet("Temporal", "Desactivar");

		mockMvc.perform(put("/vets/" + id + "/deactivate"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id)))
				.andExpect(jsonPath("$.active", is(false)));

		mockMvc.perform(delete("/vets/" + id)).andExpect(status().isOk());
	}

	@Test
	public void testReactivateVet() throws Exception {
		final int INACTIVE_VET_ID = 6;   // Sharon Jenkins (inactiva en data.sql)

		mockMvc.perform(put("/vets/" + INACTIVE_VET_ID + "/reactivate"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active", is(true)));

		// se deja el dato como estaba para no afectar otras pruebas
		mockMvc.perform(put("/vets/" + INACTIVE_VET_ID + "/deactivate"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.active", is(false)));
	}

	@Test
	public void testFindActiveVets() throws Exception {
		mockMvc.perform(get("/vets").param("active", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
				.andExpect(jsonPath("$[?(@.active == false)]", empty()));
	}

	@Test
	public void testFindInactiveVets() throws Exception {
		mockMvc.perform(get("/vets").param("active", "false"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
				.andExpect(jsonPath("$[?(@.active == true)]", empty()))
				.andExpect(jsonPath("$[0].lastName", is("Jenkins")));
	}

	@Test
	public void testDeleteVetKO() throws Exception {
		mockMvc.perform(delete("/vets/1000"))
				.andExpect(status().isNotFound());
	}
}
