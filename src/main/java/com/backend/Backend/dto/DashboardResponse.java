package com.backend.Backend.dto;


import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {

    private Map<String, Object> summary;

    private List<Map<String, Object>> osDistribution;

    private List<Map<String, Object>> hardeningCoverage;

    private List<Map<String, Object>> recentJobs;

    private List<Map<String, Object>> alerts;
}
