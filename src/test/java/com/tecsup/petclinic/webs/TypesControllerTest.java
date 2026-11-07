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
 * Pruebas de integracion (sin mocks) de TypeController: controlador -> repositorio -> H2.
 */
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class TypesControllerTest {

	private static final ObjectMapper om = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	private Integer createType(String name) throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("description", "tipo de prueba");
		body.put("sizeCategory", "small");
		body.put("averageLifespan", 5);
		body.put("careLevel", "low");

		String response = mockMvc.perform(post("/types")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.parse(response).read("$.id");
	}

	@Test
	public void testFindAllTypes() throws Exception {
		mockMvc.perform(get("/types"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(8))))
				.andExpect(jsonPath("$[0].id", is(1)));
	}

	@Test
	public void testFindTypeOK() throws Exception {
		mockMvc.perform(get("/types/1"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("cat")))
				.andExpect(jsonPath("$.active", is(true)));
	}

	@Test
	public void testFindTypeKO() throws Exception {
		mockMvc.perform(get("/types/666"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testCreateType() throws Exception {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", "parrot");
		body.put("description", "Talking bird");
		body.put("sizeCategory", "medium");
		body.put("averageLifespan", 40);
		body.put("careLevel", "high");

		String response = mockMvc.perform(post("/types")
						.content(om.writeValueAsString(body))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("parrot")))
				.andExpect(jsonPath("$.description", is("Talking bird")))
				.andExpect(jsonPath("$.averageLifespan", is(40)))
				.andExpect(jsonPath("$.active", is(true)))
				.andReturn().getResponse().getContentAsString();

		Integer id = JsonPath.parse(response).read("$.id");
		mockMvc.perform(delete("/types/" + id)).andExpect(status().isOk());
	}

	@Test
	public void testGetPetCountByType() throws Exception {
		// en data.sql hay 4 gatos (cat, id 1): pets 1, 7, 8 y 13
		mockMvc.perform(get("/types/report/pet-count"))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$[?(@.name == 'cat')].petCount", hasItem(greaterThanOrEqualTo(4))));
	}

	@Test
	public void testDeleteType() throws Exception {
		Integer id = createType("temp-delete");

		mockMvc.perform(delete("/types/" + id))
				.andExpect(status().isOk());

		mockMvc.perform(get("/types/" + id))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testFindActiveTypes() throws Exception {
		// 8 tipos en data.sql; snake (id 6) esta inactivo
		mockMvc.perform(get("/types").param("active", "true"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(7))))
				.andExpect(jsonPath("$[?(@.active == false)]", empty()))
				.andExpect(jsonPath("$[?(@.name == 'snake')]", empty()));
	}

	@Test
	public void testGetPetCountByType_ExcludeInactive() throws Exception {
		mockMvc.perform(get("/types/report/pet-count"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.name == 'snake')]", empty()));
	}

	@Test
	public void testDeleteTypeKO() throws Exception {
		mockMvc.perform(delete("/types/1000"))
				.andExpect(status().isNotFound());
	}
}
