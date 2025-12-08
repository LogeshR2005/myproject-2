package com.backend.Backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
public class ScriptFile {

    @Id
    @GeneratedValue
    private Long id;

    private String name;   // win-hardening.sh
    private String os;     // windows | linux

    @Lob
    private String content;

    private Instant createdAt;


    public ScriptFile(Long id, String name, String os, String content, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.os = os;
        this.content = content;
        this.createdAt = createdAt;
    }

    public ScriptFile(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}


