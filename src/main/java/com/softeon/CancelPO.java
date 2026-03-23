package com.softeon;

import com.softeon.slice.SliceContext;
import com.softeon.slice.component.Level;
import com.softeon.slice.component.NamedComponent;
import com.softeon.slice.component.Visibility;
import com.softeon.slice.results.ActionResult;
import com.softeon.util.ErrorUtil;
import com.softeon.validation.POValidator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelPO {
    private static final Logger log = LoggerFactory.getLogger(CancelPO.class);

    public record InputParams(String orgId, String distributionCenter, String businessUnit, String poNumber) {}

    @NamedComponent(group = "inbound", name = "cancelPO", level = Level.BASE, visibility = Visibility.EXTERNAL)
    public ActionResult doprocess(SliceContext ctx, InputParams params) {
        log.info("Inside cancelPO Slice");
        List<Map<String, String>> errors = POValidator.validate(ctx, params);
        if (!errors.isEmpty())
            return ActionResult.ofMap(ErrorUtil.buildErrorResponse(errors));
        int rowsUpdated = ctx.executeSqlUpdate(updatePOHeader(), params, new Record[0]);
        if (rowsUpdated > 0)
            return ActionResult.ofMap(
                    Map.of("message", "Po Cancelled successfully for " + params

                            .poNumber()));
        return ActionResult.ofMap(
                ErrorUtil.buildErrorResponse(
                        List.of(ErrorUtil.error("PO not found"))));
    }

    private String updatePOHeader() {
        return """ 
                UPDATE PO_HEADER   SET PO_STATUS = 'PX',       MODIFY_TSTAMP = SYSTIMESTAMP
                WHERE WHSE_ID = :orgId
                AND BLDG_ID = :distributionCenter
                AND COMPANY_NO = :businessUnit
                AND EXT_PO_NO = :poNumber
                """;
    }
}
