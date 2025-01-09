package net.yyhis.transact_analyze.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class OcrMappingService {
    public void tableOCRMappingExcel(String jsonResponse, String startKeyword, String fileName) throws IOException {
        String outputFilePath = "";
                try {
                    String oneDrivePath = System.getenv("OneDrive") + File.separator + "바탕 화면" + File.separator + "test";
                    File oneDriveTestDir = new File(oneDrivePath);
                        
                    if (oneDriveTestDir.exists()) {
                        outputFilePath = oneDrivePath + File.separator + fileName + "_OCR.xlsx";
                        System.out.println("Using path: " + outputFilePath); // 디버깅용 로그
                    } else {
                        // test 디렉토리가 없다면 생성
                        if (oneDriveTestDir.mkdirs()) {
                            outputFilePath = oneDrivePath + File.separator + fileName + "_OCR.xlsx";
                            System.out.println("Created directory and using path: " + outputFilePath); // 디버깅용 로그
                        } else {
                            throw new IOException("Failed to create directory: " + oneDrivePath);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error occurred: " + e.getMessage()); // 에러 로깅
                    // 기본 경로로 폴백
                    outputFilePath = System.getProperty("user.home") + File.separator + "Documents" 
                                    + File.separator + "OCR_Result.xlsx";
                }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode images = rootNode.path("images");

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("OCR Data");
        
        List<List<String>> rowData = new ArrayList<>();
    List<Integer> validColumns = new ArrayList<>();
    Set<String> processedTransactions = new HashSet<>();
    int maxColumn = 0;
    int keywordRow = -1;

    // 전체 테이블에서 한 번만 처리하도록 수정
    for (JsonNode image : images) {
        JsonNode tables = image.path("tables");
        
        for (JsonNode table : tables) {
            // 먼저 키워드 행과 헤더 정보 찾기
            Map<Integer, String> headerCells = new HashMap<>();
            for (JsonNode cell : table.path("cells")) {
                int rowIndex = cell.get("rowIndex").asInt();
                int columnIndex = cell.get("columnIndex").asInt();
                maxColumn = Math.max(maxColumn, columnIndex);

                StringBuilder cellContent = new StringBuilder();
                for (JsonNode cellTextLine : cell.path("cellTextLines")) {
                    for (JsonNode cellWord : cellTextLine.path("cellWords")) {
                        cellContent.append(cellWord.path("inferText").asText()).append(" ");
                    }
                }
                String content = cellContent.toString().trim();

                if (content.contains(startKeyword)) {
                    keywordRow = rowIndex;
                }
                
                if (rowIndex == keywordRow) {
                    headerCells.put(columnIndex, content);
                }
            }

            // 유효한 컬럼 찾기
            if (keywordRow != -1) {
                validColumns.clear(); // 기존 컬럼 정보 초기화
                for (int i = 0; i <= maxColumn; i++) {
                    if (headerCells.containsKey(i) && !headerCells.get(i).isEmpty()) {
                        validColumns.add(i);
                    }
                }

                // 데이터 수집
                Map<Integer, List<String>> rowsToAdd = new HashMap<>();
                for (JsonNode cell : table.path("cells")) {
                    int rowIndex = cell.get("rowIndex").asInt();
                    int columnIndex = cell.get("columnIndex").asInt();
                    
                    if (rowIndex > keywordRow && validColumns.contains(columnIndex)) {
                        StringBuilder cellContent = new StringBuilder();
                        
                        for (JsonNode cellTextLine : cell.path("cellTextLines")) {
                            for (JsonNode cellWord : cellTextLine.path("cellWords")) {
                                cellContent.append(cellWord.path("inferText").asText()).append(" ");
                            }
                        }
                        
                        rowsToAdd.computeIfAbsent(rowIndex, k -> new ArrayList<>())
                               .add(cellContent.toString().trim());
                    }
                }

                // 중복 체크 후 데이터 추가
                for (List<String> row : rowsToAdd.values()) {
                    if (row.size() >= 2) { // 최소한 거래일시와 금액이 있는지 확인
                        String transactionKey = row.get(0) + "|" + row.get(2); // 거래일시와 금액 조합
                        if (!processedTransactions.contains(transactionKey)) {
                            rowData.add(row);
                            processedTransactions.add(transactionKey);
                        }
                    }
                }
            }
        }

        // Excel에 데이터 쓰기
        for (int i = 0; i < rowData.size(); i++) {
            XSSFRow excelRow = sheet.createRow(i);
            List<String> rowValues = rowData.get(i);
            for (int j = 0; j < rowValues.size(); j++) {
                XSSFCell cell = excelRow.createCell(j);
                cell.setCellValue(rowValues.get(j));
            }
        }
    }
        // 파일 저장
        try (FileOutputStream outputStream = new FileOutputStream(outputFilePath)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }

    // 바로 옆에 값 탐색
    private String getSideAdjacentCellValue(JsonNode tables, int rowIndex, int adjacentColumnIndex) {
        for (JsonNode table : tables) {
            for (JsonNode cell : table.path("cells")) {
                int currentRowIndex = cell.get("rowIndex").asInt();
                int currentColumnIndex = cell.get("columnIndex").asInt();

                if (currentRowIndex == rowIndex && currentColumnIndex == adjacentColumnIndex) {
                    StringBuilder adjacentCellContent = new StringBuilder();
                    for (JsonNode cellTextLine : cell.path("cellTextLines")) {
                        for (JsonNode cellWord : cellTextLine.path("cellWords")) {
                            adjacentCellContent.append(cellWord.path("inferText").asText()).append(" ");
                        }
                    }
                    return adjacentCellContent.toString().trim();
                }
            }
        }
        return ""; // 인접 셀이 없으면 빈 문자열 반환
    }

    // 해당 row의 column 값들 탐색
    private List<String> getLineAdjacentCellValue(JsonNode tables, int rowIndex, int maxColumnIndex) {
        List<String> lineCellValue = new ArrayList<>();

        for (JsonNode table : tables) {
            for (JsonNode cell : table.path("cells")) {
                int currentRowIndex = cell.get("rowIndex").asInt();
                int currentColumnIndex = cell.get("columnIndex").asInt();

                if (currentRowIndex == rowIndex && maxColumnIndex >= currentColumnIndex) {
                    StringBuilder adjacentCellContent = new StringBuilder();
                    for (JsonNode cellTextLine : cell.path("cellTextLines")) {
                        for (JsonNode cellWord : cellTextLine.path("cellWords")) {
                            adjacentCellContent.append(cellWord.path("inferText").asText()).append(" ");
                        }
                    }
                    System.out.println(adjacentCellContent.toString().trim());
                    lineCellValue.add(adjacentCellContent.toString().trim());
                }
            }
        }
        return lineCellValue;
    }
}
