# 🐾 PetClinic — Examen de Pruebas de Integración (Capa Web)

**Duración:** 2.5 horas | **Stack:** Spring Boot + JUnit 5 + Spring Test (`@SpringBootTest` + `MockMvc`) + Mockito + H2

---

## 📌 Reglas generales

- Repositorio en GitHub o GitLab. Rama `main` protegida.
- Cada integrante trabaja en su propia rama `feature/...`.
- Toda integración a `main` se hace por merge con al menos 1 revisor del equipo.
- Cada integrante debe tener commits propios con su nombre/correo real.
- Mínimo: **una rama por integrante, un merge fusionado por integrante y 1 conflicto resuelto**.
- Al final, ejecutar `./mvnw clean test -Dspring.profiles.active=h2` debe pasar sin errores.

---

## 🎯 Objetivo

Cada grupo debe escribir **pruebas de la capa web (controladores REST)** de su entidad asignada,
siguiendo **los dos patrones que ya trae el proyecto** como plantilla:

### 1. `XxxControllerTest` — Integración real (extremo a extremo)
- `@SpringBootTest` + `@AutoConfigureMockMvc` + `MockMvc`.
- **Sin mocks:** la petición HTTP atraviesa el stack real (controlador → servicio → repositorio → **H2**).
- Se apoya en los **datos iniciales** precargados por `schema.sql` / `data.sql` (ver Anexo).
- Plantilla de referencia: **`webs/PetControllerTest.java`**.

### 2. `XxxControllerMockitoTest` — Controlador aislado con servicio mockeado
- `@SpringBootTest` + `@AutoConfigureMockMvc` + `MockMvc`, pero con `@MockitoBean` del servicio.
- Se usa `Mockito.when(...)` / `Mockito.doNothing()` para definir la respuesta del servicio,
  de modo que solo se prueba la **lógica del controlador y el contrato JSON**, sin tocar la BD.
- Los objetos esperados se construyen con un helper tipo `TObjectCreator` (crea uno por entidad).
- Plantilla de referencia: **`webs/PetControllerMockitoTest.java`**.

> Cada integrante entrega **ambos** archivos para su bloque de endpoints: la versión de integración real
> y la versión con Mockito. Los mismos endpoints, probados de las dos formas.

---

## 🧩 Estructura esperada de archivos

```
src/test/java/com/tecsup/petclinic/
├── webs/
    ├── OwnerControllerTest.java
    ├── OwnerControllerMockitoTest.java
    ├── VetControllerTest.java
    ├── VetControllerMockitoTest.java
    ├── SpecialtyControllerTest.java
    ├── SpecialtyControllerMockitoTest.java
    ├── TypeControllerTest.java
    ├── TypeControllerMockitoTest.java
    ├── VisitControllerTest.java
    ├── VisitControllerMockitoTest.java
    ├── VetSpecialtyControllerTest.java
    └── VetSpecialtyControllerMockitoTest.java


```

---

## 👥 Escenarios para grupos de 3 integrantes

> Cada caso debe implementarse **dos veces**: en `XxxControllerTest` (integración real contra H2)
> y en `XxxControllerMockitoTest` (servicio mockeado con `@MockitoBean`).

### Grupo 1 — OwnerController (`/owners`)

**Integrante A — Listado y consulta**
- `testFindAllOwners` — `GET /owners` → `200 OK`, JSON, primer `id = 1`.
- `testFindOwnerOK` — `GET /owners/1` → `200 OK`, `jsonPath $.lastName = "Franklin"`.
- `testFindOwnerKO` — `GET /owners/666` → `404 Not Found`.

**Integrante B — Creación**
- `testCreateOwner` — `POST /owners` → `201 Created` y `jsonPath` de los campos enviados.
- `testCreateOwner_BadRequest` — cuerpo inválido (sin `lastName`) → `400 Bad Request`.
- `testFindByLastName` — `GET /owners?lastName=Davis` → 2 resultados.

**Integrante C — Actualización y borrado**
- `testUpdateOwner` — `POST` + `PUT /owners/{id}` → `200 OK`, luego `GET` confirma el cambio.
- `testDeleteOwner` — `POST` + `DELETE /owners/{id}` → `200 OK`.
- `testDeleteOwnerKO` — `DELETE /owners/1000` → `404 Not Found`.

