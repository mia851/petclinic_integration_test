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
 * Pruebas de integracion (sin mocks) de SpecialtyController: controlador -> repositorio -> H2.
 */
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class SpecialtyControllerTest {

	private static final ObjectMapper om = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	private Map<String, Object> specialtyBody(String name, String office, int hOpen, int hClose) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("name", name);
		body.put("office", office);
		body.put("h_open", hOpen);
		body.put("h_close", hClose);
		return body;
	}

	@Test
	public void testFindAllSpecialties() throws Exception {
		mockMvc.perform(get("/specialties"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(3))))
				.andExpect(jsonPath("$[0].id", is(1)));
	}

	@Test
	public void testFindSpecialtyOK() throws Exception {
		mockMvc.perform(get("/specialties/1"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.name", is("radiology")))
				.andExpect(jsonPath("$.office", is("Farewell")))
				.andExpect(jsonPath("$.h_open", is(8)))
				.andExpect(jsonPath("$.h_close", is(18)));
	}

	@Test
	public void testFindSpecialtyKO() throws Exception {
		mockMvc.perform(get("/specialties/666"))
				.andExpect(status().isNotFound());
	}

	@Test
	public void testCreateSpecialty() throws Exception {
		String response = mockMvc.perform(post("/specialties")
						.content(om.writeValueAsString(specialtyBody("cardiology", "Lima Norte", 9, 17)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andDo(print())
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name", is("cardiology")))
				.andExpect(jsonPath("$.office", is("Lima Norte")))
				.andExpect(jsonPath("$.h_open", is(9)))
				.andExpect(jsonPath("$.h_close", is(17)))
				.andReturn().getResponse().getContentAsString();

		Integer id = JsonPath.parse(response).read("$.id");
		mockMvc.perform(delete("/specialties/" + id)).andExpect(status().isOk());
	}

	@Test
	public void testFindByName() throws Exception {
		mockMvc.perform(get("/specialties").param("name", "surgery"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id", is(2)))
				.andExpect(jsonPath("$[0].name", is("surgery")));
	}

	@Test
	public void testFindByOffice() throws Exception {
		mockMvc.perform(get("/specialties").param("office", "Farewell"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].name", is("radiology")));
	}

	@Test
	public void testCreateSpecialty_InvalidSchedule() throws Exception {
		// h_open >= h_close
		mockMvc.perform(post("/specialties")
						.content(om.writeValueAsString(specialtyBody("invalid-schedule", "Sala 1", 18, 8)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testCreateSpecialty_InvalidHourRange() throws Exception {
		// horas fuera del rango 0-23
		mockMvc.perform(post("/specialties")
						.content(om.writeValueAsString(specialtyBody("invalid-hours", "Sala 2", -1, 25)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testDeleteSpecialty() throws Exception {
		String response = mockMvc.perform(post("/specialties")
						.content(om.writeValueAsString(specialtyBody("temp-delete", "Sala 3", 8, 12)))
						.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		Integer id = JsonPath.parse(response).read("$.id");

		mockMvc.perform(delete("/specialties/" + id))
				.andExpect(status().isOk());

		mockMvc.perform(get("/specialties/" + id))
				.andExpect(status().isNotFound());
	}
}
