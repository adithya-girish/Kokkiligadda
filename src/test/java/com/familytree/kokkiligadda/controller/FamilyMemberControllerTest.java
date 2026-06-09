package com.familytree.kokkiligadda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.familytree.kokkiligadda.dto.FamilyTreeNode;
import com.familytree.kokkiligadda.exception.ResourceNotFoundException;
import com.familytree.kokkiligadda.model.FamilyMember;
import com.familytree.kokkiligadda.model.Marriage;
import com.familytree.kokkiligadda.service.FamilyMemberService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FamilyMemberController.class)
class FamilyMemberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  FamilyMemberService service;

    private FamilyMember member;
    private FamilyTreeNode treeNode;

    @BeforeEach
    void setUp() {
        member = FamilyMember.builder().id("id-001").firstName("Ravi")
                .lastName("Kokkiligadda").name("Ravi Kokkiligadda")
                .gender("MALE").type("BLOOD").gotram("Kasyapa").build();
        treeNode = FamilyTreeNode.from(member);
        treeNode.setChildren(List.of());
        treeNode.setHasChildren(false);
    }

    @Nested @DisplayName("CRUD")
    class CrudTests {
        @Test void create201() throws Exception {
            when(service.create(any())).thenReturn(member);
            mockMvc.perform(post("/api/family-members").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(member)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value("id-001"));
        }
        @Test void create400MissingFirstName() throws Exception {
            when(service.create(any())).thenThrow(new IllegalArgumentException("First name is required"));
            mockMvc.perform(post("/api/family-members").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"lastName\":\"K\"}"))
                    .andExpect(status().isBadRequest());
        }
        @Test void getAll200() throws Exception {
            when(service.getAll()).thenReturn(List.of(member));
            mockMvc.perform(get("/api/family-members")).andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value("id-001"));
        }
        @Test void getByLastName() throws Exception {
            when(service.getByLastName("K")).thenReturn(List.of(member));
            mockMvc.perform(get("/api/family-members").param("lastName", "K"))
                    .andExpect(status().isOk());
        }
        @Test void getById200() throws Exception {
            when(service.getById("id-001")).thenReturn(member);
            mockMvc.perform(get("/api/family-members/id-001")).andExpect(status().isOk());
        }
        @Test void getById404() throws Exception {
            when(service.getById("x")).thenThrow(new ResourceNotFoundException("not found"));
            mockMvc.perform(get("/api/family-members/x")).andExpect(status().isNotFound());
        }
        @Test void update200() throws Exception {
            when(service.update(eq("id-001"), any())).thenReturn(member);
            mockMvc.perform(put("/api/family-members/id-001").contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(member))).andExpect(status().isOk());
        }
        @Test void delete204() throws Exception {
            doNothing().when(service).delete("id-001");
            mockMvc.perform(delete("/api/family-members/id-001")).andExpect(status().isNoContent());
        }
        @Test void delete404() throws Exception {
            doThrow(new ResourceNotFoundException("not found")).when(service).delete("x");
            mockMvc.perform(delete("/api/family-members/x")).andExpect(status().isNotFound());
        }
    }

    @Nested @DisplayName("Children")
    class ChildrenTests {
        @Test void getChildren200() throws Exception {
            when(service.getChildren("id-001")).thenReturn(List.of(member));
            mockMvc.perform(get("/api/family-members/id-001/children")).andExpect(status().isOk());
        }
        @Test void addChild200() throws Exception {
            when(service.addChild("id-001", "c1")).thenReturn(member);
            mockMvc.perform(post("/api/family-members/id-001/children/c1")).andExpect(status().isOk());
        }
        @Test void removeChild200() throws Exception {
            when(service.removeChild("id-001", "c1")).thenReturn(member);
            mockMvc.perform(delete("/api/family-members/id-001/children/c1")).andExpect(status().isOk());
        }
    }

    @Nested @DisplayName("Marriages")
    class MarriageTests {
        @Test void getMarriages200() throws Exception {
            Marriage m = Marriage.builder().partnerName("Lakshmi").relationshipType("MARRIED").build();
            when(service.getMarriages("id-001")).thenReturn(List.of(m));
            mockMvc.perform(get("/api/family-members/id-001/marriages"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$[0].partnerName").value("Lakshmi"));
        }
        @Test void addMarriage201() throws Exception {
            Marriage m = Marriage.builder().partnerName("Lakshmi").order(1).build();
            when(service.addMarriage(eq("id-001"), any())).thenReturn(member);
            mockMvc.perform(post("/api/family-members/id-001/marriages")
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(m)))
                    .andExpect(status().isCreated());
        }
        @Test void setAllMarriages200() throws Exception {
            when(service.setAllMarriages(eq("id-001"), any())).thenReturn(member);
            mockMvc.perform(put("/api/family-members/id-001/marriages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(List.of(
                            Marriage.builder().partnerName("Lakshmi").build()))))
                    .andExpect(status().isOk());
        }
        @Test void updateMarriage200() throws Exception {
            when(service.updateMarriage(eq("id-001"), eq(0), any())).thenReturn(member);
            mockMvc.perform(put("/api/family-members/id-001/marriages/0")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            Marriage.builder().partnerName("Updated").build())))
                    .andExpect(status().isOk());
        }
        @Test void removeMarriage200() throws Exception {
            when(service.removeMarriage("id-001", 0)).thenReturn(member);
            mockMvc.perform(delete("/api/family-members/id-001/marriages/0")).andExpect(status().isOk());
        }
        @Test void removeAllMarriages200() throws Exception {
            when(service.removeAllMarriages("id-001")).thenReturn(member);
            mockMvc.perform(delete("/api/family-members/id-001/marriages")).andExpect(status().isOk());
        }
        @Test void addDuplicateMarriage400() throws Exception {
            when(service.addMarriage(eq("id-001"), any()))
                    .thenThrow(new IllegalArgumentException("Marriage with partnerId p2 already exists"));
            mockMvc.perform(post("/api/family-members/id-001/marriages")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"partnerId\":\"p2\",\"partnerName\":\"Lakshmi\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested @DisplayName("Parents")
    class ParentTests {
        @Test void setFather200() throws Exception {
            when(service.setFather("c1", "id-001")).thenReturn(member);
            mockMvc.perform(put("/api/family-members/c1/father/id-001")).andExpect(status().isOk());
        }
        @Test void setMother200() throws Exception {
            when(service.setMother("c1", "id-001")).thenReturn(member);
            mockMvc.perform(put("/api/family-members/c1/mother/id-001")).andExpect(status().isOk());
        }
    }

    @Nested @DisplayName("Tree Navigation")
    class TreeTests {
        @Test void getRootExists() throws Exception {
            when(service.getRootNode()).thenReturn(Optional.of(treeNode));
            mockMvc.perform(get("/api/family-members/tree/root")).andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("id-001"));
        }
        @Test void getRootEmpty() throws Exception {
            when(service.getRootNode()).thenReturn(Optional.empty());
            mockMvc.perform(get("/api/family-members/tree/root")).andExpect(status().isNoContent());
        }
        @Test void createRoot201() throws Exception {
            member.setRoot(true);
            when(service.createRoot(any())).thenReturn(member);
            mockMvc.perform(post("/api/family-members/tree/root")
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(member)))
                    .andExpect(status().isCreated()).andExpect(jsonPath("$.root").value(true));
        }
        @Test void createRootConflict400() throws Exception {
            when(service.createRoot(any())).thenThrow(
                    new IllegalArgumentException("A root node already exists. Only one root is allowed."));
            mockMvc.perform(post("/api/family-members/tree/root")
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(member)))
                    .andExpect(status().isBadRequest());
        }
        @Test void getNode200WithChildren() throws Exception {
            FamilyMember c = FamilyMember.builder().id("c1").firstName("S").lastName("K").build();
            treeNode.setChildren(List.of(FamilyTreeNode.from(c)));
            treeNode.setHasChildren(true);
            when(service.getNode("id-001")).thenReturn(treeNode);
            mockMvc.perform(get("/api/family-members/tree/node/id-001"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.hasChildren").value(true));
        }
        @Test void getNode404() throws Exception {
            when(service.getNode("x")).thenThrow(new ResourceNotFoundException("not found"));
            mockMvc.perform(get("/api/family-members/tree/node/x")).andExpect(status().isNotFound());
        }
        @Test void getChildNodes200() throws Exception {
            when(service.getChildNodes("id-001")).thenReturn(List.of(treeNode));
            mockMvc.perform(get("/api/family-members/tree/node/id-001/children")).andExpect(status().isOk());
        }
        @Test void getChildNodesEmpty() throws Exception {
            when(service.getChildNodes("id-001")).thenReturn(List.of());
            mockMvc.perform(get("/api/family-members/tree/node/id-001/children"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested @DisplayName("Details")
    class DetailTests {
        @Test void updateContact() throws Exception {
            when(service.patch(eq("id-001"), any())).thenReturn(member);
            mockMvc.perform(patch("/api/family-members/id-001/contact")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"phoneNumber\":\"9876543210\",\"email\":\"r@t.com\"}"))
                    .andExpect(status().isOk());
        }
        @Test void updateDocuments() throws Exception {
            when(service.patch(eq("id-001"), any())).thenReturn(member);
            mockMvc.perform(patch("/api/family-members/id-001/documents")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"aadhaar\":\"1234\",\"pan\":\"AB\"}"))
                    .andExpect(status().isOk());
        }
        @Test void updatePersonal() throws Exception {
            when(service.patch(eq("id-001"), any())).thenReturn(member);
            mockMvc.perform(patch("/api/family-members/id-001/personal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"gotram\":\"Kasyapa\"}"))
                    .andExpect(status().isOk());
        }
    }
}
