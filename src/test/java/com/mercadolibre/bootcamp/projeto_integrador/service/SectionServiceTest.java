package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.ISectionRepository;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.ManagerGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.SectionGenerator;
import com.mercadolibre.bootcamp.projeto_integrador.util.WarehouseGenerator;
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
public class SectionServiceTest {
    @InjectMocks
    private SectionService sectionService;

    @Mock
    ISectionRepository sectionRepository;

    @Mock
    IManagerService managerService;

    @Test
    void findById_returnsException_whenSectionNoExist() {
        // Arrange
        when(sectionRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> sectionService.findById(1l)
        );

        // Assert
        assertThat(exception.getMessage()).isEqualTo("There is no section with the specified id");
    }

    @Test
    void update_returnsException_whenSectionHasNoSpace() {
        // Arrange
        when(sectionRepository.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(SectionGenerator.getCrowdedFreshSection()));
        when(managerService.findById(ArgumentMatchers.anyLong()))
                .thenReturn(ManagerGenerator.getManagerWithId());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> sectionService.update(SectionGenerator.getCrowdedFreshSection(),
                            BatchGenerator.newList2BatchRequestsDTO(),1l)
        );

        // Assert
        assertThat(exception.getMessage()).isEqualTo("Section does not have enough space");
    }

    @Test
    void update_returnsException_whenManagerNotHavePermission() {
        // Arrange
        Section section = SectionGenerator.getFreshSection(WarehouseGenerator.newWarehouse(),
                ManagerGenerator.getManagerWithId());
        Manager unauthorizedManager = ManagerGenerator.newManager();
        unauthorizedManager.setManagerId(2l);
        when(managerService.findById(ArgumentMatchers.anyLong()))
                .thenReturn(unauthorizedManager);
        when(sectionRepository.save(ArgumentMatchers.any(Section.class)))
                .thenReturn(section);

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> sectionService.update(section, BatchGenerator.newList2BatchRequestsDTO(),
                            unauthorizedManager.getManagerId())
        );

        // Assert
        assertThat(exception.getMessage()).contains("is not authorized to perform this action.");
        assertThat(exception.getMessage()).contains(unauthorizedManager.getName());
    }

}
