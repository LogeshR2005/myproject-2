package com.backend.Backend.repo;


import com.backend.Backend.model.AgentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface AgentReportRepo extends JpaRepository<AgentReport, Long> {

    List<AgentReport> findByAgentId(String agentId);

    Optional<AgentReport> findTopByAgentIdOrderByCreatedAtDesc(String agentId);
}

