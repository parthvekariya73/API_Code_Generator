package com.apiCodeGenerator.common.apputil.dropdown.dynamic.controller;

import com.apiCodeGenerator.common.apputil.dropdown.dynamic.dto.response.DynamicDropdownResponse;
import com.apiCodeGenerator.common.apputil.dropdown.dynamic.dto.response.PageResponse;
import com.apiCodeGenerator.common.apputil.dropdown.dynamic.service.DynamicDropdownService;
import com.apiCodeGenerator.common.apputil.response.ApiResponse;
import com.apiCodeGenerator.common.apputil.utils.commonutil.CommonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dropdown")
@Tag(name = "Dynamic Dropdown", description = "Generic dynamic dropdown APIs with multi-column support")
@Slf4j
@RequiredArgsConstructor
public class DynamicDropdownController {

    private final DynamicDropdownService dropdownService;

    private final HttpServletRequest request;

    @GetMapping("/{type}")
    @Operation(summary = "Get dropdown values with dynamic columns")
    public ResponseEntity<ApiResponse<List<DynamicDropdownResponse>>> getDropdown(
            @PathVariable("type") String type,
            @RequestParam(value="parentId", required = false) UUID parentId,
            @RequestParam(value="search", required = false) String search,
            @RequestParam(value="filters", required = false) Map<String, Object> filters

    ) {
        List<DynamicDropdownResponse> response = dropdownService.getDropdown(
                type, parentId, search, filters
        );

        return ResponseEntity.ok(
                ApiResponse.success("Data fetched successfully", response, CommonUtil.getRequestId(request))
        );
    }

    @GetMapping("/{type}/paginated")
    @Operation(summary = "Get paginated dropdown values")
    public ResponseEntity<ApiResponse<PageResponse<DynamicDropdownResponse>>> getPaginatedDropdown(
            @PathVariable String type,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Map<String, Object> filters,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        PageResponse<DynamicDropdownResponse> response = dropdownService.getPaginatedDropdown(
                type, parentId, search, filters, page, size
        );

        return ResponseEntity.ok(
                ApiResponse.success("Paginated data fetched successfully", response, CommonUtil.getRequestId(request))
        );
    }

    @GetMapping("/{type}/hierarchy")
    @Operation(summary = "Get hierarchical dropdown values")
    public ResponseEntity<ApiResponse<List<DynamicDropdownResponse>>> getHierarchicalDropdown(
            @PathVariable String type,
            @RequestParam(defaultValue = "parent_uuid") String parentColumn,
            HttpServletRequest request) {

        List<DynamicDropdownResponse> response = dropdownService.getHierarchicalDropdown(
                type, parentColumn
        );

        return ResponseEntity.ok(
                ApiResponse.success("Hierarchical data fetched successfully", response,CommonUtil.getRequestId(request))
        );
    }

    @PostMapping("/bulk")
    @Operation(summary = "Get multiple dropdowns in one request")
    public ResponseEntity<ApiResponse<Map<String, List<DynamicDropdownResponse>>>> getBulkDropdowns(
            @RequestBody List<String> types,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(required = false) String search,
            HttpServletRequest request) {

        Map<String, List<DynamicDropdownResponse>> response = dropdownService.getMultipleDropdowns(
                types, parentId, search
        );

        return ResponseEntity.ok(
                ApiResponse.success("Bulk data fetched successfully", response,CommonUtil.getRequestId(request))
        );
    }

    @GetMapping("/{type}/{uuid}")
    @Operation(summary = "Get single dropdown item by UUID")
    public ResponseEntity<ApiResponse<DynamicDropdownResponse>> getDropdownItem(
            @PathVariable String type,
            @PathVariable UUID uuid,
            HttpServletRequest request) {

        DynamicDropdownResponse response = dropdownService.getDropdownItem(type, uuid);

        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                ApiResponse.success("Item fetched successfully", response,CommonUtil.getRequestId(request))
        );
    }
}