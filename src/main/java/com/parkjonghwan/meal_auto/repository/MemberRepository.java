package com.parkjonghwan.meal_auto.repository;

import com.parkjonghwan.meal_auto.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
  Optional<Member> findByUsername(String username);
  boolean existsByUsername(String username);
}