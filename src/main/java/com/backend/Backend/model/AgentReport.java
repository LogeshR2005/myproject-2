package com.backend.Backend.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agent_reports")
public class AgentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String agentId;

    private String command;

    @Column(columnDefinition = "TEXT")
    private String report;

    @Column(columnDefinition = "TEXT")
    private String log;

    private Instant createdAt;

    // =========================
    // GETTERS & SETTERS
    // =========================

    public AgentReport(){


    }

    public AgentReport(Long id, String agentId, String command, String report, String log, Instant createdAt) {
        this.id = id;
        this.agentId = agentId;
        this.command = command;
        this.report = report;
        this.log = log;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}

