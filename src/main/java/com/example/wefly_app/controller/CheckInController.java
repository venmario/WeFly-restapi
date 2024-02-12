package com.example.wefly_app.controller;

import com.example.wefly_app.request.checkin.CheckinRequestModel;
import com.example.wefly_app.service.CheckinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("v1/checkin/")
@Slf4j
public class CheckInController {
    @Autowired
    public CheckinService checkinService;


    @PostMapping
    public ResponseEntity<Map> save(@Valid @RequestBody CheckinRequestModel request) {
        return new ResponseEntity<>(checkinService.checkIn(request), HttpStatus.OK);
    }

    @GetMapping("/getBoardingPass/{eticketId}")
    public ResponseEntity<Resource> getBoardingPass(@PathVariable("eticketId") Long eticketId, HttpServletRequest request) {
        Resource resource = checkinService.getBoardingPass(eticketId);
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
