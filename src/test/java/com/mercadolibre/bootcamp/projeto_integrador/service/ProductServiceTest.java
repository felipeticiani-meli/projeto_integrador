package com.mercadolibre.bootcamp.projeto_integrador.service;

import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import com.mercadolibre.bootcamp.projeto_integrador.util.BatchGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {
    @InjectMocks
    private ProductService productService;

    @Mock
    IProductRepository productRepository;

    @Test
    void getProductMap_returnsException_whenProductNoExist() {
        // Arrange
        when(productRepository.findAllById(ArgumentMatchers.anyList()))
                .thenReturn(new ArrayList<>());

        // Act
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productService.getProductMap(BatchGenerator.newList2BatchRequestsDTO())
        );

        // Assert
        assertThat(exception.getMessage()).isEqualTo("There is no product with the specified id");
        // TODO o teste não passa, é para ser assim o comportamento do getProductMap?
    }
}
