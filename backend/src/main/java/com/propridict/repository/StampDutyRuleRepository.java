package com.propridict.repository;

import com.propridict.model.StampDutyRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StampDutyRuleRepository extends JpaRepository<StampDutyRule, Long> {

    Optional<StampDutyRule> findByState(String state);

    @Query("SELECT s.state FROM StampDutyRule s ORDER BY s.state")
    List<String> findAllStates();
}
