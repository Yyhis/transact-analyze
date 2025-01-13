package net.yyhis.transact_analyze;

import javax.swing.*;

import net.yyhis.transact_analyze.service.ClovaOcrService;
import net.yyhis.transact_analyze.service.OcrMappingService;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.io.IOException;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TransactionAnalyzerApp extends JFrame {

    private JTextField startKeywordField;
    private JComboBox<String> priceRangeComboBox;
    private JTextArea ocrResultArea;
    private JButton uploadButton;
    private JButton copyButton;
    private JButton clearButton;
    private JPanel dragPanel;
    
    public TransactionAnalyzerApp() {
        initialize();
    }

    private void initialize() {
        setTitle("거래내역 분석 서비스");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout());

        dragPanel = new JPanel();
        dragPanel.setPreferredSize(new Dimension(200, 600)); // 드래그 영역 크기
        dragPanel.setBorder(BorderFactory.createTitledBorder("파일 드래그"));
        dragPanel.setLayout(new BorderLayout());
        getContentPane().add(dragPanel, BorderLayout.WEST);

        // 드래그 앤 드롭 설정
        dragPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean importData(TransferSupport support) {
                if (canImport(support)) {
                    try {
                        List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                        for (File file : files) {
                            handleFile(file);
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }
        });

        // 드래그 패널에 마우스 드래그 시 커서 변경
        dragPanel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                try {
                    List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    for (File file : files) {
                        handleFile(file);
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        // OCR 결과 영역
        ocrResultArea = new JTextArea();
        ocrResultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(ocrResultArea);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // 패널 설정
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT)); // 왼쪽 정렬

        // 시작 키워드 입력
        startKeywordField = new JTextField(10); // 크기 조정
        panel.add(new JLabel("시작 키워드:"));
        panel.add(startKeywordField);

        // 거래금액 범위 선택
        priceRangeComboBox = new JComboBox<>(new String[]{
            "300,000원 ~ 500,000원", "500,000원 ~ 1,000,000원", "1,000,000원 이상"
        });
        panel.add(new JLabel("거래금액 범위 선택:"));
        panel.add(priceRangeComboBox);

        // 파일 업로드 버튼
        uploadButton = new JButton("파일 업로드");
        uploadButton.setPreferredSize(new Dimension(120, 30)); // 크기 조정
        panel.add(uploadButton);

        getContentPane().add(panel, BorderLayout.NORTH);

        // 버튼 패널 설정 (오른쪽 정렬)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // 오른쪽 정렬

        // Clear 버튼
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> ocrResultArea.setText(""));
        buttonPanel.add(clearButton);

        // Copy 버튼
        copyButton = new JButton("Copy");
        copyButton.addActionListener(e -> copyToClipboard(ocrResultArea.getText()));
        buttonPanel.add(copyButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // 버튼 액션 리스너
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(TransactionAnalyzerApp.this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    try {
                        handleFile(selectedFile);
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private void handleFile(File file) throws IOException {
        // 시작 키워드 검증
        String startKeyword = startKeywordField.getText().trim();
        if (startKeyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "시작 키워드를 입력해주세요.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 파일 타입 검증
        if (!file.getName().endsWith(".pdf")) {
            JOptionPane.showMessageDialog(this, "PDF 파일만 업로드 가능합니다.", "경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClovaOcrService clovaOcrService = new ClovaOcrService();
        StringBuffer result = clovaOcrService.analyzePdf(file);

        String ocrResult = OcrMappingService.ocrMapping(result.toString(), startKeyword);
        ocrResultArea.append(ocrResult);

        OcrMappingService.ocrToExcel(ocrResult, file.getName());
    }

    private void copyToClipboard(String text) {
        StringSelection stringSelection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        JOptionPane.showMessageDialog(this, "복사되었습니다: " + text, "복사 완료", JOptionPane.INFORMATION_MESSAGE);
    }
}