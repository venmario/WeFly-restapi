package com.example.wefly_app.controller;

import com.example.wefly_app.entity.User;
import com.example.wefly_app.repository.UserRepository;
import com.example.wefly_app.request.UpdateUserModel;
import com.example.wefly_app.service.UserService;
import com.example.wefly_app.util.SimpleStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/user")
public class UserController {
    @Autowired
    public UserService userService;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public SimpleStringUtils simpleStringUtils;

//    @PreAuthorize("hasROLE('ROLE_USER')")
    @PutMapping(value = {"/update", "/update/"})
    public ResponseEntity<Map> update(@Valid @RequestBody UpdateUserModel request) {
        return new ResponseEntity<Map>(userService.update(request), HttpStatus.OK);
    }

    @DeleteMapping(value = {"/delete", "/delete/"})
    public ResponseEntity<Map> delete(@RequestBody User request) {
        return new ResponseEntity<>(userService.delete(request), HttpStatus.OK);
    }

//    @PreAuthorize("hasROLE('ROLE_USER')")
//    @GetMapping(value = {"/detail-profile", "/detail-profile/"})
//    public ResponseEntity<Map> getById(@PathVariable("id") Long id) {
//        return new ResponseEntity<>(userService.getById(id), HttpStatus.OK);
//    }

    @GetMapping("/detail-token")
    public ResponseEntity<Map> detailProfile(
            Principal principal
    ) {
        Map map = userService.getDetailProfile(principal);
        return new ResponseEntity<Map>(map, HttpStatus.OK);
    }

    @GetMapping(value = {"/test", "/test/"})
    public ResponseEntity<Map> test() {
        Map test = new HashMap<>();
        test.put("test", "test");
        System.out.println("h");
        return new ResponseEntity<Map>(test, HttpStatus.OK);
    }
}


