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
import java.util.HashMap;
import java.util.Map;

@Service
public class OcrMappingService {
    public static String ocrMapping(String jsonResponse, String startKeyword) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        JsonNode images = rootNode.path("images");

        StringBuilder resultBuilder = new StringBuilder();
        

        for (JsonNode image : images) {
            JsonNode tables = image.path("tables");
            // 여기서 이미지 수(페이지) 갈린다.
            int keywordRow = -1;
            int maxRowIndex = -1;
            int maxColumnIndex = -1;

            // 데이터 저장을 위한 맵
            Map<Integer, Map<Integer, String>> dataMap = new HashMap<>();

            for (JsonNode table : tables) {
                for (JsonNode cell : table.path("cells")) {
                    // 셀의 텍스트 내용 생성
                    StringBuilder cellContent = new StringBuilder();
                    for (JsonNode cellTextLine : cell.path("cellTextLines")) {
                        for (JsonNode cellWord : cellTextLine.path("cellWords")) {
                            cellContent.append(cellWord.path("inferText").asText()).append(" ");
                        }
                    }

                    // cellContent에서 최종 텍스트 추출
                    String content = cellContent.toString().trim();
                    int rowIndex = cell.get("rowIndex").asInt();
                    int columnIndex = cell.get("columnIndex").asInt();

                    // 헤더 찾기
                    if (content.contains(startKeyword)) {
                        System.out.println("header found: " + content);
                        keywordRow = rowIndex; // 키워드 발견 시 행 인덱스 저장
                        continue;
                    }

                    dataMap.computeIfAbsent(columnIndex, k -> new HashMap<>()).computeIfAbsent(rowIndex, k -> content);
                    maxRowIndex = Math.max(maxRowIndex, rowIndex); // 최대 열 인덱스 갱신
                    maxColumnIndex = Math.max(maxColumnIndex, columnIndex);
                }

                // System.out.println("max: " + maxColumnIndex);

                // 결과 생성
                for (int i = 1; i <= maxRowIndex; i++) {
                    for (int j = 0; j <= maxColumnIndex; j++) {
                        Map<Integer, String> rowMap = dataMap.getOrDefault(j, new HashMap<>());
                        String rowValues = rowMap.getOrDefault(i, "");
    
                        // System.out.println("(" + i + "," + j+"): " + rowValues);
    
                        if (rowValues.isEmpty() || rowValues.equals(" ") || rowValues == null) resultBuilder.append("");
                        else resultBuilder.append(rowValues).append("\t"); // 탭으로 구분하여 저장                    
                    }
                    resultBuilder.append("\n"); // 각 열의 데이터가 끝나면 줄 바꿈
                }
            }
        }
        return resultBuilder.toString();
    }
    

    public static void ocrToExcel(String result, String fileName) throws IOException {
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
        
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("OCR Data");
        
        

        // result 문자열을 줄 바꿈으로 나누기
        String[] rows = result.split("\n");
        
        for (int i = 0; i < rows.length; i++) {
            XSSFRow excelRow = sheet.createRow(i);
            // 각 행을 탭으로 나누기
            String[] columns = rows[i].split("\t");
            for (int j = 0; j < columns.length; j++) {
                XSSFCell cell = excelRow.createCell(j);
                cell.setCellValue(columns[j]);
            }
        }
        // 파일 저장
        try (FileOutputStream outputStream = new FileOutputStream(outputFilePath)) {
            workbook.write(outputStream);
        }
        workbook.close();
    }
}