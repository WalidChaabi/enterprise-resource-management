package com.company.erm.service;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.company.erm.dto.settings.EntitlementsResponseDTO;
import com.company.erm.entitlement.PlanCode;
import com.company.erm.entitlement.PlanEntitlement;
import com.company.erm.entity.Organization;
import com.company.erm.exception.BusinessException;
import com.company.erm.repository.UserRepository;

@Service
public class EntitlementService {

    private static final int TRIAL_SEAT_LIMIT = 5;
    private static final int STARTER_DEFAULT_SEAT_LIMIT = 25;
    private static final int FREE_SEAT_LIMIT = 3;

    private final UserRepository userRepository;

    public EntitlementService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public EntitlementsResponseDTO getEntitlements(Organization organization) {
        PlanCode plan = PlanCode.from(organization.getPlanCode());
        long seatCount = userRepository.countByOrganizationIdAndActiveTrue(organization.getId());

        EntitlementsResponseDTO dto = new EntitlementsResponseDTO();
        dto.setPlanCode(plan.name());
        dto.setPlanName(formatPlanName(plan));
        dto.setEntitlements(getEntitlementsForPlan(plan).stream().map(Enum::name).toList());
        dto.setSeatLimit(getSeatLimit(organization, plan));
        dto.setSeatCount(seatCount);
        dto.setUpgradeRequired(plan == PlanCode.TRIAL || plan == PlanCode.FREE);
        return dto;
    }

    public Set<PlanEntitlement> getEntitlementsForPlan(PlanCode plan) {
        return switch (plan) {
            case PRO -> EnumSet.of(PlanEntitlement.REPORTING, PlanEntitlement.WEBHOOKS);
            case STARTER -> EnumSet.of(PlanEntitlement.REPORTING);
            case TRIAL, FREE -> EnumSet.noneOf(PlanEntitlement.class);
        };
    }

    public boolean hasEntitlement(Organization organization, PlanEntitlement entitlement) {
        PlanCode plan = PlanCode.from(organization.getPlanCode());
        return getEntitlementsForPlan(plan).contains(entitlement);
    }

    public void requireEntitlement(Organization organization, PlanEntitlement entitlement) {
        if (!hasEntitlement(organization, entitlement)) {
            throw new BusinessException(
                    "UPGRADE_REQUIRED",
                    "This feature requires a paid plan. Upgrade in Settings → Billing.",
                    HttpStatus.FORBIDDEN);
        }
    }

    public void requireSeatAvailable(Organization organization) {
        PlanCode plan = PlanCode.from(organization.getPlanCode());
        Integer seatLimit = getSeatLimit(organization, plan);
        if (seatLimit == null) {
            return;
        }
        long seatCount = userRepository.countByOrganizationIdAndActiveTrue(organization.getId());
        if (seatCount >= seatLimit) {
            throw new BusinessException(
                    "SEAT_LIMIT_REACHED",
                    "Seat limit reached (%d). Upgrade your plan or deactivate users in Settings → Billing."
                            .formatted(seatLimit),
                    HttpStatus.FORBIDDEN);
        }
    }

    public Integer getSeatLimit(Organization organization, PlanCode plan) {
        if (organization.getSeatLimit() != null) {
            return organization.getSeatLimit();
        }
        return switch (plan) {
            case TRIAL -> TRIAL_SEAT_LIMIT;
            case STARTER -> STARTER_DEFAULT_SEAT_LIMIT;
            case PRO -> null;
            case FREE -> FREE_SEAT_LIMIT;
        };
    }

    private String formatPlanName(PlanCode plan) {
        return switch (plan) {
            case TRIAL -> "Trial";
            case STARTER -> "Starter";
            case PRO -> "Pro";
            case FREE -> "Free";
        };
    }
}
