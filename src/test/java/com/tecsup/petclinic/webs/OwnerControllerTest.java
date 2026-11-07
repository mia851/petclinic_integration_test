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
 * Pruebas de integracion (sin mocks) de OwnerController: controlador -> repositorio -> H2.
 */
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class OwnerControllerTest {

	private static final ObjectMapper om = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	private Map<String, Object> ownerBody(String firstName, String lastName) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("firstName", firstName);
		body.put("lastName", lastName);
		body.put("address", "Av. Cascanueces 2221");
		body.put("city", "Lima");
		body.put("telephone", "999888777");
		return body;
	}

	private Integer createOwner(String firstName, String lastName) throws Exception {
		String response = mockMvc.perform(post("/owners")
						.content(om.writeValueAsString(ownerBody(firstName, lastName)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.parse(response).read("$.id");
	}

	@Test
	public void testFindAllOwners() throws Exception {
		final int ID_FIRST_RECORD = 1;

		mockMvc.perform(get("/owners"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$[0].id", is(ID_FIRST_RECORD)));
	}

	@Test
	public void testFindOwnerOK() throws Exception {
		mockMvc.perform(get("/owners/1"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.firstName", is("George")))
				.andExpect(jsonPath("$.lastName", is("Franklin")))
				.andExpect(jsonPath("$.city", is("Madison")));
	}

	@Test
	public void testFindOwnerKO() throws Exception {
		mockMvc.perform(get("/owners/666"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testCreateOwner() throws Exception {
		Map<String, Object> body = ownerBody("Mia", "Castillo");

		String response = mockMvc.perform(post("/owners")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.firstName", is("Mia")))
				.andExpect(jsonPath("$.lastName", is("Castillo")))
				.andExpect(jsonPath("$.city", is("Lima")))
				.andExpect(jsonPath("$.telephone", is("999888777")))
				.andReturn().getResponse().getContentAsString();

		Integer id = JsonPath.parse(response).read("$.id");
		mockMvc.perform(delete("/owners/" + id)).andExpect(status().isOk());
	}

	@Test
	public void testCreateOwner_BadRequest() throws Exception {
		Map<String, Object> body = ownerBody("Sin", "x");
		body.remove("lastName");   // cuerpo invalido: falta lastName

		mockMvc.perform(post("/owners")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testFindByLastName() throws Exception {
		mockMvc.perform(get("/owners").param("lastName", "Davis"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].lastName", is("Davis")))
				.andExpect(jsonPath("$[1].lastName", is("Davis")));
	}

	@Test
	public void testUpdateOwner() throws Exception {
		Integer id = createOwner("Ana", "Lopez");

		Map<String, Object> upBody = ownerBody("Ana Maria", "Lopez Diaz");
		upBody.put("city", "Cusco");

		mockMvc.perform(put("/owners/" + id)
						.content(om.writeValueAsString(upBody))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk());

		mockMvc.perform(get("/owners/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(id)))
				.andExpect(jsonPath("$.firstName", is("Ana Maria")))
				.andExpect(jsonPath("$.lastName", is("Lopez Diaz")))
				.andExpect(jsonPath("$.city", is("Cusco")));

		mockMvc.perform(delete("/owners/" + id)).andExpect(status().isOk());
	}

	@Test
	public void testDeleteOwner() throws Exception {
		Integer id = createOwner("Borrar", "Prueba");

		mockMvc.perform(delete("/owners/" + id))
				.andExpect(status().isOk());

		mockMvc.perform(get("/owners/" + id))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testDeleteOwnerKO() throws Exception {
		mockMvc.perform(delete("/owners/1000"))
				.andExpect(status().isNotFound());
	}
}
