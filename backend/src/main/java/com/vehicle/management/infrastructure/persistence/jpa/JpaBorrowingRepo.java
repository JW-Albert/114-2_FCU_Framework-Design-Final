package com.vehicle.management.infrastructure.persistence.jpa;

import com.vehicle.management.infrastructure.persistence.entity.BorrowingRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaBorrowingRepo extends JpaRepository<BorrowingRequestEntity, UUID> {

    List<BorrowingRequestEntity> findByState(String state);

    List<BorrowingRequestEntity> findByUserId(UUID userId);

    @Query("""
            SELECT b FROM BorrowingRequestEntity b
            WHERE b.vehicleId = :vehicleId
              AND b.state IN ('PENDING','APPROVED','IN_USE')
              AND b.periodStart < :end
              AND b.periodEnd   > :start
            """)
    List<BorrowingRequestEntity> findConflicting(
            @Param("vehicleId") UUID vehicleId,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
