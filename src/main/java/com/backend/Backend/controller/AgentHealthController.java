package com.backend.Backend.controller;

import com.backend.Backend.model.AgentHealthPayload;
import com.backend.Backend.model.Job;
import com.backend.Backend.model.Target;
import com.backend.Backend.repo.JobRepo;
import com.backend.Backend.repo.TargetRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;



import java.io.IOException;


import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin
public class AgentHealthController {

    private final ConcurrentMap<String, AgentHealthPayload> lastHealth = new ConcurrentHashMap<>();

    @Autowired
    private TargetRepo targetRepo;

    // =========================================
    // HEARTBEAT
    // =========================================
    @PostMapping("/{agentId}/health")
    public ResponseEntity<?> updateHealth(
            @PathVariable String agentId,
            @RequestBody AgentHealthPayload payload
    ) {

        payload.setAgentId(agentId);
        payload.setTimestamp(Instant.now());
        lastHealth.put(agentId, payload);

        targetRepo.save(new Target(
                agentId,
                payload.getHostname(),
                payload.getOs(),
                "Online"
        ));

        return ResponseEntity.ok(Map.of(
                "status", "HEARTBEAT_RECEIVED",
                "agentId", agentId
        ));
    }

    // =========================================
    // LIST TARGETS (UI)
    // =========================================
    @GetMapping("/targets")
    public ResponseEntity<?> getTargetsForUI() {

        Instant now = Instant.now();

        return ResponseEntity.ok(
                lastHealth.values().stream().map(p -> {

                    long seconds =
                            Duration.between(p.getTimestamp(), now).getSeconds();

                    boolean alive = seconds <= 10;

                    String lastSeen =
                            seconds < 60 ? seconds + " sec ago" :
                                    seconds < 3600 ? (seconds / 60) + " min ago" :
                                            seconds < 86400 ? (seconds / 3600) + " hr ago"
                                                    : (seconds / 86400) + " day ago";

                    return Map.of(
                            "id", p.getAgentId(),
                            "deviceName", p.getHostname(),
                            "agentId", p.getAgentId(),
                            "os", p.getOs(),
                            "lastSeen", lastSeen,
                            "status", alive ? "online" : "offline",
                            "level", "Easy"
                    );
                }).toList()
        );
    }

    // =========================================
    // ✅✅ FINAL DOWNLOAD API (BASIC / MODERATE / STRICT + ROLLBACK)
    // =========================================
    @GetMapping("/{agentId}/download/{os}/{type}/{level}")
    public ResponseEntity<Resource> downloadScript(
            @PathVariable String agentId,
            @PathVariable String os,
            @PathVariable String type,    // HARDENING / ROLLBACK
            @PathVariable String level    // basic / moderate / strict
    ) throws IOException {

        String file;

        // ✅ WINDOWS FILE SELECTION
        if (os.equalsIgnoreCase("windows")) {

            if (type.equalsIgnoreCase("ROLLBACK")) {
                file = "rollback.ps1";
            } else if (type.equalsIgnoreCase("HARDENING")) {

                file = switch (level.toLowerCase()) {
                    case "basic" -> "basic.ps1";
                    case "moderate" -> "moderate.ps1";
                    case "strict" -> "strict.ps1";
                    default -> throw new RuntimeException("Invalid hardening level: " + level);
                };

            } else {
                throw new RuntimeException("Invalid type: " + type);
            }

        }
        // ✅ LINUX FILE SELECTION
        else {

            if (type.equalsIgnoreCase("ROLLBACK")) {
                file = "rollback.sh";
            } else if (type.equalsIgnoreCase("HARDENING")) {

                file = switch (level.toLowerCase()) {
                    case "basic" -> "basic.sh";
                    case "moderate" -> "moderate.sh";
                    case "strict" -> "strict.sh";
                    default -> throw new RuntimeException("Invalid hardening level: " + level);
                };

            } else {
                throw new RuntimeException("Invalid type: " + type);
            }
        }

        // ✅ FINAL RESOURCE LOAD
        ClassPathResource resource =
                new ClassPathResource("static/scripts/" + os.toLowerCase() + "/" + file);

        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
