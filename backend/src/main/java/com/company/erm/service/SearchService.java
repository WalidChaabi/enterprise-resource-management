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

    /**
     * Global search across all entity types.
     */
    public SearchResultDTO search(UUID organizationId, SearchRequestDTO request, String currentUserEmail) {
        String query = request.getQuery() != null ? request.getQuery().toLowerCase().trim() : "";
        
        if (query.isEmpty()) {
            return createEmptyResult(request);
        }

        List<SearchEntityType> entityTypes = request.getEntityTypes();
        if (entityTypes == null || entityTypes.isEmpty()) {
            // Search all entity types
            entityTypes = List.of(
                SearchEntityType.PROJECT,
                SearchEntityType.EMPLOYEE,
                SearchEntityType.TASK,
                SearchEntityType.DEPARTMENT,
                SearchEntityType.TIME_ENTRY,
                SearchEntityType.LEAVE_REQUEST
            );
        }

        Map<SearchEntityType, SearchResultDTO.EntitySearchResults> resultsByType = new HashMap<>();
        List<SearchItemDTO> allItems = new ArrayList<>();

        // Search each entity type
        for (SearchEntityType entityType : entityTypes) {
            List<SearchItemDTO> items = searchEntityType(organizationId, entityType, query, currentUserEmail);
            if (!items.isEmpty()) {
                resultsByType.put(entityType, new SearchResultDTO.EntitySearchResults(
                    entityType,
                    items.size(),
                    items
                ));
                allItems.addAll(items);
            }
        }

        // Sort all items by relevance/date
        Sort sort = createSort(request.getSortBy(), request.getSortDirection());
        allItems = sortItems(allItems, sort, query);

        // Paginate
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        List<SearchItemDTO> paginatedItems = paginateItems(allItems, pageable);

        // Calculate totals
        long totalResults = allItems.size();
        int totalPages = (int) Math.ceil((double) totalResults / request.getSize());

        // Update results with paginated items
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

    private List<SearchItemDTO> searchEntityType(
            UUID organizationId,
            SearchEntityType entityType,
            String query,
            String currentUserEmail) {
        
        return switch (entityType) {
            case PROJECT -> searchProjects(organizationId, query);
            case EMPLOYEE -> searchEmployees(organizationId, query);
            case TASK -> searchTasks(organizationId, query);
            case DEPARTMENT -> searchDepartments(organizationId, query);
            case TIME_ENTRY -> searchTimeEntries(organizationId, query, currentUserEmail);
            case LEAVE_REQUEST -> searchLeaveRequests(organizationId, query, currentUserEmail);
        };
    }

    private List<SearchItemDTO> searchProjects(UUID organizationId, String query) {
        List<Project> projects = projectRepository.findByOrganizationIdAndArchivedFalse(organizationId);
        
        return projects.stream()
            .filter(p -> matches(p.getName(), query) || matches(p.getDescription(), query))
            .map(p -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("status", p.getStatus() != null ? p.getStatus().toString() : null);
                metadata.put("manager", p.getManager() != null 
                    ? p.getManager().getFirstName() + " " + p.getManager().getLastName() 
                    : null);
                metadata.put("membersCount", p.getMembers() != null ? p.getMembers().size() : 0);
                
                String highlight = highlightMatch(p.getName() + " " + (p.getDescription() != null ? p.getDescription() : ""), query);
                
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
    }

    private List<SearchItemDTO> searchEmployees(UUID organizationId, String query) {
        Pageable pageable = PageRequest.of(0, 10000); // Get all employees
        Page<User> users = userRepository.findByOrganizationIdAndFilters(organizationId, null, true, pageable);
        
        return users.getContent().stream()
            .filter(u -> matches(u.getFirstName(), query) 
                || matches(u.getLastName(), query)
                || matches(u.getEmail(), query))
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
    }

    private List<SearchItemDTO> searchTasks(UUID organizationId, String query) {
        List<Task> tasks = taskRepository.findByOrganizationId(organizationId);
        
        return tasks.stream()
            .filter(t -> matches(t.getTitle(), query) || matches(t.getDescription(), query))
            .map(t -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("status", t.getStatus() != null ? t.getStatus().toString() : null);
                metadata.put("priority", t.getPriority() != null ? t.getPriority().toString() : null);
                metadata.put("project", t.getProject() != null ? t.getProject().getName() : null);
                metadata.put("assignedTo", t.getAssignedTo() != null 
                    ? t.getAssignedTo().getFirstName() + " " + t.getAssignedTo().getLastName() 
                    : null);
                
                String highlight = highlightMatch(t.getTitle() + " " + (t.getDescription() != null ? t.getDescription() : ""), query);
                
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
    }

    private List<SearchItemDTO> searchDepartments(UUID organizationId, String query) {
        List<Department> departments = departmentRepository.findByOrganizationId(organizationId);
        
        return departments.stream()
            .filter(d -> matches(d.getName(), query))
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
    }

    private List<SearchItemDTO> searchTimeEntries(UUID organizationId, String query, String currentUserEmail) {
        List<TimeEntry> entries = timeEntryRepository.findByOrganizationId(organizationId);
        
        return entries.stream()
            .filter(te -> matches(te.getDescription(), query)
                || (te.getProject() != null && matches(te.getProject().getName(), query))
                || (te.getTask() != null && matches(te.getTask().getTitle(), query)))
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
    }

    private List<SearchItemDTO> searchLeaveRequests(UUID organizationId, String query, String currentUserEmail) {
        List<LeaveRequest> requests = leaveRequestRepository.findByOrganizationId(organizationId);
        
        return requests.stream()
            .filter(lr -> matches(lr.getReason(), query)
                || (lr.getEmployee() != null && matches(lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName(), query)))
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
                    "Leave Request - " + (lr.getEmployee() != null ? lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName() : "Unknown"),
                    lr.getReason(),
                    highlight,
                    lr.getStartDate(),
                    lr.getCreatedAt(),
                    metadata
                );
            })
            .collect(Collectors.toList());
    }

    private boolean matches(String text, String query) {
        if (text == null || query == null || query.isEmpty()) {
            return false;
        }
        return text.toLowerCase().contains(query.toLowerCase());
    }

    private String highlightMatch(String text, String query) {
        if (text == null || query == null || query.isEmpty()) {
            return text != null ? text : "";
        }
        // Simple highlighting - in production, use a proper highlighting library
        return text.replaceAll("(?i)" + query, "**" + query + "**");
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
        // Default: relevance (no specific sort, keep original order)
        return Sort.unsorted();
    }

    private List<SearchItemDTO> sortItems(List<SearchItemDTO> items, Sort sort, String query) {
        if (sort.isUnsorted()) {
            // Sort by relevance (items with query in title first)
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
        
        // Sort by date or name
        Sort.Order dateOrder = sort.getOrderFor("date");
        Sort.Order nameOrder = sort.getOrderFor("title");
        boolean ascending = (dateOrder != null && dateOrder.getDirection() == Sort.Direction.ASC) || 
                           (nameOrder != null && nameOrder.getDirection() == Sort.Direction.ASC);
        
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
