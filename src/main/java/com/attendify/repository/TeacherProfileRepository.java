package com.attendify.repository;

import com.attendify.entity.TeacherProfile;
import com.attendify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherProfileRepository extends JpaRepository<TeacherProfile, Long> {
    Optional<TeacherProfile> findByUser(User user);
    Optional<TeacherProfile> findByUserId(Long userId);
    Optional<TeacherProfile> findByEmployeeId(String employeeId);
    boolean existsByEmployeeId(String employeeId);
    List<TeacherProfile> findByIsApproved(boolean isApproved);
}
