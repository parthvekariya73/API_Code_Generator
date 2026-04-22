package com.apiCodeGenerator.common.apputil.dropdown.dynamic.dto.response;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true) // Add this
public class DynamicDropdownResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    private String id;

    @JsonIgnore
    private String displayName;

    @Builder.Default
    private Map<String, Object> attributes = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @JsonAnySetter
    public void setAttribute(String key, Object value) {
        if (value != null) {
            if (value instanceof UUID) {
                this.attributes.put(key, value.toString());
            } else {
                this.attributes.put(key, value);
            }
        } else {
            this.attributes.put(key, null);
        }
    }

    public void addAttribute(String key, Object value) {
        setAttribute(key, value);
    }

    public void addAttributes(Map<String, Object> attributes) {
        attributes.forEach(this::setAttribute);
    }

    @JsonIgnore
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @JsonIgnore
    public String getAttributeAsString(String key) {
        Object value = attributes.get(key);
        return value != null ? value.toString() : null;
    }

    // Factory method to create from database row
    public static DynamicDropdownResponse fromRow(Object[] row, List<String> responseKeys) {
        DynamicDropdownResponse response = new DynamicDropdownResponse();
        for (int i = 0; i < responseKeys.size() && i < row.length; i++) {
            response.setAttribute(responseKeys.get(i), row[i]);
        }
        return response;
    }

    // Factory method to create from map
    public static DynamicDropdownResponse fromMap(Map<String, Object> data) {
        DynamicDropdownResponse response = new DynamicDropdownResponse();
        response.addAttributes(data);
        return response;
    }
}
