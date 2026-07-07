package com.company.erm.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.LeaveRequestStatus;
import com.company.erm.entity.LeaveType;
import com.company.erm.entity.User;
import com.company.erm.support.OrgIntegrationTestSupport;

@DisplayName("Optimistic locking integration tests")
class OptimisticLockIntegrationTest extends OrgIntegrationTestSupport {

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Test
    @DisplayName("Leave request version increments on update")
    void leaveRequest_versionIncrementsOnUpdate() {
        User employee = userRepository.findById(adminUser.getId()).orElseThrow();

        LeaveRequest created = new LeaveRequest();
        created.setEmployee(employee);
        created.setLeaveType(LeaveType.VACATION);
        created.setStartDate(LocalDate.now().plusDays(10));
        created.setEndDate(LocalDate.now().plusDays(12));
        created.setStatus(LeaveRequestStatus.PENDING);
        created.setReason("original");
        LeaveRequest persisted = leaveRequestRepository.saveAndFlush(created);

        assertThat(persisted.getVersion()).isZero();

        persisted.setReason("updated");
        LeaveRequest updated = leaveRequestRepository.saveAndFlush(persisted);

        assertThat(updated.getVersion()).isEqualTo(1L);
    }
}
