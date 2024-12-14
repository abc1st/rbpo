package ru.mtuci.babok.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
    private int current_device;
    private int max_devices;
    private String activation_code;
}
