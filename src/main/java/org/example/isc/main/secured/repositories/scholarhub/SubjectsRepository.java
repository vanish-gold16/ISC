package org.example.isc.main.secured.repositories.scholarhub;

import org.example.isc.main.dto.scholarship.SubjectOptionDTO;
import org.example.isc.main.secured.models.scholarship.Subject;
import org.example.isc.main.secured.models.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubjectsRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByUserAndFullNameContainingIgnoreCaseOrderByFullNameAsc(User user, String fullName);
    Optional<Subject> findByUserAndFullNameIgnoreCase(User user, String fullName);

    @Query("select new org.example.isc.main.dto.scholarship.SubjectOptionDTO(s.id, s.fullName) from Subject s where s.user = :user order by s.fullName asc")
    List<SubjectOptionDTO> findByUser(@Param("user") User user);


    boolean existsByUserAndFullName(User user, String fullName);

    @Query("select new org.example.isc.main.dto.scholarship.SubjectOptionDTO(s.id, s.fullName) from Subject s where s.user = :user and lower(s.fullName) = lower(:fullName)")
    SubjectOptionDTO findOptionByUserAndFullNameIgnoreCase(@Param("user") User user, @Param("fullName") String fullName);
}
