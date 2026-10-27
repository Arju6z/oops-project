package com.attendify.repository;

import com.attendify.entity.ParentStudentLink;
import com.attendify.entity.User;
import com.attendify.entity.enums.LinkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long> {
    List<ParentStudentLink> findByParent(User parent);
    List<ParentStudentLink> findByParentId(Long parentId);
    List<ParentStudentLink> findByParentIdAndStatus(Long parentId, LinkStatus status);
    List<ParentStudentLink> findByStudentId(Long studentId);
    List<ParentStudentLink> findByStatus(LinkStatus status);
    Optional<ParentStudentLink> findByParentIdAndStudentId(Long parentId, Long studentId);
}
