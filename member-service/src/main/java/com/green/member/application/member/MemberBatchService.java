package com.green.member.application.member;

import com.green.member.application.admin.model.FailRowRes;
import com.green.member.application.admin.model.MemberBatchRes;
import com.green.member.application.member.model.MemberCreateReq;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class MemberBatchService<T extends MemberCreateReq> {

    // 서비스별 application.yaml의 constants.file.max-size 값 사용
    // 설정이 없으면 기본값 5MB(5242880 bytes) 적용
    @Value("${constants.file.max-size:5242880}")
    private long maxFileSize;
    protected static final int MAX_ROW_COUNT = 500;
    protected static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    protected static final Pattern TEL_PATTERN = Pattern.compile("^0\\d{9,10}$");

    // ── 서브클래스 구현 필수
    protected abstract String getSheetName();
    protected abstract String getSampleEmail();
    protected abstract String[] getHeaders();
    protected abstract String[] getSampleData();
    protected abstract int[] getColWidths();
    protected abstract void addDropdowns(Sheet sheet, XSSFWorkbook workbook, DataValidationHelper dvHelper);
    protected abstract void populateRows(Sheet sheet, List<T> validRows, List<FailRowRes> failList);
    protected abstract void save(T req);

    // ── 선택적 훅 (기본값: "입사일", 학생은 "입학일"로 오버라이드)
    protected String getEntryDateLabel() { return "입사일"; }

    public byte[] generateTemplate() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(getSheetName());

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
            String[] headers = getHeaders();
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(20);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── 샘플 데이터 행 (0-indexed: 2·8=날짜, 3·4·5=텍스트)
            String[] sampleData = getSampleData();
            Row sampleRow = sheet.createRow(1);
            sampleRow.setHeightInPoints(18);
            for (int i = 0; i < sampleData.length; i++) {
                Cell cell = sampleRow.createCell(i);
                cell.setCellValue(sampleData[i]);
                if (i == 2 || i == 8) {
                    cell.setCellStyle(sampleDateStyle);
                } else if (i == 3 || i == 4 || i == 5) {
                    cell.setCellStyle(sampleTextStyle);
                } else {
                    cell.setCellStyle(sampleStyle);
                }
            }

            // ── 공통 열 서식 (Member 공통 9개 컬럼 기준)
            sheet.setDefaultColumnStyle(2, colDateStyle);
            sheet.setDefaultColumnStyle(3, colTextStyle);
            sheet.setDefaultColumnStyle(4, colTextStyle);
            sheet.setDefaultColumnStyle(5, colTextStyle);
            sheet.setDefaultColumnStyle(8, colDateStyle);

            addDropdowns(sheet, workbook, sheet.getDataValidationHelper());

            int[] colWidths = getColWidths();
            for (int i = 0; i < colWidths.length; i++) {
                sheet.setColumnWidth(i, colWidths[i]);
            }
            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional
    public MemberBatchRes batchRegister(MultipartFile file) throws IOException {
        validateFile(file);

        List<T> validRows = new ArrayList<>();
        List<FailRowRes> failList = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getLastRowNum() > MAX_ROW_COUNT) {
                throw new IllegalArgumentException(
                    "최대 " + MAX_ROW_COUNT + "행까지 등록할 수 있습니다. (현재: " + sheet.getLastRowNum() + "행)");
            }
            if (sheet.getLastRowNum() < 1) {
                throw new IllegalArgumentException("파일에 데이터 행이 없습니다. 2행부터 데이터를 입력하세요.");
            }

            populateRows(sheet, validRows, failList);

            if (validRows.isEmpty() && failList.isEmpty()) {
                throw new IllegalArgumentException("처리 가능한 데이터가 없습니다. 이메일 컬럼(A열)이 비어있는지 확인하세요.");
            }
        }

        if (!failList.isEmpty()) {
            return MemberBatchRes.builder()
                    .successCount(0).failCount(failList.size()).failList(failList).build();
        }

        // ── 2단계: 전체 저장 (하나라도 실패 시 전체 롤백)
        for (int i = 0; i < validRows.size(); i++) {
            try {
                save(validRows.get(i));
            } catch (DataIntegrityViolationException e) {
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

    // ── 공통 헬퍼

    protected boolean shouldSkipRow(Row row) {
        return row == null || getString(row, 0).isEmpty() || getSampleEmail().equals(getString(row, 0));
    }

    protected void parseCommonFields(Row row, T req) {
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
        req.setEntryDate(parseDate(getString(row, 8), getEntryDateLabel()));
    }

    protected void validateCommonRequired(MemberCreateReq req) {
        if (req.getEmail() == null || req.getEmail().isEmpty()) throw new IllegalArgumentException("이메일 필수");
        if (!EMAIL_PATTERN.matcher(req.getEmail()).matches())   throw new IllegalArgumentException("이메일 형식 오류: " + req.getEmail());
        if (req.getName() == null || req.getName().isEmpty())   throw new IllegalArgumentException("이름 필수");
        if (req.getBirth() == null)                             throw new IllegalArgumentException("생년월일 오류 (YYYY-MM-DD)");
        if (req.getTel() == null || req.getTel().isEmpty())     throw new IllegalArgumentException("전화번호 필수");
        if (!TEL_PATTERN.matcher(req.getTel()).matches())       throw new IllegalArgumentException("전화번호 형식 오류 (숫자 10~11자리): " + req.getTel());
        if (req.getEntryDate() == null)                         throw new IllegalArgumentException(getEntryDateLabel() + " 오류 (YYYY-MM-DD)");
    }

    protected String getString(Row row, int col) {
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

    protected LocalDate parseDate(String value, String fieldName) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new IllegalArgumentException(fieldName + " 형식 오류 (YYYY-MM-DD): " + value);
        }
    }

    protected boolean parseBoolean(String value) {
        return "TRUE".equalsIgnoreCase(value) || "Y".equalsIgnoreCase(value);
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        // 확장자 검사: 대소문자 구분 없이 .xlsx만 허용
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("xlsx 파일만 허용됩니다.");
        }
        // magic bytes 검사: xlsx는 ZIP 포맷. 앞 4바이트가 PK 시그니처(50 4B 03 04)여야 함
        // 파일 이름을 .xlsx로 위장한 다른 파일 유형을 파싱 전에 차단
        byte[] header = file.getInputStream().readNBytes(4);
        if (header.length < 4
                || (header[0] & 0xFF) != 0x50 || (header[1] & 0xFF) != 0x4B
                || (header[2] & 0xFF) != 0x03 || (header[3] & 0xFF) != 0x04) {
            throw new IllegalArgumentException("올바른 xlsx 파일이 아닙니다.");
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}