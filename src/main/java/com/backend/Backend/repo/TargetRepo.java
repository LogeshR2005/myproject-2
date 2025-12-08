package com.backend.Backend.repo;

import com.backend.Backend.model.Target;



import org.springframework.stereotype.Repository;


import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TargetRepo extends JpaRepository<Target, String> {

    long countByStatus(String status);
}