---

### Grupo 2 — VetController (`/vets`)

**Integrante A — Listado y consulta**
- `testFindAllVets` — `GET /vets` → `200 OK` y lista JSON.
- `testFindVetOK` — `GET /vets/1` → `200 OK`, `jsonPath $.lastName = "Carter"`.
- `testFindVetKO` — `GET /vets/666` → `404 Not Found`.

**Integrante B — Creación y soft delete**
- `testCreateVet` — `POST /vets` → `201 Created`.
- `testDeactivateVet` — `PUT /vets/{id}/deactivate` → `200 OK` y `jsonPath $.active = false`.
- `testReactivateVet` — reactivar el vet 6 (inactivo) → `jsonPath $.active = true`.

**Integrante C — Filtros y borrado**
- `testFindActiveVets` — `GET /vets?active=true` → solo activos (5).
- `testFindInactiveVets` — `GET /vets?active=false` → solo inactivos (1).
- `testDeleteVetKO` — `DELETE /vets/1000` → `404 Not Found`.

---

### Grupo 3 — SpecialtyController (`/specialties`)

**Integrante A — Listado y consulta**
- `testFindAllSpecialties` — `GET /specialties` → `200 OK` y lista JSON.
- `testFindSpecialtyOK` — `GET /specialties/1` → `jsonPath $.name = "radiology"`.
- `testFindSpecialtyKO` — `GET /specialties/666` → `404 Not Found`.

**Integrante B — Creación y búsqueda**
- `testCreateSpecialty` — `POST /specialties` → `201 Created`.
- `testFindByName` — `GET /specialties?name=surgery` → la especialidad correcta.
- `testFindByOffice` — `GET /specialties?office=Farewell` → radiology.

**Integrante C — Validaciones y borrado**
- `testCreateSpecialty_InvalidSchedule` — `POST` con `h_open >= h_close` → `400 Bad Request`.
- `testCreateSpecialty_InvalidHourRange` — horas fuera de 0–23 → `400 Bad Request`.
- `testDeleteSpecialty` — `POST` + `DELETE /specialties/{id}` → `200 OK`.

---

### Grupo 6 — TypeController (`/types`)

**Integrante A — Listado y consulta**
- `testFindAllTypes` — `GET /types` → `200 OK` y lista JSON.
- `testFindTypeOK` — `GET /types/1` → `jsonPath $.name = "cat"`.
- `testFindTypeKO` — `GET /types/666` → `404 Not Found`.

**Integrante B — Creación y reporte**
- `testCreateType` — `POST /types` → `201 Created`.
- `testGetPetCountByType` — `GET /types/report/pet-count` → conteo por tipo (cat = 4).
- `testDeleteType` — `POST` + `DELETE /types/{id}` → `200 OK`.

**Integrante C — Filtros y errores**
- `testFindActiveTypes` — `GET /types?active=true` → 7 tipos (excluye snake).
- `testGetPetCountByType_ExcludeInactive` — el reporte excluye tipos `active=false`.
- `testDeleteTypeKO` — `DELETE /types/1000` → `404 Not Found`.

---

## 👥 Escenarios para grupos de 4 integrantes

### Grupo 4 — VisitController (`/visits`)

**Integrante A — Listado y consulta**
- `testFindAllVisits` — `GET /visits` → `200 OK` y lista JSON.
- `testFindVisitOK` — `GET /visits/1` → `jsonPath $.description = "rabies shot"`.
- `testFindVisitKO` — `GET /visits/666` → `404 Not Found`.

**Integrante B — Creación y borrado**
- `testCreateVisit` — `POST /visits` → `201 Created`.
- `testUpdateVisit` — `POST` + `PUT /visits/{id}` → `200 OK` y cambio confirmado.
- `testDeleteVisit` — `POST` + `DELETE /visits/{id}` → `200 OK`.

**Integrante C — Consultas por relación**
- `testFindByPetId` — `GET /visits?petId=7` → 2 visitas (Samantha).
- `testFindByVetId` — `GET /visits?vetId=2` → visitas del vet James.
- `testCalculateTotalCostByPet` — `GET /visits/pet/{id}/total-cost` → suma `BigDecimal`.

