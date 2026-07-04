package com.volunteer.registration.repository;

import com.volunteer.registration.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    List<Campaign> findByActiveTrue();

    List<Campaign> findByActiveTrueOrderByStartDateAsc();

    @Query("SELECT c FROM Campaign c WHERE c.active = true AND (c.endDate IS NULL OR c.endDate >= :currentDate)")
    List<Campaign> findActiveCampaigns(@Param("currentDate") LocalDate currentDate);

    List<Campaign> findByCampaignNameContainingIgnoreCase(String campaignName);

    Optional<Campaign> findByCampaignName(String campaignName);

    List<Campaign> findByStatus(String status);

    List<Campaign> findByType(String type);

    List<Campaign> findByParentCampaignId(Long parentCampaignId);

    @Query("SELECT DISTINCT c.type FROM Campaign c WHERE c.type IS NOT NULL")
    List<String> findAllTypes();

    @Query("SELECT DISTINCT c.status FROM Campaign c WHERE c.status IS NOT NULL")
    List<String> findAllStatuses();
}
