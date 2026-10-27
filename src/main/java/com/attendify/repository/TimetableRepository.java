package com.attendify.repository;

import com.attendify.entity.Timetable;
import com.attendify.entity.enums.DayOfWeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findBySchoolClassId(Long classId);
    List<Timetable> findByTeacherId(Long teacherId);
    List<Timetable> findByTeacherIdAndDayOfWeek(Long teacherId, DayOfWeekEnum dayOfWeek);
    List<Timetable> findBySchoolClassIdAndDayOfWeek(Long classId, DayOfWeekEnum dayOfWeek);
}
