package com.backend.Backend.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin
public class AgentDownloadController {

    // ✅ DOWNLOAD LATEST AGENT
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadAgent() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("static/agent/agent.py");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"agent.py\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
