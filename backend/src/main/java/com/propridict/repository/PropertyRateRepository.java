package com.propridict.repository;

import com.propridict.model.PropertyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyRateRepository extends JpaRepository<PropertyRate, Long> {

    @Query("SELECT DISTINCT p.city FROM PropertyRate p ORDER BY p.city")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT p.landmark FROM PropertyRate p WHERE p.city = :city ORDER BY p.landmark")
    List<String> findDistinctLandmarksByCity(String city);

    @Query("SELECT DISTINCT p.category FROM PropertyRate p WHERE p.city = :city AND p.landmark = :landmark ORDER BY p.category")
    List<String> findDistinctCategoriesByCityAndLandmark(String city, String landmark);

    Optional<PropertyRate> findByCityAndLandmarkAndCategory(String city, String landmark, String category);
}
