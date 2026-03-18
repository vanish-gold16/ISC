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
    @Query("""
            select s from Subject s
            where s.user = :user
              and lower(coalesce(s.fullName, s.legacyName)) like lower(concat('%', :query, '%'))
            order by coalesce(s.fullName, s.legacyName) asc
            """)
    List<Subject> searchByUserAndResolvedName(
            @Param("user") User user,
            @Param("query") String query
    );

    @Query("""
            select s from Subject s
            where s.user = :user
              and lower(coalesce(s.fullName, s.legacyName)) = lower(:name)
            """)
    Optional<Subject> findByUserAndResolvedNameIgnoreCase(
            @Param("user") User user,
            @Param("name") String name
    );

    List<Subject> findByUserOrderByFullNameAsc(User user);

    boolean existsByUserAndFullName(User user, String fullName);

    SubjectOptionDTO findByUserAndId(User user, Long id);

    Subject findByUserAndFullName(User user, String fullName);
}
