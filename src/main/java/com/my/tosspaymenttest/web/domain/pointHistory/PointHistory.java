package com.my.tosspaymenttest.web.domain.pointHistory;

import com.my.tosspaymenttest.web.domain.BaseEntity;
import com.my.tosspaymenttest.web.domain.point.Point;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PointHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_id")
    private Point point;


    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PointTransactionType pointTransactionType;

    @Builder
    public PointHistory(Long id, Point point, Integer amount, PointTransactionType pointTransactionType) {
        this.id = id;
        this.point = point;
        this.amount = amount;
        this.pointTransactionType = pointTransactionType;
    }
}
