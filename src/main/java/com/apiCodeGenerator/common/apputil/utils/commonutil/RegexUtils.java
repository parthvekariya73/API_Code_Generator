package com.apiCodeGenerator.common.apputil.utils.commonutil;

public class RegexUtils {
    public static final String NAME_REGEX = "^[a-zA-Z]{2,100}$";

    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    // Mobile: Supports +91, 0, or just numbers. Length 10 to 15.
    // Matches: +919876543210, 9876543210, 09876543210
    public static final String PHONE_REGEX = "^(\\+\\d{1,3})?\\d{10,15}$";

    // Medical License: Alpha-numeric, allows hyphens and slashes (e.g., "MC/GUJ/123-A")
    public static final String LICENSE_REGEX = "^[a-zA-Z0-9\\-/\\s]{5,100}$";

    // Doctor Code: Starts with DR-, followed by Year, then 3+ digits (e.g., DR-2026-001)
    public static final String DOCTOR_CODE_REGEX = "^DR-\\d{4}-\\d{3,}$";


}
