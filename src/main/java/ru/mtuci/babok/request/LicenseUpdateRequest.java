package ru.mtuci.babok.request;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Data;

@Data
public class LicenseUpdateRequest {
    private String password, codeActivation, durationAdd;
}
