package com.canteenbackend.helper.base.construct;

import com.canteenbackend.helper.base.response.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

public abstract class RestfullController<P, S, U> {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public abstract ResponseEntity<?> getPaginate(@ModelAttribute P p);

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public abstract ResponseEntity<?> get(@PathVariable UUID id); // if dto required check null

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // CREATED
    public abstract ResponseEntity<?> store(@Validated @RequestBody S s); // if dto required check null

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED) //ACCEPTED
    public abstract ResponseEntity<?> update(@PathVariable UUID id, @Validated @RequestBody U u); // if dto required check null

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED) //ACCEPTED
    public abstract ResponseEntity<?> destroy(@PathVariable UUID id); // if dto required check null
}
