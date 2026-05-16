package com.green.member.application.student;

import com.green.common.enumcode.EnumStudentStatus;
import com.green.member.application.admin.AdminService;
import com.green.member.application.admin.model.FailRowRes;
import com.green.member.application.admin.model.MemberBatchRes;
import com.green.member.application.major.MajorCacheRepository;
import com.green.member.application.student.model.StudentCreateReq;
import com.green.member.entity.cache.MajorCache;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentBatchService {
    private final AdminService adminService;
    private final MajorCacheRepository majorCacheRepository;

    private static final String[] HEADERS = {
        "이메일*", "이름*", "생년월일*(YYYY-MM-DD)", "전화번호*", "비상연락처",
        "우편번호", "주소", "상세주소", "입학일*(YYYY-MM-DD)", "학과명*",
        "학년*", "학기*", "편입여부(Y/비워두기)", "다자녀여부(Y/비워두기)",
        "보훈여부(Y/비워두기)"
    };

    static final String SAMPLE_EMAIL = "__sample__@example.com";

    private static final String[] SAMPLE_DATA = {
        SAMPLE_EMAIL, "홍길동", "2000-01-15", "01012345678", "01087654321",
        "06234", "서울특별시 강남구 테헤란로 123", "101호", "2024-03-01", "",
        "1", "1", "", "", ""
    };

    private static final int[] COL_WIDTHS = {
        6000, 3000, 5000, 4500, 4500,
        3500, 9000, 4000, 5000, 3000,
        2500, 2500, 5000, 5000, 5000
    };

    public byte[] generateTemplate() throws IOException {
        List<MajorCache> activeMajors = majorCacheRepository.findByActive("RUNNING");

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("학생 일괄 등록");

            // ── 헤더 스타일
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(headerStyle);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontName("맑은 고딕");
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);

            DataFormat dataFormat = workbook.createDataFormat();

            // ── 샘플 데이터 스타일 (기본)
            Font sampleFont = workbook.createFont();
            sampleFont.setItalic(true);
            sampleFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
            sampleFont.setFontName("맑은 고딕");
            sampleFont.setFontHeightInPoints((short) 10);

            CellStyle sampleStyle = workbook.createCellStyle();
            sampleStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            sampleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(sampleStyle);
            sampleStyle.setFont(sampleFont);

            // ── 샘플 + 텍스트 서식 (전화번호·우편번호용)
            CellStyle sampleTextStyle = workbook.createCellStyle();
            sampleTextStyle.cloneStyleFrom(sampleStyle);
            sampleTextStyle.setDataFormat(dataFormat.getFormat("@"));

            // ── 샘플 + 날짜 서식 (생년월일·입학일용)
            CellStyle sampleDateStyle = workbook.createCellStyle();
            sampleDateStyle.cloneStyleFrom(sampleStyle);
            sampleDateStyle.setDataFormat(dataFormat.getFormat("YYYY-MM-DD"));

            // ── 데이터 입력 열 서식 (사용자가 입력할 빈 셀에 적용)
            CellStyle colTextStyle = workbook.createCellStyle();
            colTextStyle.setDataFormat(dataFormat.getFormat("@"));

            CellStyle colDateStyle = workbook.createCellStyle();
            colDateStyle.setDataFormat(dataFormat.getFormat("YYYY-MM-DD"));

            // ── 헤더 행 (0행)
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── 샘플 데이터 행 (열별 서식 적용)
            Row sampleRow = sheet.createRow(1);
            sampleRow.setHeightInPoints(18);
            for (int i = 0; i < SAMPLE_DATA.length; i++) {
                Cell cell = sampleRow.createCell(i);
                cell.setCellValue(SAMPLE_DATA[i]);
                // 날짜 열: C(2), I(8) / 텍스트 열: D(3), E(4), F(5)
                if (i == 2 || i == 8) {
                    cell.setCellStyle(sampleDateStyle);
                } else if (i == 3 || i == 4 || i == 5) {
                    cell.setCellStyle(sampleTextStyle);
                } else {
                    cell.setCellStyle(sampleStyle);
                }
            }

            // ── 열 기본 서식 (사용자 입력 셀에 자동 적용)
            sheet.setDefaultColumnStyle(2, colDateStyle);  // 생년월일
            sheet.setDefaultColumnStyle(3, colTextStyle);  // 전화번호
            sheet.setDefaultColumnStyle(4, colTextStyle);  // 비상연락처
            sheet.setDefaultColumnStyle(5, colTextStyle);  // 우편번호
            sheet.setDefaultColumnStyle(8, colDateStyle);  // 입학일

            // ── 학과명 드롭다운 (숨김 시트 + Named Range 방식)
            if (!activeMajors.isEmpty()) {
                Sheet majorSheet = workbook.createSheet("학과목록");
                workbook.setSheetVisibility(
                        workbook.getSheetIndex("학과목록"), SheetVisibility.VERY_HIDDEN);
                for (int i = 0; i < activeMajors.size(); i++) {
                    majorSheet.createRow(i).createCell(0)
                            .setCellValue(activeMajors.get(i).getName());
                }

                Name namedRange = workbook.createName();
                namedRange.setNameName("MajorList");
                namedRange.setRefersToFormula("'학과목록'!$A$1:$A$" + activeMajors.size());

                DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint =
                        dvHelper.createFormulaListConstraint("MajorList");
                CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, 9, 9);
                DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
                validation.setShowErrorBox(true);
                validation.createErrorBox("입력 오류", "목록에서 학과명을 선택해주세요.");
                sheet.addValidationData(validation);
            }

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

    @org.springframework.transaction.annotation.Transactional
    public MemberBatchRes batchRegister(MultipartFile file) throws IOException {
        validateFile(file);

        List<StudentCreateReq> validRows = new ArrayList<>();
        List<FailRowRes> failList = new ArrayList<>();

        Map<String, Long> majorNameToId = majorCacheRepository.findByActive("RUNNING").stream()
                .collect(Collectors.toMap(MajorCache::getName, MajorCache::getMajorId));

        // ── 1단계: 파싱·검증만 (DB 접근 없음) ──────────────
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

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

        // 검증 오류가 하나라도 있으면 저장 없이 반환 (DB 미접근이므로 롤백 불필요)
        if (!failList.isEmpty()) {
            return MemberBatchRes.builder()
                    .successCount(0).failCount(failList.size()).failList(failList).build();
        }

        // ── 2단계: 전체 저장 (하나라도 실패 시 전체 롤백) ────
        for (int i = 0; i < validRows.size(); i++) {
            try {
                adminService.createStudent(validRows.get(i), null);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                String reason = e.getMessage() != null && e.getMessage().contains("Duplicate entry")
                        ? "이미 등록된 이메일 또는 전화번호"
                        : "DB 저장 오류";
                throw new IllegalStateException(
                    String.format("%d행 오류로 전체 등록이 취소되었습니다. (%s) 수정 후 전체 파일을 재업로드해주세요.", i + 2, reason));
            }
        }

        return MemberBatchRes.builder()
                .successCount(validRows.size()).failCount(0).failList(List.of()).build();
    }

    private StudentCreateReq parseRow(Row row, Map<String, Long> majorNameToId) {
        StudentCreateReq req = new StudentCreateReq();
        req.setEmail(getString(row, 0));
        req.setName(getString(row, 1));
        req.setBirth(parseDate(getString(row, 2), "생년월일"));
        req.setTel(getString(row, 3));
        req.setEmergencyTel(getString(row, 4));
        req.setPostcode(getString(row, 5));
        req.setAddress(getString(row, 6));
        req.setDetailAddress(getString(row, 7));
        req.setEntryDate(parseDate(getString(row, 8), "입학일"));

        String majorName = getString(row, 9);
        if (majorName.isEmpty()) throw new IllegalArgumentException("학과명 필수");
        Long majorId = majorNameToId.get(majorName);
        if (majorId == null) throw new IllegalArgumentException("존재하지 않는 학과명: " + majorName);
        req.setMajorId(majorId);

        req.setAcademicYear(parseInt(getString(row, 10), "학년"));
        req.setSemester(parseInt(getString(row, 11), "학기"));
        req.setIsTransfer(parseBoolean(getString(row, 12)));
        req.setIsMultiChild(parseBoolean(getString(row, 13)));
        req.setIsVeteran(parseBoolean(getString(row, 14)));
        req.setStatus(EnumStudentStatus.UNREGISTERED);

        validateRequired(req);
        return req;
    }

    private void validateRequired(StudentCreateReq req) {
        if (req.getEmail() == null || req.getEmail().isEmpty()) throw new IllegalArgumentException("이메일 필수");
        if (req.getName() == null || req.getName().isEmpty())   throw new IllegalArgumentException("이름 필수");
        if (req.getBirth() == null)                             throw new IllegalArgumentException("생년월일 오류 (YYYY-MM-DD)");
        if (req.getTel() == null || req.getTel().isEmpty())     throw new IllegalArgumentException("전화번호 필수");
        if (req.getEntryDate() == null)                         throw new IllegalArgumentException("입학일 오류 (YYYY-MM-DD)");
        if (req.getAcademicYear() == null)                      throw new IllegalArgumentException("학년 필수");
        if (req.getSemester() == null)                          throw new IllegalArgumentException("학기 필수");
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.endsWith(".xlsx")) {
            throw new IllegalArgumentException("xlsx 파일만 허용됩니다.");
        }
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

private Integer parseInt(String value, String fieldName) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " 숫자 형식 오류: " + value);
        }
    }

    private Boolean parseBoolean(String value) {
        return "TRUE".equalsIgnoreCase(value) || "Y".equalsIgnoreCase(value);
    }

}