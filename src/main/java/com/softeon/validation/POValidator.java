package com.softeon.validation;

import com.softeon.CancelPO;
import com.softeon.slice.SliceContext;
import com.softeon.slice.results.QueryResults;
import com.softeon.util.ErrorUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class POValidator {
    private static boolean isBlank(String value) {
        return (value == null || value.trim().isEmpty());
    }

    private static String poExistsSql() {
        return """
                SELECT COUNT(1)    FROM PO_HEADER    WHERE EXT_PO_NO = :poNumber
                """;
    }

    public static List<Map<String, String>> validate(SliceContext ctx, CancelPO.InputParams params) {
        List<Map<String, String>> errors = new ArrayList<>();
        if (isBlank(params.orgId()))
            errors.add(ErrorUtil.error("orgId is mandatory"));
        if (isBlank(params.distributionCenter()))
            errors.add(ErrorUtil.error("distributionCenter is mandatory"));
        if (isBlank(params.businessUnit()))
            errors.add(ErrorUtil.error("businessUnit is mandatory"));
        if (isBlank(params.poNumber()))
            errors.add(ErrorUtil.error("poNumber is mandatory"));
        if (!errors.isEmpty())
            return errors;
        QueryResults count = ctx.executeSqlQuery(poExistsSql(), (Record)params, new Record[0]);
        if (count == null)
            errors.add(ErrorUtil.error("Invalid PO Number"));
        return errors;
    }
}
