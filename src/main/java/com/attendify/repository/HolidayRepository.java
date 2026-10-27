package com.attendify.repository;

import com.attendify.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, Long> {
    @Query("SELECT h FROM Holiday h WHERE :date BETWEEN h.startDate AND h.endDate")
    List<Holiday> findHolidaysForDate(@Param("date") LocalDate date);

    @Query("SELECT h FROM Holiday h WHERE h.startDate <= :endDate AND h.endDate >= :startDate")
    List<Holiday> findHolidaysBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
