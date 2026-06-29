package com.company.erm.service.reporting;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.company.erm.entity.Employee;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.LeaveType;
import com.company.erm.entity.Organization;
import com.company.erm.entity.User;
import com.company.erm.repository.EmployeeRepository;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapacityDashboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @InjectMocks
    private CapacityDashboardService capacityDashboardService;

    @Test
    void getCapacityDashboard_marksEmployeeOnLeave() {
        UUID orgId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate weekStart = LocalDate.of(2026, 6, 23);

        Organization org = new Organization();
        org.setId(orgId);

        User user = new User();
        user.setId(userId);
        user.setOrganization(org);
        user.setFirstName("Alex");
        user.setLastName("Martin");
        user.setActive(true);

        Employee employee = new Employee();
        employee.setId(userId);
        employee.setOrganization(org);
        employee.setEmployeeCode("EMP-001");
        employee.setJobTitle("Developer");
        employee.setActive(true);

        LeaveRequest leave = new LeaveRequest();
        leave.setEmployee(user);
        leave.setLeaveType(LeaveType.VACATION);
        leave.setStartDate(LocalDate.of(2026, 6, 24));
        leave.setEndDate(LocalDate.of(2026, 6, 26));
        leave.setStatus(LeaveRequestStatus.APPROVED);
        leave.setTotalDays(3);

        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findByOrganizationIdAndFilters(eq(orgId), eq(null), eq(true), any(Pageable.class)))
                .thenReturn(page);
        when(employeeRepository.findById(userId)).thenReturn(java.util.Optional.of(employee));
        when(projectRepository.findByOrganizationIdAndArchivedFalse(orgId)).thenReturn(List.of());
        when(timeEntryRepository.findByOrganizationId(orgId)).thenReturn(List.of());
        when(leaveRequestRepository.findByOrganizationId(orgId)).thenReturn(List.of(leave));

        var result = capacityDashboardService.getCapacityDashboard(orgId, weekStart);

        assertThat(result.getWeekStart()).isEqualTo(LocalDate.of(2026, 6, 22));
        assertThat(result.getOnLeaveEmployees()).isEqualTo(1);
        assertThat(result.getEmployees()).hasSize(1);
        assertThat(result.getEmployees().get(0).isOnLeaveThisWeek()).isTrue();
        assertThat(result.getEmployees().get(0).getLeaveDaysThisWeek()).isEqualTo(3);
    }
}
