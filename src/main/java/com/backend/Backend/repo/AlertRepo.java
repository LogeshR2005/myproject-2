package com.backend.Backend.repo;

import com.backend.Backend.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepo extends JpaRepository<Alert, Long> {

    List<Alert> findTop5ByOrderByCreatedAtDesc();
}

