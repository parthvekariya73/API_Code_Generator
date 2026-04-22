package com.apiCodeGenerator.common.apputil.dropdown.dynamic.service;

import com.apiCodeGenerator.common.apputil.dropdown.dynamic.dto.response.DynamicDropdownResponse;
import com.apiCodeGenerator.common.apputil.dropdown.dynamic.dto.response.PageResponse;
import com.apiCodeGenerator.common.apputil.dropdown.dynamic.enums.DynamicDropdownType;
import com.apiCodeGenerator.common.apputil.dropdown.dynamic.repository.DynamicDropdownRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DynamicDropdownService {

    private final DynamicDropdownRepository repository;
    private final ExecutorService executor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    @Transactional(readOnly = true)
    public List<DynamicDropdownResponse> getDropdown(
            String type,
            UUID parentId,
            String search,
            Map<String, Object> filters) {

        try {
            DynamicDropdownType dropdownType = DynamicDropdownType.fromType(type);

            long startTime = System.currentTimeMillis();

            List<DynamicDropdownResponse> result = repository.getDropdown(
                    dropdownType, parentId, search, filters
            );

            long duration = System.currentTimeMillis() - startTime;
            log.debug("Fetched {} items for {} in {}ms", result.size(), type, duration);

            return result;

        } catch (IllegalArgumentException e) {
            log.error("Invalid dropdown type: {}", type, e);
            throw new IllegalArgumentException("Invalid dropdown type");
        } catch (Exception e) {
            log.error("Error fetching dropdown for type: {}", type, e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<DynamicDropdownResponse> getPaginatedDropdown(
            String type,
            UUID parentId,
            String search,
            Map<String, Object> filters,
            int page,
            int size) {

        DynamicDropdownType dropdownType = DynamicDropdownType.fromType(type);
        Pageable pageable = PageRequest.of(page, size);

        Page<DynamicDropdownResponse> pageResult = repository.getPaginatedDropdown(
                dropdownType, parentId, search, filters, pageable
        );

        return PageResponse.fromPage(pageResult);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#type + '_hierarchy'")
    public List<DynamicDropdownResponse> getHierarchicalDropdown(String type, String parentColumn) {
        DynamicDropdownType dropdownType = DynamicDropdownType.fromType(type);
        return repository.getHierarchicalDropdown(dropdownType, parentColumn);
    }

    @Transactional(readOnly = true)
    public Map<String, List<DynamicDropdownResponse>> getMultipleDropdowns(
            List<String> types,
            UUID parentId,
            String search) {

        Map<String, List<DynamicDropdownResponse>> result = new LinkedHashMap<>();

        List<CompletableFuture<AbstractMap.SimpleEntry<String, List<DynamicDropdownResponse>>>> futures = types.stream()
                .map(type -> CompletableFuture.supplyAsync(() -> {
                    List<DynamicDropdownResponse> data = getDropdown(type, parentId, search, null);
                    return new AbstractMap.SimpleEntry<>(type, data);
                }, executor))
                .toList();

        futures.forEach(future -> {
            try {
                Map.Entry<String, List<DynamicDropdownResponse>> entry = future.join();
                result.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("Error fetching dropdown in parallel", e);
            }
        });

        return result;
    }

    @Transactional(readOnly = true)
    public DynamicDropdownResponse getDropdownItem(String type, UUID itemUuid) {
        Map<String, Object> filters = new HashMap<>();
        filters.put(getUuidColumnName(type), itemUuid.toString());

        List<DynamicDropdownResponse> results = getDropdown(type, null, null, filters);
        return results.isEmpty() ? null : results.get(0);
    }

    private String getUuidColumnName(String type) {
        DynamicDropdownType dropdownType = DynamicDropdownType.fromType(type);
        return dropdownType.getColumns().get(0).getColumnName();
    }
}