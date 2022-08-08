package com.mercadolibre.bootcamp.projeto_integrador.controller;

import com.mercadolibre.bootcamp.projeto_integrador.exceptions.SectionNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestsController {

    @GetMapping()
    public void testeController() throws SectionNotFoundException {
        throw new SectionNotFoundException(1L);
    }
}
