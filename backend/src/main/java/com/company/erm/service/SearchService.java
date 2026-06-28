package com.company.erm.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.erm.dto.SearchEntityType;
import com.company.erm.dto.SearchItemDTO;
import com.company.erm.dto.SearchRequestDTO;
import com.company.erm.dto.SearchResultDTO;
import com.company.erm.entity.Department;
import com.company.erm.entity.LeaveRequest;
import com.company.erm.entity.Project;
import com.company.erm.entity.Task;
import com.company.erm.entity.TimeEntry;
import com.company.erm.entity.User;
import com.company.erm.repository.DepartmentRepository;
import com.company.erm.repository.LeaveRequestRepository;
import com.company.erm.repository.ProjectRepository;
import com.company.erm.repository.TaskRepository;
import com.company.erm.repository.TimeEntryRepository;
import com.company.erm.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class SearchService {

    private static final int MAX_FETCH_PER_TYPE = 200;

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final DepartmentRepository departmentRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;

    public SearchService(
            ProjectRepository projectRepository,
            TaskRepository taskRepository,
            DepartmentRepository departmentRepository,
            TimeEntryRepository timeEntryRepository,
            LeaveRequestRepository leaveRequestRepository,
            UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.departmentRepository = departmentRepository;
        this.timeEntryRepository = timeEntryRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }

    public SearchResultDTO search(UUID organizationId, SearchRequestDTO request, String currentUserEmail) {
        String query = request.getQuery() != null ? request.getQuery().trim() : "";

        if (query.isEmpty()) {
            return createEmptyResult(request);
        }

        List<SearchEntityType> entityTypes = request.getEntityTypes();
        if (entityTypes == null || entityTypes.isEmpty()) {
            entityTypes = List.of(
                SearchEntityType.PROJECT,
                SearchEntityType.EMPLOYEE,
                SearchEntityType.TASK,
                SearchEntityType.DEPARTMENT,
                SearchEntityType.TIME_ENTRY,
                SearchEntityType.LEAVE_REQUEST
            );
        }

        int fetchLimit = Math.min(
                MAX_FETCH_PER_TYPE,
                Math.max(request.getSize() * (request.getPage() + 1), request.getSize()));

        Map<SearchEntityType, SearchResultDTO.EntitySearchResults> resultsByType = new HashMap<>();
        List<SearchItemDTO> allItems = new ArrayList<>();
        long totalResults = 0;

        for (SearchEntityType entityType : entityTypes) {
            TypeSearchResult typeResult = searchEntityType(organizationId, entityType, query, fetchLimit);
            totalResults += typeResult.totalCount();
            if (!typeResult.items().isEmpty()) {
                resultsByType.put(entityType, new SearchResultDTO.EntitySearchResults(
                    entityType,
                    typeResult.totalCount(),
                    typeResult.items()
                ));
                allItems.addAll(typeResult.items());
            }
        }

        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        allItems = sortItems(allItems, sort, query);

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        List<SearchItemDTO> paginatedItems = paginateItems(allItems, pageable);

        int totalPages = totalResults == 0
                ? 0
                : (int) Math.ceil((double) totalResults / request.getSize());

        Map<SearchEntityType, SearchResultDTO.EntitySearchResults> paginatedResultsByType = new HashMap<>();
        for (Map.Entry<SearchEntityType, SearchResultDTO.EntitySearchResults> entry : resultsByType.entrySet()) {
            SearchEntityType type = entry.getKey();
            long count = entry.getValue().getCount();
            List<SearchItemDTO> typeItems = paginatedItems.stream()
                .filter(item -> item.getEntityType() == type)
                .collect(Collectors.toList());
            paginatedResultsByType.put(type, new SearchResultDTO.EntitySearchResults(type, count, typeItems));
        }

        return new SearchResultDTO(
            query,
            totalResults,
            request.getPage(),
            request.getSize(),
            totalPages,
            paginatedResultsByType
        );
    }

    private record TypeSearchResult(List<SearchItemDTO> items, long totalCount) {}

    private TypeSearchResult searchEntityType(
            UUID organizationId,
            SearchEntityType entityType,
            String query,
            int fetchLimit) {

        Pageable pageable = PageRequest.of(0, fetchLimit);
        return switch (entityType) {
            case PROJECT -> searchProjects(organizationId, query, pageable);
            case EMPLOYEE -> searchEmployees(organizationId, query, pageable);
            case TASK -> searchTasks(organizationId, query, pageable);
            case DEPARTMENT -> searchDepartments(organizationId, query, pageable);
            case TIME_ENTRY -> searchTimeEntries(organizationId, query, pageable);
            case LEAVE_REQUEST -> searchLeaveRequests(organizationId, query, pageable);
        };
    }

    private TypeSearchResult searchProjects(UUID organizationId, String query, Pageable pageable) {
        Page<Project> page = projectRepository.searchByOrganizationIdAndQuery(organizationId, query, pageable);
        long total = projectRepository.countSearchByOrganizationIdAndQuery(organizationId, query);
        List<SearchItemDTO> items = page.getContent().stream()
            .map(p -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("status", p.getStatus() != null ? p.getStatus().toString() : null);
                metadata.put("manager", p.getManager() != null
                    ? p.getManager().getFirstName() + " " + p.getManager().getLastName()
                    : null);
                metadata.put("membersCount", p.getMembers() != null ? p.getMembers().size() : 0);

                String highlight = highlightMatch(
                        p.getName() + " " + (p.getDescription() != null ? p.getDescription() : ""), query);

                return new SearchItemDTO(
                    SearchEntityType.PROJECT,
                    p.getId(),
                    p.getName(),
                    p.getDescription(),
                    highlight,
                    p.getStartDate(),
                    p.getCreatedAt(),
                    metadata
                );
            })
            .collect(Collectors.toList());
        return new TypeSearchResult(items, total);
    }

    private TypeSearchResult searchEmployees(UUID organizationId, String query, Pageable pageable) {
        Page<User> page = userRepository.searchActiveByOrganizationIdAndQuery(organizationId, query, pageable);
        long total = userRepository.countSearchActiveByOrganizationIdAndQuery(organizationId, query);
        List<SearchItemDTO> items = page.getContent().stream()
            .map(u -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("email", u.getEmail());
                metadata.put("department", u.getDepartment() != null ? u.getDepartment().getName() : null);
                metadata.put("active", u.isActive());

                String highlight = highlightMatch(
                    u.getFirstName() + " " + u.getLastName() + " " + u.getEmail(),
                    query
                );

                return new SearchItemDTO(
                    SearchEntityType.EMPLOYEE,
                    u.getId(),
                    u.getFirstName() + " " + u.getLastName(),
                    u.getEmail(),
                    highlight,
                    null,
                    u.getCreatedAt(),
                    metadata
                );
            })
            .collect(Collectors.toList());
        return new TypeSearchResult(items, total);
    }

    private TypeSearchResult searchTasks(UUID organizationId, String query, Pageable pageable) {
        Page<Task> page = taskRepository.searchByOrganizationIdAndQuery(organizationId, query, pageable);
        long total = taskRepository.countSearchByOrganizationIdAndQuery(organizationId, query);
        List<SearchItemDTO> items = page.getContent().stream()
            .map(t -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("status", t.getStatus() != null ? t.getStatus().toString() : null);
                metadata.put("priority", t.getPriority() != null ? t.getPriority().toString() : null);
                metadata.put("project", t.getProject() != null ? t.getProject().getName() : null);
                metadata.put("assignedTo", t.getAssignedTo() != null
                    ? t.getAssignedTo().getFirstName() + " " + t.getAssignedTo().getLastName()
                    : null);

                String highlight = highlightMatch(
                        t.getTitle() + " " + (t.getDescription() != null ? t.getDescription() : ""), query);

                return new SearchItemDTO(
                    SearchEntityType.TASK,
                    t.getId(),
                    t.getTitle(),
                    t.getDescription(),
                    highlight,
                    t.getDueDate(),
                    t.getCreatedAt(),
                    metadata
                );
            })
            .collect(Collectors.toList());
        return new TypeSearchResult(items, total);
    }

    private TypeSearchResult searchDepartments(UUID organizationId, String query, Pageable pageable) {
        Page<Department> page = departmentRepository.searchByOrganizationIdAndQuery(organizationId, query, pageable);
        long total = departmentRepository.countSearchByOrganizationIdAndQuery(organizationId, query);
        List<SearchItemDTO> items = page.getContent().stream()
            .map(d -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("active", d.isActive());
                metadata.put("parent", d.getParent() != null ? d.getParent().getName() : null);
                metadata.put("code", d.getCode());

                String highlight = highlightMatch(d.getName(), query);

                return new SearchItemDTO(
                    SearchEntityType.DEPARTMENT,
                    d.getId(),
                    d.getName(),
                    d.getCode() != null ? d.getCode() : "",
                    highlight,
                    null,
                    d.getCreatedAt(),
                    metadata
                );
            })
            .collect(Collectors.toList());
        return new TypeSearchResult(items, total);
    }

    private TypeSearchResult searchTimeEntries(UUID organizationId, String query, Pageable pageable) {
        Page<TimeEntry> page = timeEntryRepository.searchByOrganizationIdAndQuery(organizationId, query, pageable);
        long total = timeEntryRepository.countSearchByOrganizationIdAndQuery(organizationId, query);
        List<SearchItemDTO> items = page.getContent().stream()
            .map(te -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("status", te.getStatus() != null ? te.getStatus().toString() : null);
                metadata.put("durationHours", te.getDurationHours());
                metadata.put("project", te.getProject() != null ? te.getProject().getName() : null);
                metadata.put("task", te.getTask() != null ? te.getTask().getTitle() : null);
                metadata.put("employee", te.getEmployee() != null
                    ? te.getEmployee().getFirstName() + " " + te.getEmployee().getLastName()
                    : null);

                String highlight = highlightMatch(
                    (te.getDescription() != null ? te.getDescription() : "") + " " +
                    (te.getProject() != null ? te.getProject().getName() : "") + " " +
                    (te.getTask() != null ? te.getTask().getTitle() : ""),
                    query
                );

                return new SearchItemDTO(
                    SearchEntityType.TIME_ENTRY,
                    te.getId(),
                    te.getDescription() != null ? te.getDescription() : "Time Entry",
                    "Project: " + (te.getProject() != null ? te.getProject().getName() : "N/A"),
                    highlight,
                    te.getEntryDate(),
                    te.getCreatedAt(),
                    metadata
                );
            })
            .collect(Collectors.toList());
        return new TypeSearchResult(items, total);
    }

    private TypeSearchResult searchLeaveRequests(UUID organizationId, String query, Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.searchByOrganizationIdAndQuery(organizationId, query, pageable);
        long total = leaveRequestRepository.countSearchByOrganizationIdAndQuery(organizationId, query);
        List<SearchItemDTO> items = page.getContent().stream()
            .map(lr -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("status", lr.getStatus() != null ? lr.getStatus().toString() : null);
                metadata.put("leaveType", lr.getLeaveType() != null ? lr.getLeaveType().toString() : null);
                metadata.put("totalDays", lr.getTotalDays());
                metadata.put("employee", lr.getEmployee() != null
                    ? lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName()
                    : null);

                String highlight = highlightMatch(
                    (lr.getReason() != null ? lr.getReason() : "") + " " +
                    (lr.getEmployee() != null ? lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName() : ""),
                    query
                );

                return new SearchItemDTO(
                    SearchEntityType.LEAVE_REQUEST,
                    lr.getId(),
                    "Leave Request - " + (lr.getEmployee() != null
                            ? lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName()
                            : "Unknown"),
                    lr.getReason(),
                    highlight,
                    lr.getStartDate(),
                    lr.getCreatedAt(),
                    metadata
                );
            })
            .collect(Collectors.toList());
        return new TypeSearchResult(items, total);
    }

    private String highlightMatch(String text, String query) {
        if (text == null || query == null || query.isEmpty()) {
            return text != null ? text : "";
        }
        return text.replaceAll("(?i)" + java.util.regex.Pattern.quote(query), "**" + query + "**");
    }

    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        if ("date".equalsIgnoreCase(sortBy)) {
            return Sort.by(direction, "date");
        } else if ("name".equalsIgnoreCase(sortBy) || "title".equalsIgnoreCase(sortBy)) {
            return Sort.by(direction, "title");
        }
        return Sort.unsorted();
    }

    private List<SearchItemDTO> sortItems(List<SearchItemDTO> items, Sort sort, String query) {
        if (sort.isUnsorted()) {
            return items.stream()
                .sorted((a, b) -> {
                    boolean aInTitle = a.getTitle() != null && a.getTitle().toLowerCase().contains(query.toLowerCase());
                    boolean bInTitle = b.getTitle() != null && b.getTitle().toLowerCase().contains(query.toLowerCase());
                    if (aInTitle && !bInTitle) return -1;
                    if (!aInTitle && bInTitle) return 1;
                    return 0;
                })
                .collect(Collectors.toList());
        }

        Sort.Order dateOrder = sort.getOrderFor("date");
        Sort.Order nameOrder = sort.getOrderFor("title");
        boolean ascending = (dateOrder != null && dateOrder.getDirection() == Sort.Direction.ASC)
                           || (nameOrder != null && nameOrder.getDirection() == Sort.Direction.ASC);

        return items.stream()
            .sorted((a, b) -> {
                if (dateOrder != null) {
                    LocalDate dateA = a.getDate();
                    LocalDate dateB = b.getDate();
                    if (dateA == null && dateB == null) return 0;
                    if (dateA == null) return 1;
                    if (dateB == null) return -1;
                    int comparison = dateA.compareTo(dateB);
                    return ascending ? comparison : -comparison;
                } else {
                    String titleA = a.getTitle() != null ? a.getTitle() : "";
                    String titleB = b.getTitle() != null ? b.getTitle() : "";
                    int comparison = titleA.compareToIgnoreCase(titleB);
                    return ascending ? comparison : -comparison;
                }
            })
            .collect(Collectors.toList());
    }

    private List<SearchItemDTO> paginateItems(List<SearchItemDTO> items, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), items.size());
        if (start > items.size()) {
            return new ArrayList<>();
        }
        return items.subList(start, end);
    }

    private SearchResultDTO createEmptyResult(SearchRequestDTO request) {
        return new SearchResultDTO(
            request.getQuery() != null ? request.getQuery() : "",
            0,
            request.getPage(),
            request.getSize(),
            0,
            new HashMap<>()
        );
    }
}
