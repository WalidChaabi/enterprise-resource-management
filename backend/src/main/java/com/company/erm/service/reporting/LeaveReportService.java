package com.company.erm.service.reporting;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.LeaveBalanceDTO;
import com.company.erm.dto.LeaveReportDTO;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.User;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class LeaveReportService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public LeaveReportService(
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    public List<LeaveBalanceDTO> getLeaveBalance(UUID organizationId) {
        Pageable pageable = PageRequest.of(0, 10000);
        List<User> employees = userRepository.findByOrganizationIdAndFilters(
                organizationId, null, true, pageable).getContent();

        return employees.stream()
                .map(employee -> {
                    List<LeaveRequest> requests = leaveRequestRepository.findByEmployeeId(employee.getId());

                    int totalDaysUsed = requests.stream()
                            .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED)
                            .mapToInt(LeaveRequest::getTotalDays)
                            .sum();

                    int totalDaysPending = requests.stream()
                            .filter(lr -> lr.getStatus() == LeaveRequestStatus.PENDING)
                            .mapToInt(LeaveRequest::getTotalDays)
                            .sum();

                    int totalDaysApproved = requests.stream()
                            .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED)
                            .mapToInt(LeaveRequest::getTotalDays)
                            .sum();

                    int totalDaysThisYear = requests.stream()
                            .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED
                                    && lr.getStartDate().getYear() == LocalDate.now().getYear())
                            .mapToInt(LeaveRequest::getTotalDays)
                            .sum();

                    return new LeaveBalanceDTO(
                            employee.getId(),
                            employee.getFirstName() + " " + employee.getLastName(),
                            employee.getEmail(),
                            totalDaysUsed,
                            totalDaysPending,
                            totalDaysApproved,
                            totalDaysThisYear
                    );
                })
                .collect(Collectors.toList());
    }

    public List<LeaveReportDTO> getUpcomingLeaveRequests(UUID organizationId) {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysFromNow = today.plusDays(30);

        List<LeaveRequest> requests = leaveRequestRepository.findByOrganizationId(organizationId);

        return requests.stream()
                .filter(lr -> lr.getStatus() == LeaveRequestStatus.APPROVED
                        && !lr.getStartDate().isBefore(today)
                        && !lr.getStartDate().isAfter(thirtyDaysFromNow))
                .map(lr -> new LeaveReportDTO(
                        lr.getEmployee().getId(),
                        lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName(),
                        lr.getEmployee().getEmail(),
                        lr.getId(),
                        lr.getLeaveType(),
                        lr.getStartDate(),
                        lr.getEndDate(),
                        lr.getTotalDays(),
                        lr.getStatus()
                ))
                .collect(Collectors.toList());
    }
}