**Integrante D — Consultas por fecha**
- `testFindByDateBetween` — `GET /visits?from=2024-01-01&to=2024-12-31` → 2 visitas (ids 5 y 6).
- `testFindByVetIdAndDateBetween` — visitas de un vet en un rango.
- `testCountVisitsByPet` — `GET /visits/pet/{id}/count` → conteo de visitas.

---

### Grupo 5 — VetSpecialtyController (`/vet-specialties`)

**Integrante A — Asignación**
- `testAssignSpecialtyToVet` — `POST /vet-specialties` → `201 Created`.
- `testRemoveSpecialtyFromVet` — `DELETE /vet-specialties/{vetId}/{specId}` → `200 OK`.
- `testAssignDuplicate_ShouldFail` — asignar la misma combinación dos veces → `409 Conflict`.

**Integrante B — Consultas**
- `testFindSpecialtiesByVet` — `GET /vets/3/specialties` → 2 especialidades (surgery, dentistry).
- `testFindVetsBySpecialty` — `GET /specialties/1/vets` → vets 2 y 5.
- `testFindSpecialtiesByVet_Empty` — vet sin asignaciones → lista vacía.

**Integrante C — Especialidad principal**
- `testSetPrimarySpecialty` — `PUT /vet-specialties/{vetId}/{specId}/primary` → `200 OK`.
- `testSetPrimarySpecialty_OnlyOnePrimary` — al marcar una nueva, las demás quedan `false`.
- `testSetPrimarySpecialty_NotAssigned` — especialidad no asignada → `404 Not Found`.

**Integrante D — Experiencia y certificaciones**
- `testFindVetsByMinExperience` — `GET /specialties/1/vets?minExperience=7` → vets 2 y 5.
- `testUpdateYearsExperience` — `PUT /vet-specialties/{vetId}/{specId}/experience` → `200 OK`.
- `testUpdateCertificationDate` — `PUT /vet-specialties/{vetId}/{specId}/certification` → `200 OK`.

---

## 🧪 Ejemplo de referencia — Integración real (patrón `PetControllerTest`)

```java
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class OwnerControllerTest {

    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;          // golpea H2 real, SIN mocks

    @Test
    public void testFindOwnerOK() throws Exception {
        this.mockMvc.perform(get("/owners/1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.lastName", is("Franklin")));
    }

    @Test
    public void testFindOwnerKO() throws Exception {
        mockMvc.perform(get("/owners/666"))
                .andExpect(status().isNotFound());
    }
}
```

## 🧪 Ejemplo de referencia — Servicio mockeado (patrón `PetControllerMockitoTest`)

```java
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class OwnerControllerMockitoTest {

    private static final ObjectMapper om = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerService ownerService;   // servicio mockeado

    @Test
    public void testFindOwnerOK() throws Exception {
        OwnerDTO ownerTO = TObjectCreator.getOwnerTO();

        Mockito.when(ownerService.findById(ownerTO.getId()))
                .thenReturn(ownerTO);

        mockMvc.perform(get("/owners/1"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(ownerTO.getId())))
                .andExpect(jsonPath("$.lastName", is(ownerTO.getLastName())));
    }

    @Test
    public void testFindOwnerKO() throws Exception {
        Integer ID_NOT_EXIST = 666;

        Mockito.when(this.ownerService.findById(ID_NOT_EXIST))
                .thenThrow(new OwnerNotFoundException("Record not found...!"));

        mockMvc.perform(get("/owners/666"))
                .andExpect(status().isNotFound());
    }
}
```

---

## 📏 Evaluación

| Criterio                                                                         | Puntos |
|----------------------------------------------------------------------------------|--------|
| Uso de Git (ramas, merge, revisiones)                                            | 6      |
| Cada integrante completó sus pruebas asignadas (ambos archivos: real + Mockito)  | 4      |
| Resolución de conflictos                                                         | 3      |
| Calidad (asserts con `jsonPath`, códigos HTTP correctos, datos semilla bien usados) | 4   |
| Las pruebas pasan (`./mvnw clean test -Dspring.profiles.active=h2`)              | 3      |

---

## 📦 Entregables

1. Enlace al repositorio.
2. Captura de `git log --all --graph --oneline`.
3. Captura de la ejecución verde de `./mvnw clean test -Dspring.profiles.active=h2`.
4. Sección al final del README del repo con: integrantes, pruebas que hizo cada uno y conflictos resueltos.

