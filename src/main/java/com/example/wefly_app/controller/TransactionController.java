package com.example.wefly_app.controller;

import com.example.wefly_app.request.transaction.TransactionSaveModel;
import com.example.wefly_app.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/v1/transaction/")
public class TransactionController {
    @Autowired
    public TransactionService transactionService;

    @PostMapping(value = {"/save", "/save/"})
    public ResponseEntity<Map> save(@Valid @RequestBody TransactionSaveModel request) {
        return new ResponseEntity<>(transactionService.save(request), HttpStatus.OK);
    }
    @DeleteMapping(value = {"/delete/{id}", "/delete/{id}/"})
    public ResponseEntity<Map> delete(@PathVariable("id") Long request) {
        return new ResponseEntity<>(transactionService.delete(request), HttpStatus.OK);
    }

    @GetMapping(value = {"/getById/{id}", "/getById/{id}/"})
    public ResponseEntity<Map> getById(@PathVariable("id") Long request) {
        return new ResponseEntity<>(transactionService.getById(request), HttpStatus.OK);
    }

    @GetMapping(value = {"/list", "/list/"})
    public ResponseEntity<Map> getAll(@RequestParam(required = true, defaultValue = "0") int page,
                                       @RequestParam(required = true, defaultValue = "10") int size,
                                       @RequestParam(required = false, defaultValue = "id") String orderBy,
                                       @RequestParam(required = false, defaultValue = "ascending") String orderType,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate,
                                       @RequestParam(required = false) String status) {
        return new ResponseEntity<>(transactionService.getAll(page, size, orderBy, orderType, startDate, endDate, status), HttpStatus.OK);
    }

}
