package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.Manager;
import com.mercadolibre.bootcamp.projeto_integrador.model.Product;
import com.mercadolibre.bootcamp.projeto_integrador.model.Section;
import com.mercadolibre.bootcamp.projeto_integrador.model.Warehouse;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IManagerRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IProductRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.ISectionRepository;
import com.mercadolibre.bootcamp.projeto_integrador.repository.IWarehouseRepository;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InboundOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ISectionRepository sectionRepository;

    @Autowired
    private IWarehouseRepository warehouseRepository;

    @Autowired
    private IManagerRepository managerRepository;

    @Autowired
    private IProductRepository productRepository;

    @Autowired
    private DataSource dataSource;

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    static String asJsonString(final Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }

    @BeforeEach
    void cleanAllRepositories() throws SQLException {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.createStatement()
        ) {
            statement.execute("SET REFERENTIAL_INTEGRITY FALSE");
            context.getBeansOfType(JpaRepository.class).values().forEach(CrudRepository::deleteAll);
            statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }
    }

    @Test
    void createInboundOrder_returnsOk_whenIsGivenAValidInput() throws Exception {
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.excludeField(FieldPredicates.named("^.+(Code|Id|Number)$"));

        EasyRandom generator = new EasyRandom(parameters);
        var warehouse = generator.nextObject(Warehouse.class);
        var manager = generator.nextObject(Manager.class);
        var section = generator.nextObject(Section.class);
        section.setWarehouse(warehouse);
        section.setManager(manager);
        var product = generator.nextObject(Product.class);

        warehouseRepository.save(warehouse);
        managerRepository.save(manager);
        sectionRepository.save(section);
        productRepository.save(product);

        var batchRequest = generator.nextObject(BatchRequestDto.class);
        batchRequest.setProductId(product.getProductId());
        batchRequest.setDueDate(LocalDate.now().plusWeeks(1));
        batchRequest.setManufacturingDate(LocalDate.now());
        batchRequest.setManufacturingTime(LocalDateTime.now());

        var requestDto = new InboundOrderRequestDto();
        requestDto.setBatchStock(List.of(batchRequest));
        requestDto.setSectionCode(section.getSectionCode());

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
