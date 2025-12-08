package com.backend.Backend.repo;



import com.backend.Backend.model.HardeningJob;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public interface HardeningJobRepo extends JpaRepository<HardeningJob, Long> {

    // level → count
    @Query("select h.level as level, count(h) as devices from HardeningJob h group by h.level")
    List<Object[]> getCoverageRaw();
}

