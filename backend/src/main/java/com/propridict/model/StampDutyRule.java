package com.propridict.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stamp_duty_rules")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StampDutyRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String state;

    @Column(name = "stamp_base", nullable = false)
    private double stampBase;

    @Column(name = "stamp_female")
    private Double stampFemale;

    @Column(name = "stamp_joint")
    private Double stampJoint;

    @Column(name = "registration_pct", nullable = false)
    private double registrationPct;
}
