package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.jms.BillingJmsSender;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private BillingServiceGrpcClient billingServiceGrpcClient;
    @Mock private KafkaProducer kafkaProducer;
    @Mock private BillingJmsSender billingJmsSender;

    @InjectMocks private PatientService patientService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void createPatient() {
        // Arrange
        PatientRequestDTO requestDTO = new PatientRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setEmail("johndoe@example.com");
        requestDTO.setAddress("Street 123");
        requestDTO.setDateOfBirth("1990-01-01");
        requestDTO.setRegisteredDate("2025-07-11");

        Patient savedPatient = PatientMapper.toEntity(requestDTO);
        savedPatient.setId(UUID.randomUUID());
        savedPatient.setRegisteredDate(LocalDate.now());

        when(patientRepository.existsByEmail(requestDTO.getEmail())).thenReturn(Boolean.FALSE);
        when(patientRepository.save(any(Patient.class))).thenReturn(savedPatient);

        // Act
        PatientResponseDTO responseDto = patientService.createPatient(requestDTO);

        // Assert
        assertThat(responseDto.getEmail()).isEqualTo("johndoe@example.com");
        verify(billingServiceGrpcClient).createBillingAccount(anyString(), eq("John Doe"), eq("johndoe@example.com"));
        verify(kafkaProducer).sendEvent(savedPatient);
        verify(billingJmsSender).sendCreateAccount(savedPatient);
    }

    @Test
    void getPatients() {
    }

    @Test
    void updatePatient() {
    }

    @Test
    void deletePatient() {
    }
}