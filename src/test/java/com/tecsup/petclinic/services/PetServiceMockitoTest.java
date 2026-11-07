package com.tecsup.petclinic.services;

import com.tecsup.petclinic.dtos.PetDTO;
import com.tecsup.petclinic.entities.Pet;
import com.tecsup.petclinic.exceptions.PetNotFoundException;
import com.tecsup.petclinic.mapper.PetMapper;
import com.tecsup.petclinic.repositories.PetRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class PetServiceMockitoTest {

    @Autowired
    private PetService petService;

    @Autowired
    private PetMapper petMapper;

    @MockitoBean
    private PetRepository repository;


    @BeforeEach
    void setUp() {
    }

    /**
     *
     */
    @Test
    public void testFindPetById() {

        Pet petExpected = Pet.builder().id(1).name("Leo").typeId(1).ownerId(1).build();

        Mockito.when(this.repository.findById(1))
                .thenReturn((Optional.of(petExpected)));
        PetDTO pet = null;

        try {
            pet = this.petService.findById(1);
        } catch (PetNotFoundException e) {
            fail(e.getMessage());
        }

        log.info("" + petExpected);
        log.info("" + pet);
        assertEquals(petExpected.getName(), pet.getName());

    }

    /**
     *
     */
    @Test
    public void testFindPetByName() {

        String FIND_NAME = "Leo";

        List<Pet> petsExpected = List.of(
                Pet.builder().id(1).name("Leo").typeId(1).ownerId(1).build());

        Mockito.when(this.repository.findByName(FIND_NAME))
                .thenReturn(petsExpected);

        List<PetDTO> pets = this.petService.findByName(FIND_NAME);

        assertEquals(petsExpected.size(), pets.size());
    }

    /**
     *
     */
    @Test
    public void testFindPetByTypeId() {

        int TYPE_ID = 5;

        List<Pet> petsExpected = List.of(
                Pet.builder().id(9).name("Lucky").typeId(5).ownerId(7).build(),
                Pet.builder().id(11).name("Freddy").typeId(5).ownerId(9).build());

        Mockito.when(this.repository.findByTypeId(TYPE_ID))
                .thenReturn(petsExpected);

        List<Pet> pets = this.petService.findByTypeId(TYPE_ID);

        assertEquals(petsExpected.size(), pets.size());
    }

    /**
     *
     */
    @Test
    public void testFindPetByOwnerId() {

        int OWNER_ID = 10;

        List<Pet> petsExpected = List.of(
                Pet.builder().id(12).name("Lucky").typeId(2).ownerId(10).build(),
                Pet.builder().id(13).name("Sly").typeId(1).ownerId(10).build());

        Mockito.when(this.repository.findByOwnerId(OWNER_ID))
                .thenReturn(petsExpected);

        List<Pet> pets = this.petService.findByOwnerId(OWNER_ID);

        assertEquals(petsExpected.size(), pets.size());

    }

    /**
     * To get ID generate , you need
     * setup in id primary key in your
     * entity this annotation :
     *
     * @GeneratedValue(strategy = GenerationType.IDENTITY)
     */
    @Test
    public void testCreatePet() {


        Pet newPet = Pet.builder().id(0).name("Punky").typeId(1).ownerId(1).build();
        Pet newPetCreated = Pet.builder().id(1000).name("Punky").typeId(1).ownerId(1).build();

        PetDTO newPetDTO = this.petMapper.mapToDto(newPet);
        PetDTO hopePetDTOCreated = this.petMapper.mapToDto(newPetCreated);

        Mockito.when(this.repository.save(newPet))
                .thenReturn(newPetCreated);

        PetDTO newPetDTOCreated = this.petService.create(newPetDTO);

        log.info("Pet created : {}" , newPetDTOCreated);

        assertNotNull(newPetDTOCreated.getId());
        assertEquals(hopePetDTOCreated.getName(), newPetDTOCreated.getName());
        assertEquals(hopePetDTOCreated.getOwnerId(), newPetDTOCreated.getOwnerId());
        assertEquals(hopePetDTOCreated.getTypeId(), newPetDTOCreated.getTypeId());

    }


    /**
     *
     */
    @Test
    public void testUpdatePet() {

        String UP_PET_NAME = "Bear2";
        int UP_OWNER_ID = 2;
        int UP_TYPE_ID = 2;

        Pet newPet = Pet.builder().id(0).name("Bear").typeId(1).ownerId(1).build();
        Pet newPetCreate = Pet.builder().id(4000).name("Bear").typeId(1).ownerId(1).build();

        PetDTO newPetDTO = petMapper.mapToDto(newPet);
        PetDTO hopePetDTOCreate = petMapper.mapToDto(newPetCreate);


        // ------------ Create ---------------

        Mockito.when(this.repository.save(newPet))
                .thenReturn(newPetCreate);

        PetDTO newPetDTOCreate = this.petService.create(newPetDTO);
        log.info("{}" , newPetDTOCreate);

        // ------------ Update ---------------

        // Prepare data for update
        newPetDTOCreate.setName(UP_PET_NAME);
        newPetDTOCreate.setOwnerId(UP_OWNER_ID);
        newPetDTOCreate.setTypeId(UP_TYPE_ID);

        PetDTO newPetDTOUpdate = newPetDTOCreate;
        Pet newPetUpdate = this.petMapper.mapToEntity(newPetDTOUpdate);

        // Create
        Mockito.when(this.repository.save(newPetUpdate))
                .thenReturn(newPetUpdate);

        // Execute update
        PetDTO petDTOUpdate = this.petService.update(newPetDTOCreate);
        log.info("{}" + petDTOUpdate);

        //            EXPECTED           ACTUAL
        assertEquals(UP_PET_NAME, petDTOUpdate.getName());
        assertEquals(UP_OWNER_ID, petDTOUpdate.getTypeId());
        assertEquals(UP_TYPE_ID, petDTOUpdate.getOwnerId());
    }

    /**
     *
     */
    @Test
    public void testDeletePet() {

        Pet newPet = Pet.builder().id(0).name("Bird").typeId(1).ownerId(1).build();
        Pet newPetCreate = Pet.builder().id(2000).name("Bird").typeId(1).ownerId(1).build();

        PetDTO newPetDTO = this.petMapper.mapToDto(newPet);

        // ------------ Create ---------------

        Mockito.when(this.repository.save(newPet))
                .thenReturn(newPetCreate);

        PetDTO petDTOCreate = this.petService.create(newPetDTO);
        log.info("{}" ,petDTOCreate);

        // ------------ Delete ---------------

        Mockito.doNothing().when(this.repository).delete(newPetCreate);
        Mockito.when(this.repository.findById(newPetCreate.getId()))
                .thenReturn(Optional.of(newPetCreate));

        try {
            this.petService.delete(petDTOCreate.getId());
        } catch (PetNotFoundException e) {
            fail(e.getMessage());
        }

        // ------------ Validate ---------------

        Mockito.when(this.repository.findById(newPetCreate.getId()))
                .thenReturn(Optional.ofNullable(null));

        try {
            this.petService.findById(petDTOCreate.getId());
            assertTrue(false);
        } catch (PetNotFoundException e) {
            assertTrue(true);
        }

    }

}