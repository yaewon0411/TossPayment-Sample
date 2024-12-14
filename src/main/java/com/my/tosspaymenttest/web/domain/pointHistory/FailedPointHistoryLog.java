package com.my.tosspaymenttest.web.domain.pointHistory;

import com.my.tosspaymenttest.web.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class FailedPointHistoryLog extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long pointId;

    private Integer amount;

    private String errorMessage;

    @Builder
    public FailedPointHistoryLog(Long id, Long userId, Long pointId, Integer amount, String errorMessage) {
        this.id = id;
        this.userId = userId;
        this.pointId = pointId;
        this.amount = amount;
        this.errorMessage = errorMessage;
    }
}
