package com.my.tosspaymenttest.web.domain.point;

import com.my.tosspaymenttest.web.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointRepository extends JpaRepository<Point, Long> {

    Optional<Point> findByUser(User user);
}
