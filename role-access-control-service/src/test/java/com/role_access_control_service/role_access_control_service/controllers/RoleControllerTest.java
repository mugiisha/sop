package com.role_access_control_service.role_access_control_service.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.role_access_control_service.role_access_control_service.dtos.CreateRoleDto;
import com.role_access_control_service.role_access_control_service.models.Role;
import com.role_access_control_service.role_access_control_service.utils.Response;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Transactional
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateRoleDto createRoleDto;

    @BeforeEach
    void setUp() {
        createRoleDto = new CreateRoleDto();
        createRoleDto.setRoleName("TEST ROLE");
    }

    @Test
    void shouldCreateRole() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role created successfully"))
                .andExpect(jsonPath("$.data.roleName").value("TEST ROLE"));
    }

    @Test
    void shouldGetRoleById() throws Exception {
        // First, create a role
        String response = mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoleDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Response<Role> createdRoleResponse = objectMapper.readValue(response, new TypeReference<Response<Role>>() {});
        UUID roleId = createdRoleResponse.getData().getId();

        // Then, retrieve the role by ID
        mockMvc.perform(get("/api/v1/roles/" + roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(roleId.toString()))
                .andExpect(jsonPath("$.data.roleName").value("TEST ROLE"));
    }

    @Test
    void shouldUpdateRole() throws Exception {
        // First, create a role
        String response = mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoleDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Response<Role> createdRoleResponse = objectMapper.readValue(response, new TypeReference<Response<Role>>() {});
        UUID roleId = createdRoleResponse.getData().getId();

        // Update the role
        createRoleDto.setRoleName("Updated Role");
        mockMvc.perform(put("/api/v1/roles/" + roleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role updated successfully"))
                .andExpect(jsonPath("$.data.roleName").value("UPDATED ROLE"));
    }

    @Test
    void shouldDeleteRole() throws Exception {
        // First, create a role
        String response = mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoleDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Response<Role> createdRoleResponse = objectMapper.readValue(response, new TypeReference<Response<Role>>() {});
        UUID roleId = createdRoleResponse.getData().getId();

        // Delete the role
        mockMvc.perform(delete("/api/v1/roles/" + roleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Role deleted successfully"));
    }

    @Test
    void shouldGetAllRoles() throws Exception {
        // Create a role
        mockMvc.perform(post("/api/v1/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoleDto)))
                .andExpect(status().isOk());

        // Retrieve all roles
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles retrieved successfully"))
                .andExpect(jsonPath("$.data").isArray());
    }
}