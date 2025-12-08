package com.backend.Backend.controller;

import com.backend.Backend.model.Job;
import com.backend.Backend.repo.JobRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminCommandController {

    // key = agentId , value = HARDENING:basic | HARDENING:moderate | HARDENING:strict | ROLLBACK:default
    private final ConcurrentMap<String, String> agentCommands = new ConcurrentHashMap<>();

    @Autowired
    private JobRepo jobRepo;

    // ================================
    // ✅ HARDENING WITH LEVEL
    // ================================
    @PostMapping("/hardening/{agentId}/{level}")
    public ResponseEntity<?> applyHardening(
            @PathVariable String agentId,
            @PathVariable String level
    ) {

        // ✅ Store command + level
        agentCommands.put(agentId, "HARDENING:" + level);

        jobRepo.save(new Job(
                null,
                agentId,
                "Hardening triggered (" + level + ")",
                "pending",
                Instant.now()
        ));

        return ResponseEntity.ok(Map.of(
                "command", "HARDENING",
                "level", level,
                "agentId", agentId
        ));
    }

    // ================================
    // ✅ ROLLBACK
    // ================================
    @PostMapping("/rollback/{agentId}")
    public ResponseEntity<?> rollback(@PathVariable String agentId) {

        agentCommands.put(agentId, "ROLLBACK:default");

        jobRepo.save(new Job(
                null,
                agentId,
                "Rollback triggered",
                "pending",
                Instant.now()
        ));

        return ResponseEntity.ok(Map.of(
                "command", "ROLLBACK",
                "agentId", agentId
        ));
    }

    // ================================
    // ✅ AGENT POLLING FOR COMMAND
    // ================================
    @GetMapping("/command/{agentId}")
    public ResponseEntity<?> getCommand(@PathVariable String agentId) {

        String data = agentCommands.getOrDefault(agentId, "NONE");

        if (!data.equals("NONE")) {
            agentCommands.remove(agentId);
        }

        String[] parts = data.split(":");
        String command = parts[0];
        String level = parts.length > 1 ? parts[1] : "basic";

        return ResponseEntity.ok(Map.of(
                "command", command,
                "level", level
        ));
    }

    // ================================
    // ✅ ACK FROM AGENT
    // ================================
    @PostMapping("/{agentId}/ack")
    public ResponseEntity<?> receiveAck(
            @PathVariable String agentId,
            @RequestBody Map<String, Object> payload
    ) {

        String status  = payload.get("status").toString();
        String message = payload.get("message").toString();
        String level   = payload.getOrDefault("level", "basic").toString();

        jobRepo.save(new Job(
                null,
                agentId,
                message + " (" + level + ")",
                status,
                Instant.now()
        ));

        return ResponseEntity.ok(Map.of(
                "status", "ACK_RECEIVED",
                "agentId", agentId,
                "level", level
        ));
    }
}
