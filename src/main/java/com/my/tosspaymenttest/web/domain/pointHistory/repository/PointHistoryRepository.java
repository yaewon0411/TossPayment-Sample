package com.my.tosspaymenttest.web.domain.pointHistory.repository;

import com.my.tosspaymenttest.web.domain.pointHistory.PointHistory;
import com.my.tosspaymenttest.web.domain.pointHistory.PointTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

}
