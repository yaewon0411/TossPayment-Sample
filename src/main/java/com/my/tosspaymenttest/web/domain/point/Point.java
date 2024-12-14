package com.my.tosspaymenttest.web.domain.point;


import com.my.tosspaymenttest.web.domain.BaseEntity;
import com.my.tosspaymenttest.web.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private Integer amount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void charge(Integer amount) throws Exception {
        this.amount += amount;
        //TODO 테스트!!!!!!!!!!!!! 제거할 것============================================
        throw new Exception("포인트 충전 중 예기치 못한 오류 발생함");
        //TODO=======================================================================
    }


    public void restore(Integer amountToAdd) {
        this.amount += amountToAdd;
    }


}
