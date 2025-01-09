package net.yyhis.transact_analyze.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import net.yyhis.transact_analyze.service.ClovaOcrService;
import net.yyhis.transact_analyze.service.OcrMappingService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FileController {
    @Autowired
    private ClovaOcrService clovaOcrService;

    @Autowired
    private OcrMappingService ocrMappingService;

    @PostMapping("/upload")
    public ResponseEntity<?> analyzePdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("priceRange") String priceRange,
            @RequestParam("startKeyword") String startKeyword,
            @RequestParam("fileName") String fileName) {
        try {
            File tempFile = File.createTempFile("tempPdf", ".pdf");
            file.transferTo(tempFile);

            StringBuffer result = clovaOcrService.analyzePdf(tempFile);

            // OCR 및 Excel 파일 생성
            ocrMappingService.tableOCRMappingExcel(result.toString(), startKeyword, fileName);

            return ResponseEntity.ok("success");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("An error occurred while processing the file.");
        }
    }
}