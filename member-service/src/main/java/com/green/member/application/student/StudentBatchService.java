package com.green.member.application.student;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.application.admin.AdminService;
import com.green.member.application.admin.model.FailRowRes;
import com.green.member.application.major.MajorCacheRepository;
import com.green.member.application.member.MemberBatchService;
import com.green.member.application.student.model.StudentCreateReq;
import com.green.member.entity.cache.MajorCache;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentBatchService extends MemberBatchService<StudentCreateReq> {
    private final AdminService adminService;
    private final MajorCacheRepository majorCacheRepository;

    private static final String[] HEADERS = {
        "이메일*", "이름*", "생년월일*(YYYY-MM-DD)", "전화번호*", "비상연락처",
        "우편번호", "주소", "상세주소", "입학일*(YYYY-MM-DD)", "학과명*",
        "편입여부(Y)", "다자녀여부(Y)", "보훈여부(Y)"
    };

    static final String SAMPLE_EMAIL = "__student__@example.com";

    private static final String[] SAMPLE_DATA = {
        SAMPLE_EMAIL, "홍길동", "2000-01-15", "01012345678", "01087654321",
        "06234", "서울특별시 강남구 테헤란로 123", "101호", "2024-03-01", "",
        "", "", ""
    };

    private static final int[] COL_WIDTHS = {
        6000, 3000, 6000, 4500, 4500,
        3000, 9000, 5000, 6000, 5000,
        5000, 5000, 5000
    };

    @Override protected String getSheetName()    { return "학생 일괄 등록"; }
    @Override protected String getSampleEmail()  { return SAMPLE_EMAIL; }
    @Override protected String[] getHeaders()    { return HEADERS; }
    @Override protected String[] getSampleData() { return SAMPLE_DATA; }
    @Override protected int[] getColWidths()     { return COL_WIDTHS; }
    @Override protected String getEntryDateLabel() { return "입학일"; }

    @Override
    protected void addDropdowns(Sheet sheet, XSSFWorkbook workbook, DataValidationHelper dvHelper) {
        List<MajorCache> activeMajors = majorCacheRepository.findByActive("RUNNING");
        if (activeMajors.isEmpty()) return;

        Sheet majorSheet = workbook.createSheet("학과목록");
        workbook.setSheetVisibility(workbook.getSheetIndex("학과목록"), SheetVisibility.VERY_HIDDEN);
        for (int i = 0; i < activeMajors.size(); i++) {
            majorSheet.createRow(i).createCell(0).setCellValue(activeMajors.get(i).getName());
        }
        Name namedRange = workbook.createName();
        namedRange.setNameName("MajorList");
        namedRange.setRefersToFormula("'학과목록'!$A$1:$A$" + activeMajors.size());

        DataValidation validation = dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("MajorList"),
                new CellRangeAddressList(1, 1000, 9, 9));
        validation.setShowErrorBox(true);
        validation.createErrorBox("입력 오류", "목록에서 학과명을 선택해주세요.");
        sheet.addValidationData(validation);
    }

    @Override
    protected void populateRows(Sheet sheet, List<StudentCreateReq> validRows, List<FailRowRes> failList) {
        Map<String, Long> majorNameToId = majorCacheRepository.findByActive("RUNNING").stream()
                .collect(Collectors.toMap(MajorCache::getName, MajorCache::getMajorId));

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (shouldSkipRow(row)) continue;
            try {
                validRows.add(parseRow(row, majorNameToId));
            } catch (Exception e) {
                failList.add(FailRowRes.builder().row(i + 1).reason(e.getMessage()).build());
            }
        }
    }

    private StudentCreateReq parseRow(Row row, Map<String, Long> majorNameToId) {
        StudentCreateReq req = new StudentCreateReq();
        parseCommonFields(row, req);

        String majorName = getString(row, 9);
        if (majorName.isEmpty()) throw new IllegalArgumentException("학과명 필수");
        Long majorId = majorNameToId.get(majorName);
        if (majorId == null) throw new IllegalArgumentException("존재하지 않는 학과: " + majorName);
        req.setMajorId(majorId);

        req.setAcademicYear(1);
        req.setSemester(1);
        req.setIsTransfer(parseBoolean(getString(row, 10)));
        req.setIsMultiChild(parseBoolean(getString(row, 11)));
        req.setIsVeteran(parseBoolean(getString(row, 12)));
        req.setStatus(EnumStudentStatus.UNREGISTERED);

        validateCommonRequired(req);
        return req;
    }

    @Override
    protected void save(StudentCreateReq req) {
        adminService.createStudent(req, null);
    }
}