package com.mercadolibre.bootcamp.projeto_integrador.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mercadolibre.bootcamp.projeto_integrador.dto.BatchRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.dto.InboundOrderRequestDto;
import com.mercadolibre.bootcamp.projeto_integrador.model.*;
import com.mercadolibre.bootcamp.projeto_integrador.repository.*;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.jeasy.random.FieldPredicates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class InboundOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DataSource dataSource;

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
    private IBatchRepository batchRepository;

    @Autowired
    private IInboundOrderRepository inboundOrderRepository;

    private static final ObjectMapper objectMapper;

    private EasyRandom generator;

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
        EasyRandomParameters parameters = new EasyRandomParameters();
        parameters.excludeField(FieldPredicates.named("^.+(Code|Id|Number)$"));

        generator = new EasyRandom(parameters);
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
    }

    @Test
    void createInboundOrder_returnsOk_whenIsGivenAValidInput() throws Exception {
        var batchRequest = generator.nextObject(BatchRequestDto.class);
        batchRequest.setProductId(1L);
        batchRequest.setDueDate(LocalDate.now().plusWeeks(1));
        batchRequest.setManufacturingDate(LocalDate.now());
        batchRequest.setManufacturingTime(LocalDateTime.now());

        var requestDto = new InboundOrderRequestDto();
        requestDto.setBatchStock(List.of(batchRequest));
        requestDto.setSectionCode(1L);

        mockMvc.perform(post("/api/v1/fresh-products/inboundorder")
                .content(asJsonString(requestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void putUpdateInboundOrder_returnCreated_whenBatchExists() throws Exception {
        var batchRequest = generator.nextObject(BatchRequestDto.class);
        batchRequest.setManufacturingTime(LocalDateTime.now());
        batchRequest.setManufacturingDate(LocalDate.now());
        batchRequest.setDueDate(LocalDate.of(2022, 9,01));
        batchRequest.setProductId(1L);

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(BatchRequestDto.class, Batch.class).addMappings(mapper -> {
            mapper.map(BatchRequestDto::getProductId, Batch::setProduct);
        });
        Batch batch = modelMapper.map(batchRequest, Batch.class);

        batchRepository.save(batch);

        InboundOrderRequestDto inboundOrderRequestDto = new InboundOrderRequestDto();
        inboundOrderRequestDto.setSectionCode(1L);
        inboundOrderRequestDto.setBatchStock(Arrays.asList(batchRequest));

        InboundOrder ib = new InboundOrder();
        ib.setOrderDate(LocalDate.now());
        ib.setSection(sectionRepository.findById(1L).get());

        inboundOrderRepository.save(ib);

        mockMvc.perform(put("/api/v1/fresh-products/inboundorder")
                .param("orderNumber", "1")
                .content(asJsonString(inboundOrderRequestDto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }
}
