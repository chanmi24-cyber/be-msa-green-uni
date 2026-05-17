package com.green.member.application.professor;

import com.green.common.enumcode.EnumProfessorDegree;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.member.application.admin.AdminService;
import com.green.member.application.admin.model.FailRowRes;
import com.green.member.application.major.MajorCacheRepository;
import com.green.member.application.member.MemberBatchService;
import com.green.member.application.professor.model.ProfessorCreateReq;
import com.green.member.entity.cache.MajorCache;
import com.green.member.enumcode.EnumProfessorPosition;
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
public class ProfessorBatchService extends MemberBatchService<ProfessorCreateReq> {
    private final AdminService adminService;
    private final MajorCacheRepository majorCacheRepository;

    private static final String[] HEADERS = {
        "이메일*", "이름*", "생년월일*(YYYY-MM-DD)", "전화번호*", "비상연락처",
        "우편번호", "주소", "상세주소", "입사일*(YYYY-MM-DD)", "학과명*", "직위*", "학위*"
    };

    static final String SAMPLE_EMAIL = "__professor__@example.com";

    private static final String[] SAMPLE_DATA = {
        SAMPLE_EMAIL, "홍길동", "1980-01-15", "01012345678", "01087654321",
        "06234", "서울특별시 강남구 테헤란로 123", "101호", "2026-03-01", "",
        "정교수", "박사"
    };

    private static final int[] COL_WIDTHS = {
        6000, 3000, 6000, 4500, 4500,
        3000, 9000, 5000, 6000, 5000,
        4000, 4000
    };

    @Override protected String getSheetName()    { return "교수 일괄 등록"; }
    @Override protected String getSampleEmail()  { return SAMPLE_EMAIL; }
    @Override protected String[] getHeaders()    { return HEADERS; }
    @Override protected String[] getSampleData() { return SAMPLE_DATA; }
    @Override protected int[] getColWidths()     { return COL_WIDTHS; }

    @Override
    protected void addDropdowns(Sheet sheet, XSSFWorkbook workbook, DataValidationHelper dvHelper) {
        // ── 학과명 (col 9)
        List<MajorCache> activeMajors = majorCacheRepository.findByActive("RUNNING");
        if (!activeMajors.isEmpty()) {
            Sheet majorSheet = workbook.createSheet("학과목록");
            workbook.setSheetVisibility(workbook.getSheetIndex("학과목록"), SheetVisibility.VERY_HIDDEN);
            for (int i = 0; i < activeMajors.size(); i++) {
                majorSheet.createRow(i).createCell(0).setCellValue(activeMajors.get(i).getName());
            }
            Name majorRange = workbook.createName();
            majorRange.setNameName("MajorList");
            majorRange.setRefersToFormula("'학과목록'!$A$1:$A$" + activeMajors.size());

            DataValidation majorValidation = dvHelper.createValidation(
                    dvHelper.createFormulaListConstraint("MajorList"),
                    new CellRangeAddressList(1, 1000, 9, 9));
            majorValidation.setShowErrorBox(true);
            majorValidation.createErrorBox("입력 오류", "목록에서 학과명을 선택해주세요.");
            sheet.addValidationData(majorValidation);
        }

        // ── 직위 (col 10)
        EnumProfessorPosition[] positions = EnumProfessorPosition.values();
        Sheet positionSheet = workbook.createSheet("직위목록");
        workbook.setSheetVisibility(workbook.getSheetIndex("직위목록"), SheetVisibility.VERY_HIDDEN);
        for (int i = 0; i < positions.length; i++) {
            positionSheet.createRow(i).createCell(0).setCellValue(positions[i].getValue());
        }
        Name positionRange = workbook.createName();
        positionRange.setNameName("PositionList");
        positionRange.setRefersToFormula("'직위목록'!$A$1:$A$" + positions.length);

        DataValidation positionValidation = dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("PositionList"),
                new CellRangeAddressList(1, 1000, 10, 10));
        positionValidation.setShowErrorBox(true);
        positionValidation.createErrorBox("입력 오류", "목록에서 직위를 선택해주세요.");
        sheet.addValidationData(positionValidation);

        // ── 학위 (col 11)
        EnumProfessorDegree[] degrees = EnumProfessorDegree.values();
        Sheet degreeSheet = workbook.createSheet("학위목록");
        workbook.setSheetVisibility(workbook.getSheetIndex("학위목록"), SheetVisibility.VERY_HIDDEN);
        for (int i = 0; i < degrees.length; i++) {
            degreeSheet.createRow(i).createCell(0).setCellValue(degrees[i].getValue());
        }
        Name degreeRange = workbook.createName();
        degreeRange.setNameName("DegreeList");
        degreeRange.setRefersToFormula("'학위목록'!$A$1:$A$" + degrees.length);

        DataValidation degreeValidation = dvHelper.createValidation(
                dvHelper.createFormulaListConstraint("DegreeList"),
                new CellRangeAddressList(1, 1000, 11, 11));
        degreeValidation.setShowErrorBox(true);
        degreeValidation.createErrorBox("입력 오류", "목록에서 학위를 선택해주세요.");
        sheet.addValidationData(degreeValidation);
    }

    @Override
    protected void populateRows(Sheet sheet, List<ProfessorCreateReq> validRows, List<FailRowRes> failList) {
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

    private ProfessorCreateReq parseRow(Row row, Map<String, Long> majorNameToId) {
        ProfessorCreateReq req = new ProfessorCreateReq();
        parseCommonFields(row, req);

        String majorName = getString(row, 9);
        if (majorName.isEmpty()) throw new IllegalArgumentException("학과명 필수");
        Long majorId = majorNameToId.get(majorName);
        if (majorId == null) throw new IllegalArgumentException("존재하지 않는 학과: " + majorName);
        req.setMajorId(majorId);

        req.setPosition(parsePosition(getString(row, 10)));
        req.setDegree(parseDegree(getString(row, 11)));
        req.setStatus(EnumProfessorStatus.EMPLOYMENT);

        validateCommonRequired(req);
        if (req.getPosition() == null) throw new IllegalArgumentException("직위 필수");
        if (req.getDegree() == null)   throw new IllegalArgumentException("학위 필수");
        return req;
    }

    @Override
    protected void save(ProfessorCreateReq req) {
        adminService.createProfessor(req, null);
    }

    private EnumProfessorPosition parsePosition(String value) {
        if (value == null || value.isEmpty()) return null;
        for (EnumProfessorPosition p : EnumProfessorPosition.values()) {
            if (p.getValue().equals(value)) return p;
        }
        throw new IllegalArgumentException("유효하지 않은 직위: " + value);
    }

    private EnumProfessorDegree parseDegree(String value) {
        if (value == null || value.isEmpty()) return null;
        for (EnumProfessorDegree d : EnumProfessorDegree.values()) {
            if (d.getValue().equals(value)) return d;
        }
        throw new IllegalArgumentException("유효하지 않은 학위: " + value);
    }
}