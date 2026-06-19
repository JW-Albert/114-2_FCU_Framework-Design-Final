package com.vehicle.management.infrastructure.persistence.jpa;

import com.vehicle.management.infrastructure.persistence.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface JpaVehicleRepo extends JpaRepository<VehicleEntity, UUID> {

    @Query("""
            SELECT v FROM VehicleEntity v
            WHERE v.status = 'AVAILABLE'
              AND v.id NOT IN (
                SELECT b.vehicleId FROM BorrowingRequestEntity b
                WHERE b.state IN ('PENDING','APPROVED','IN_USE')
                  AND b.periodStart < :end
                  AND b.periodEnd   > :start
              )
            """)
    List<VehicleEntity> findAvailable(@Param("start") Instant start, @Param("end") Instant end);

    List<VehicleEntity> findByDeletedAtIsNull();

    List<VehicleEntity> findByDeletedAtIsNotNull();
}
