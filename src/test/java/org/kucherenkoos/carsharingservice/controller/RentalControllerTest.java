package org.kucherenkoos.carsharingservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kucherenkoos.carsharingservice.dto.rental.CreateRentalRequestDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalDetailDto;
import org.kucherenkoos.carsharingservice.dto.rental.RentalResponseDto;
import org.kucherenkoos.carsharingservice.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RentalService rentalService;

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("Create a new rental - Success")
    void createRental_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto();

        requestDto.setCarId(1L);
        requestDto.setRentalDate(LocalDate.now());
        requestDto.setReturnDate(LocalDate.now().plusDays(3));

        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(1L);

        when(rentalService.createRental(any(CreateRentalRequestDto.class))).thenReturn(responseDto);

        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("Get rentals with pagination and filters - Success")
    void getRentals_ValidParams_ReturnsPage() throws Exception {
        // Given
        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(1L);

        Page<RentalResponseDto> mockPage = new PageImpl<>(List.of(responseDto));

        when(rentalService.getRentals(eq(1L), eq(true), any(Pageable.class))).thenReturn(mockPage);

        // When & Then
        mockMvc.perform(get("/rentals")
                        .param("user_id", "1")
                        .param("is_active", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("Get rental by ID - Success")
    void getRentalById_ValidId_ReturnsDetail() throws Exception {
        // Given
        Long rentalId = 1L;
        RentalDetailDto detailDto = new RentalDetailDto();
        detailDto.setId(rentalId);

        when(rentalService.getRentalById(rentalId)).thenReturn(detailDto);

        // When & Then
        mockMvc.perform(get("/rentals/{id}", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rentalId));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("Return rental by ID - Success")
    void returnRental_ValidId_ReturnsResponse() throws Exception {
        // Given
        Long rentalId = 1L;
        RentalResponseDto responseDto = new RentalResponseDto();
        responseDto.setId(rentalId);

        when(rentalService.returnRental(rentalId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/rentals/{id}/return", rentalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(rentalId));
    }

    @Test
    @DisplayName("Unauthorized access throws 401")
    void createRental_Unauthenticated_ThrowsUnauthorized() throws Exception {
        // Given
        CreateRentalRequestDto requestDto = new CreateRentalRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        // When & Then
        mockMvc.perform(post("/rentals")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
