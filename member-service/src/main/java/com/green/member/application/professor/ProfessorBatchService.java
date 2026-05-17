package com.green.member.application.professor;

import com.green.common.enumcode.EnumProfessorDegree;
import com.green.common.enumcode.EnumProfessorStatus;
import com.green.member.application.admin.AdminService;
import com.green.member.application.admin.model.FailRowRes;
import com.green.member.application.admin.model.MemberBatchRes;
import com.green.member.application.major.MajorCacheRepository;
import com.green.member.application.professor.model.ProfessorCreateReq;
import com.green.member.entity.cache.MajorCache;
import com.green.member.enumcode.EnumProfessorPosition;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfessorBatchService {
    private final AdminService adminService;
    private final MajorCacheRepository majorCacheRepository;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_ROW_COUNT = 500;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TEL_PATTERN = Pattern.compile("^0\\d{9,10}$");

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

    public byte[] generateTemplate() throws IOException {
        List<MajorCache> activeMajors = majorCacheRepository.findByActive("RUNNING");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("교수 일괄 등록");

            // ── 헤더 스타일
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(headerStyle);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontName("맑은 고딕");
            headerFont.setFontHeightInPoints((short) 10);
            headerStyle.setFont(headerFont);

            DataFormat dataFormat = workbook.createDataFormat();

            // ── 샘플 데이터 스타일
            Font sampleFont = workbook.createFont();
            sampleFont.setItalic(true);
            sampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            sampleFont.setFontName("맑은 고딕");
            sampleFont.setFontHeightInPoints((short) 9);

            CellStyle sampleStyle = workbook.createCellStyle();
            sampleStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            sampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(sampleStyle);
            sampleStyle.setFont(sampleFont);

            CellStyle sampleTextStyle = workbook.createCellStyle();
            sampleTextStyle.cloneStyleFrom(sampleStyle);
            sampleTextStyle.setDataFormat(dataFormat.getFormat("@"));

            CellStyle sampleDateStyle = workbook.createCellStyle();
            sampleDateStyle.cloneStyleFrom(sampleStyle);
            sampleDateStyle.setDataFormat(dataFormat.getFormat("YYYY-MM-DD"));

            CellStyle colTextStyle = workbook.createCellStyle();
            colTextStyle.setDataFormat(dataFormat.getFormat("@"));

            CellStyle colDateStyle = workbook.createCellStyle();
            colDateStyle.setDataFormat(dataFormat.getFormat("YYYY-MM-DD"));

            // ── 헤더 행
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── 샘플 데이터 행
            Row sampleRow = sheet.createRow(1);
            sampleRow.setHeightInPoints(18);
            for (int i = 0; i < SAMPLE_DATA.length; i++) {
                Cell cell = sampleRow.createCell(i);
                cell.setCellValue(SAMPLE_DATA[i]);
                if (i == 2 || i == 8) {
                    cell.setCellStyle(sampleDateStyle);
                } else if (i == 3 || i == 4 || i == 5) {
                    cell.setCellStyle(sampleTextStyle);
                } else {
                    cell.setCellStyle(sampleStyle);
                }
            }

            // ── 열 기본 서식
            sheet.setDefaultColumnStyle(2, colDateStyle);
            sheet.setDefaultColumnStyle(3, colTextStyle);
            sheet.setDefaultColumnStyle(4, colTextStyle);
            sheet.setDefaultColumnStyle(5, colTextStyle);
            sheet.setDefaultColumnStyle(8, colDateStyle);

            DataValidationHelper dvHelper = sheet.getDataValidationHelper();

            // ── 학과명 드롭다운 (col 9)
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

            // ── 직위 드롭다운 (col 10)
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

            // ── 학위 드롭다운 (col 11)
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

            // ── 열 너비 + 헤더 고정
            for (int i = 0; i < COL_WIDTHS.length; i++) {
                sheet.setColumnWidth(i, COL_WIDTHS[i]);
            }
            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    @Transactional
    public MemberBatchRes batchRegister(MultipartFile file) throws IOException {
        validateFile(file);

        List<ProfessorCreateReq> validRows = new ArrayList<>();
        List<FailRowRes> failList = new ArrayList<>();

        Map<String, Long> majorNameToId = majorCacheRepository.findByActive("RUNNING").stream()
                .collect(Collectors.toMap(MajorCache::getName, MajorCache::getMajorId));

        // ── 1단계: 파싱·검증만
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getLastRowNum() > MAX_ROW_COUNT) {
                throw new IllegalArgumentException(
                    "최대 " + MAX_ROW_COUNT + "행까지 등록할 수 있습니다. (현재: " + sheet.getLastRowNum() + "행)");
            }
            if (sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("파일에 데이터 행이 없습니다. 2행부터 데이터를 입력하세요.");
            }

            int skippedCount = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || getString(row, 0).isEmpty() || SAMPLE_EMAIL.equals(getString(row, 0))) { skippedCount++; continue; }
                try {
                    validRows.add(parseRow(row, majorNameToId));
                } catch (Exception e) {
                    failList.add(FailRowRes.builder().row(i + 1).reason(e.getMessage()).build());
                }
            }

            if (validRows.isEmpty() && failList.isEmpty()) {
                throw new IllegalArgumentException(
                    "처리 가능한 데이터가 없습니다. 이메일 컬럼(A열)이 비어있는지 확인하세요. (건너뜀: " + skippedCount + "행)");
            }
        }

        if (!failList.isEmpty()) {
            return MemberBatchRes.builder()
                    .successCount(0).failCount(failList.size()).failList(failList).build();
        }

        // ── 2단계: 전체 저장 (하나라도 실패 시 전체 롤백)
        for (int i = 0; i < validRows.size(); i++) {
            try {
                adminService.createProfessor(validRows.get(i), null);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                String reason = e.getMessage() != null && e.getMessage().contains("Duplicate entry")
                        ? "이미 등록된 이메일 또는 전화번호"
                        : "데이터 저장 오류가 발생";
                throw new IllegalStateException(
                    String.format("%d행 오류로 전체 등록이 취소되었습니다. (%s) 수정 후 전체 파일을 재업로드해주세요.", i + 2, reason));
            }
        }

        return MemberBatchRes.builder()
                .successCount(validRows.size()).failCount(0).failList(List.of()).build();
    }

    private ProfessorCreateReq parseRow(Row row, Map<String, Long> majorNameToId) {
        ProfessorCreateReq req = new ProfessorCreateReq();
        req.setEmail(getString(row, 0));
        req.setName(getString(row, 1));
        req.setBirth(parseDate(getString(row, 2), "생년월일"));
        req.setTel(getString(row, 3));
        String emergencyTel = getString(row, 4);
        if (!emergencyTel.isEmpty() && !TEL_PATTERN.matcher(emergencyTel).matches()) {
            throw new IllegalArgumentException("비상연락처 형식 오류 (숫자 10~11자리): " + emergencyTel);
        }
        req.setEmergencyTel(emergencyTel);
        req.setPostcode(getString(row, 5));
        req.setAddress(getString(row, 6));
        req.setDetailAddress(getString(row, 7));
        req.setEntryDate(parseDate(getString(row, 8), "입사일"));

        String majorName = getString(row, 9);
        if (majorName.isEmpty()) throw new IllegalArgumentException("학과명 필수");
        Long majorId = majorNameToId.get(majorName);
        if (majorId == null) throw new IllegalArgumentException("존재하지 않는 학과: " + majorName);
        req.setMajorId(majorId);

        req.setPosition(parsePosition(getString(row, 10)));
        req.setDegree(parseDegree(getString(row, 11)));
        req.setStatus(EnumProfessorStatus.EMPLOYMENT);

        validateRequired(req);
        return req;
    }

    private void validateRequired(ProfessorCreateReq req) {
        if (req.getEmail() == null || req.getEmail().isEmpty()) throw new IllegalArgumentException("이메일 필수");
        if (!EMAIL_PATTERN.matcher(req.getEmail()).matches())   throw new IllegalArgumentException("이메일 형식 오류: " + req.getEmail());
        if (req.getName() == null || req.getName().isEmpty())   throw new IllegalArgumentException("이름 필수");
        if (req.getBirth() == null)                             throw new IllegalArgumentException("생년월일 오류 (YYYY-MM-DD)");
        if (req.getTel() == null || req.getTel().isEmpty())     throw new IllegalArgumentException("전화번호 필수");
        if (!TEL_PATTERN.matcher(req.getTel()).matches())       throw new IllegalArgumentException("전화번호 형식 오류 (숫자 10~11자리): " + req.getTel());
        if (req.getEntryDate() == null)                         throw new IllegalArgumentException("입사일 오류 (YYYY-MM-DD)");
        if (req.getPosition() == null)                          throw new IllegalArgumentException("직위 필수");
        if (req.getDegree() == null)                            throw new IllegalArgumentException("학위 필수");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.endsWith(".xlsx")) {
            throw new IllegalArgumentException("xlsx 파일만 허용됩니다.");
        }
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

    private String getString(Row row, int col) {
        Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        CellType type = cell.getCellType() == CellType.FORMULA
                ? cell.getCachedFormulaResultType()
                : cell.getCellType();
        return switch (type) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toLocalDate().toString()
                    : String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " 형식 오류 (YYYY-MM-DD): " + value);
        }
    }
}