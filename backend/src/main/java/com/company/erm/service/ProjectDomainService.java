package com.company.erm.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.company.erm.exception.BusinessException;
import com.company.erm.entity.ProjectStatus;

@Service
public class ProjectDomainService {

    public void validateDates(LocalDate start, LocalDate deadline) {
        if (deadline != null && start != null && deadline.isBefore(start)) {
            throw new BusinessException("Deadline cannot be before start date");
        }
    }

    public void validateStatusTransition(ProjectStatus current, ProjectStatus next) {
        if (current == ProjectStatus.CANCELLED) {
            throw new BusinessException("Cancelled project cannot change status");
        }
    }
}