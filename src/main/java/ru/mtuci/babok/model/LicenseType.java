package ru.mtuci.babok.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "LicenseType")
public class LicenseType {
    @GeneratedValue
    @Id
    private Long id;

    private String name;

    @Column(length = 500)
    private String description;

    private Long default_duration;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "licenseType")
    private List<License> licenses;
}
