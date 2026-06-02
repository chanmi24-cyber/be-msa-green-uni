package com.green.member.application.admin;

import com.green.common.auth.MemberContext;
import com.green.member.application.admin.model.AdminCreateReq;
import com.green.member.application.admin.model.FailRowRes;
import com.green.member.application.member.MemberBatchService;
import com.green.common.enumcode.EnumAdminStatus;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminBatchService extends MemberBatchService<AdminCreateReq> {
    private final AdminService adminService;

    private static final String[] HEADERS = {
        "이메일*", "이름*", "생년월일*(YYYY-MM-DD)", "전화번호*", "비상연락처",
        "우편번호", "주소", "상세주소", "입사일*(YYYY-MM-DD)"
    };

    static final String SAMPLE_EMAIL = "__admin__@example.com";

    private static final String[] SAMPLE_DATA = {
        SAMPLE_EMAIL, "홍길동", "2000-01-15", "01012345678", "01087654321",
        "06234", "서울특별시 강남구 테헤란로 123", "101호", "2024-03-01"
    };

    private static final int[] COL_WIDTHS = {
        6000, 3000, 6000, 4500, 4500,
        3000, 9000, 5000, 6000
    };

    @Override protected String getSheetName()    { return "관리자 일괄 등록"; }
    @Override protected String getSampleEmail()  { return SAMPLE_EMAIL; }
    @Override protected String[] getHeaders()    { return HEADERS; }
    @Override protected String[] getSampleData() { return SAMPLE_DATA; }
    @Override protected int[] getColWidths()     { return COL_WIDTHS; }

    @Override
    protected void addDropdowns(Sheet sheet, XSSFWorkbook workbook, DataValidationHelper dvHelper) {// 관리자는 드롭다운 X
    }

    @Override
    protected void populateRows(Sheet sheet, List<AdminCreateReq> validRows, List<FailRowRes> failList) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (shouldSkipRow(row)) continue;
            try {
                validRows.add(parseRow(row));
            } catch (Exception e) {
                failList.add(FailRowRes.builder().row(i + 1).reason(e.getMessage()).build());
            }
        }
    }

    private AdminCreateReq parseRow(Row row) {
        AdminCreateReq req = new AdminCreateReq();
        parseCommonFields(row, req);
        req.setStatus(EnumAdminStatus.EMPLOYMENT);
        validateCommonRequired(req);
        return req;
    }

    @Override
    protected void save(AdminCreateReq req) {
        adminService.createAdmin(req, null, MemberContext.get().memberCode());
    }
}