package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.util.ManagerGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerServiceTest {
    @InjectMocks
    private ManagerService managerService;

    @Mock
    IManagerRepository managerRepository;

    @Test
    void findById_returnsException_whenManagerNoExist() {
        // Arrange
        long managerIdNotRegistered = 99l;
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> managerService.findById(managerIdNotRegistered)
        );

        // Assert
        assertThat(exception.getMessage()).isEqualTo("Manager with id " + managerIdNotRegistered + " not found");
    }

    @Test
    void findById_returnsException_whenManagerExist() {
        // Arrange
        Manager manager = ManagerGenerator.getManagerWithId();
        long managerIdRegistered = manager.getManagerId();
        when(managerRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(manager));

        // Act
        Manager managerResponse = managerService.findById(managerIdRegistered);

        // Assert
        assertThat(managerResponse).isEqualTo(manager);
    }
}
