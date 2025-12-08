package com.backend.Backend.service;


import com.backend.Backend.model.*;
import com.backend.Backend.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashBoardService {

    @Autowired
    private  TargetRepo targetRepo;
    @Autowired
    private  JobRepo jobRepo;
    @Autowired
    private  HardeningJobRepo hardeningJobRepo;
    @Autowired
    private  AlertRepo alertRepo;

    public Map<String, Object> getDashboard() {

        Map<String, Object> response = new HashMap<>();


        List<Target> targets = targetRepo.findAll();

        long totalTargets = targets.size();

        long online = targets.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().equalsIgnoreCase("online"))
                .count();

        long offline = targets.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().equalsIgnoreCase("offline"))
                .count();


        long windowsCount = targets.stream()
                .filter(t -> t.getOs() != null &&
                        t.getOs().toLowerCase().contains("win"))
                .count();

        long linuxCount = targets.stream()
                .filter(t -> t.getOs() != null && (
                        t.getOs().toLowerCase().contains("linux") ||
                                t.getOs().toLowerCase().contains("ubuntu") ||
                                t.getOs().toLowerCase().contains("centos") ||
                                t.getOs().toLowerCase().contains("debian") ||
                                t.getOs().toLowerCase().contains("mac") ||       //  MAC = LINUX
                                t.getOs().toLowerCase().contains("darwin")       // MAC = LINUX
                ))
                .count();

        List<Job> jobs = jobRepo.findAll();

        long failedJobs = jobs.stream()
                .filter(j -> j.getStatus() != null && j.getStatus().equalsIgnoreCase("failed"))
                .count();

        List<Map<String, Object>> recentJobs = jobs.stream()
                .sorted(Comparator.comparing(Job::getTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .map(j -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", j.getId());
                    m.put("message", j.getMessage());
                    m.put("status", j.getStatus());
                    m.put("time", j.getTime() != null ? j.getTime().toString() : "NA");
                    return m;
                }).toList();


        List<HardeningJob> hardeningJobs = hardeningJobRepo.findAll();

        Map<String, Long> levelMap = hardeningJobs.stream()
                .filter(h -> h.getLevel() != null)
                .collect(Collectors.groupingBy(
                        HardeningJob::getLevel,
                        Collectors.counting()
                ));

        List<Map<String, Object>> hardeningCoverage = levelMap.entrySet()
                .stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("level", e.getKey());
                    m.put("devices", e.getValue());
                    return m;
                }).toList();


        List<Alert> alerts = alertRepo.findAll();

        List<Map<String, Object>> alertList = alerts.stream()
                .sorted(Comparator.comparing(Alert::getTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(5)
                .map(a -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", a.getId());
                    m.put("severity", a.getSeverity());
                    m.put("message", a.getMessage());
                    m.put("time", a.getTime() != null ? a.getTime().toString() : "NA");
                    return m;
                }).toList();


        Map<String, Long> osMap = targets.stream()
                .filter(t -> t.getOs() != null)
                .collect(Collectors.groupingBy(t -> {
                    String os = t.getOs().toLowerCase();
                    if (os.contains("win")) return "Windows";
                    if (os.contains("linux") || os.contains("ubuntu") || os.contains("debian")) return "Linux";
                    return "Other";
                }, Collectors.counting()));

        List<Map<String, Object>> osDistribution = osMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("value", e.getValue());
                    return m;
                }).toList();


        Map<String, Object> summary = new HashMap<>();
        summary.put("totalTargets", totalTargets);
        summary.put("online", online);
        summary.put("offline", offline);
        summary.put("failedJobs", failedJobs);
        summary.put("windowsCount", windowsCount);
        summary.put("linuxCount", linuxCount);

        response.put("summary", summary);
        response.put("recentJobs", recentJobs);
        response.put("hardeningCoverage", hardeningCoverage);
        response.put("alerts", alertList);
        response.put("osDistribution", osDistribution);

        return response;
    }
}

