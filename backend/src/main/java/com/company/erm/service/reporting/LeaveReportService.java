package com.company.erm.service.reporting;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.LeaveBalanceDTO;
import com.company.erm.dto.LeaveReportDTO;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.reporting.LeaveBalanceAggregate;
import com.company.erm.security.OrganizationSecurityService;

@Service
@Transactional(readOnly = true)
public class LeaveReportService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final OrganizationSecurityService organizationSecurity;

    public LeaveReportService(
            LeaveRequestRepository leaveRequestRepository,
            OrganizationSecurityService organizationSecurity) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.organizationSecurity = organizationSecurity;
    }

    public List<LeaveBalanceDTO> getLeaveBalance(UUID organizationId) {
        organizationSecurity.requireOrgMembership(organizationId);
        LocalDate now = LocalDate.now();
        LocalDate yearStart = LocalDate.of(now.getYear(), 1, 1);
        LocalDate yearEnd = LocalDate.of(now.getYear(), 12, 31);

        return leaveRequestRepository.aggregateLeaveBalanceByOrganization(organizationId, yearStart, yearEnd)
                .stream()
                .map(this::toLeaveBalanceDto)
                .collect(Collectors.toList());
    }

    public List<LeaveReportDTO> getUpcomingLeaveRequests(UUID organizationId) {
        organizationSecurity.requireOrgMembership(organizationId);
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);

        return leaveRequestRepository.findUpcomingApprovedByOrganization(organizationId, today, thirtyDaysFromNow)
                .stream()
                .map(this::toLeaveReportDto)
                .collect(Collectors.toList());
    }

    private LeaveBalanceDTO toLeaveBalanceDto(LeaveBalanceAggregate row) {
        return new LeaveBalanceDTO(
                row.getEmployeeId(),
                row.getEmployeeName(),
                row.getEmail(),
                (int) row.getTotalDaysUsed(),
                (int) row.getTotalDaysPending(),
                (int) row.getTotalDaysApproved(),
                (int) row.getTotalDaysThisYear());
    }

    private LeaveReportDTO toLeaveReportDto(LeaveRequest lr) {
        return new LeaveReportDTO(
                lr.getEmployee().getId(),
                lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName(),
                lr.getEmployee().getEmail(),
                lr.getId(),
                lr.getLeaveType(),
                lr.getStartDate(),
                lr.getEndDate(),
                lr.getTotalDays(),
                lr.getStatus());
    }
}
