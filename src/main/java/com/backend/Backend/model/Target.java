package com.backend.Backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Entity
@Table(name = "targets")
public class Target {

    @Id
    private String agentId;   // AGT-001, agent-1 etc.

    private String name;      // PC-01, Server-01 etc.

    private String os;        // Windows / Linux / Ubuntu / etc.

    private String status;    // Online / Offline


    public Target(String agentId, String name, String os, String status) {
        this.agentId = agentId;
        this.name = name;
        this.os = os;
        this.status = status;
    }

    public Target(){


    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
