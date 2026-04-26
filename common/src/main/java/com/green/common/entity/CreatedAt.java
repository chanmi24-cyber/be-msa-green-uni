package com.green.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass //Entity 부모역할
@EntityListeners(AuditingEntityListener.class) //MySQL로 치면 CurrentTimestamp 역할
public class CreatedAt {
    @CreatedDate //insert시 현재일시값이 삽입된다.
    @Column(nullable = false) //컬럼의 속성값을 줄 때 사용. 지금은 NOT NULL 속성을 추가함.
    //타입, 이름으로 컬럼이 된다. LocalDateTime > DATETIME, createdAt > created_at
    private LocalDateTime createdAt;
}