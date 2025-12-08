package com.backend.Backend.repo;



import com.backend.Backend.model.Job;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface JobRepo extends JpaRepository<Job, Long> {

    long countByStatus(String status);

    List<Job> findTop5ByOrderByTimeDesc();
}

