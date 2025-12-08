package com.backend.Backend.controller;



import com.backend.Backend.model.AgentReport;
import com.backend.Backend.repo.AgentReportRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AgentReportController {

    @Autowired
    private AgentReportRepo reportRepo;

    // =========================================
    // SAVE REPORT + LOG (FROM AGENT)
    // =========================================
    @PostMapping("/{agentId}/report")
    public ResponseEntity<?> saveReport(
            @PathVariable String agentId,
            @RequestBody Map<String, String> payload
    ) {

        AgentReport report = new AgentReport();
        report.setAgentId(agentId);
        report.setCommand(payload.get("command"));
        report.setReport(payload.get("report"));
        report.setLog(payload.get("log"));
        report.setCreatedAt(Instant.now());

        reportRepo.save(report);

        return ResponseEntity.ok(Map.of(
                "status", "REPORT_SAVED",
                "agentId", agentId
        ));
    }

    // =========================================
    //  FETCH ALL REPORTS FOR AGENT (FRONTEND)
    // =========================================
    @GetMapping("/reports/{agentId}")
    public ResponseEntity<List<AgentReport>> getReports(
            @PathVariable String agentId
    ) {
        return ResponseEntity.ok(
                reportRepo.findByAgentId(agentId)
        );
    }

    // =========================================
    // DOWNLOAD REPORT LOG FILE
    // =========================================
    @GetMapping("/reports/download/{agentId}")
    public ResponseEntity<Resource> downloadLog(@PathVariable String agentId) throws IOException {


        AgentReport report = reportRepo
                .findTopByAgentIdOrderByCreatedAtDesc(agentId)
                .orElseThrow(() -> new RuntimeException("No report found for agent: " + agentId));

        byte[] data = report.getLog().getBytes(StandardCharsets.UTF_8);

        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + agentId + "_hardening.log\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
    }
    @GetMapping("/reports/download/{agentId}/report")
    public ResponseEntity<Resource> downloadReport(@PathVariable String agentId) throws IOException {

        AgentReport report = reportRepo
                .findTopByAgentIdOrderByCreatedAtDesc(agentId)

                .orElseThrow(() -> new RuntimeException("No report found"));

        byte[] data = report.getReport().getBytes(StandardCharsets.UTF_8);

        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + agentId + "_hardening_report.txt\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
    }


}
