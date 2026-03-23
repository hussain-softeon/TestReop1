package com.softeon.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorUtil {
    public static Map<String, Object> buildErrorResponse(List<Map<String, String>> errors) {
        Map<String, Object> response = new HashMap<>();
        response.put("errors", errors);
        return response;
    }

    public static Map<String, String> error(String detail) {
        Map<String, String> err = new HashMap<>();
        err.put("title", "ERROR");
        err.put("detail", detail);
        return err;
    }
}
