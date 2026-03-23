# AGENTS.md

## Project Snapshot
- This is a single-slice Java 17 Maven service that cancels purchase orders by updating `PO_HEADER`.
- Runtime surface is one external Slice component: `cancelPO` in `src/main/java/com/softeon/CancelPO.java`.
- Packaging is a standard jar (`target/POCancel-1.0-SNAPSHOT.jar`) built with Maven.

## Architecture And Data Flow
- Entry point: `CancelPO.doprocess(SliceContext ctx, InputParams params)`.
- Request contract is `CancelPO.InputParams` record: `orgId`, `distributionCenter`, `businessUnit`, `poNumber`.
- Flow is validate-first, mutate-second:
  1) `POValidator.validate(...)` returns `List<Map<String,String>>` errors.
  2) On errors, return `ActionResult.ofMap(ErrorUtil.buildErrorResponse(errors))`.
  3) Otherwise run `ctx.executeSqlUpdate(updatePOHeader(), params, new Record[0])`.
  4) Return success message when rows updated; otherwise return `PO not found` error payload.
- SQL lives inline as Java text blocks (`""" ... """`) in methods like `updatePOHeader()` and `poExistsSql()`.

## Key Files To Read First
- `pom.xml`: Java/Maven baseline and Slice dependencies (`slice-core`, `slice-api`, both `1.3.0`).
- `src/main/java/com/softeon/CancelPO.java`: component annotation, action orchestration, update SQL.
- `src/main/java/com/softeon/validation/POValidator.java`: mandatory-field validation and PO existence query.
- `src/main/java/com/softeon/util/ErrorUtil.java`: canonical error map shape used across responses.

## Developer Workflow
- Build/package: `mvn -DskipTests package` (verified in this repo).
- Tests: there are currently no files under `src/test/java`; rely on compile/package checks unless adding tests.
- Clean rebuild when changing signatures/records: `mvn clean package`.

## Project-Specific Conventions
- Keep API responses as map payloads wrapped by `ActionResult.ofMap(...)`; do not return custom DTO classes.
- Error payload convention: top-level `errors` list where each item has `title` and `detail` (see `ErrorUtil.error`).
- Validation currently returns aggregated errors instead of fail-fast; preserve this behavior unless requirements change.
- SQL parameter names must match `InputParams` record component names (for named binds like `:poNumber`).
- Component exposure is controlled via `@NamedComponent(... visibility = Visibility.EXTERNAL)`; treat annotation values as contract.

## Integration Notes
- `SliceContext` is the boundary to data access (`executeSqlQuery`, `executeSqlUpdate`); no direct JDBC usage in codebase.
- Database coupling is to `PO_HEADER` fields: `WHSE_ID`, `BLDG_ID`, `COMPANY_NO`, `EXT_PO_NO`, `PO_STATUS`, `MODIFY_TSTAMP`.
- Status transition implemented here is hard-coded to `PO_STATUS = 'PX'`.

## Change Checklist For Agents
- If you add/rename input fields, update `InputParams`, SQL named parameters, and validator checks together.
- If you change error semantics, keep `ErrorUtil.buildErrorResponse(...)` output shape backward compatible.
- For new slice actions, mirror existing pattern: `@NamedComponent` + validator + `ActionResult.ofMap` response maps.

