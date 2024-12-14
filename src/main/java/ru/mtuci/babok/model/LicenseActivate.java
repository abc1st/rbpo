package ru.mtuci.babok.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "License")
@NoArgsConstructor
public class LicenseActivate {
    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime activation_date, end_date;
    private boolean is_active;
    private String current_device;
    private Integer device_count;
    private Integer max_devices;
    private String activation_code;
    private Integer lifeTime;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "userId")
    private ApplicationUser user;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CreatorId")
    private ApplicationUser CreatorId;
}
