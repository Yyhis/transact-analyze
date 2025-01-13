package net.yyhis.transact_analyze.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.yyhis.transact_analyze.util.PriceRange;

import java.nio.file.Files;
import java.util.UUID;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class ClovaOcrService {

    // @Value("${clova.ocr.api.secret}")
    private static String secretKey="dGZIUlRwY0pNc0dTcWFPSE9hamVqRndJeXZ2cHRxRlc=";

    // @Value("${clova.ocr.api.url}")
    private String apiURL="https://0e6ryat0i0.apigw.ntruss.com/custom/v1/34340/86d895995d846e7cc775e0b649e415222de5ffd8ac365d08111fb4e6d38292db/general";

    @Autowired
    PriceRange listFillter;

    @Autowired
    OcrMappingService ocrMappingService;

    public StringBuffer analyzePdf(File pdfFile) throws IOException {

        byte[] buffer = Files.readAllBytes(pdfFile.toPath());

        System.out.println("buffer: " + buffer);

        try {
            URL url = new URL(apiURL);
            System.out.println("url: " + url);
            HttpURLConnection connection = createRequestHeader(url);
            JSONObject requestBody = createRequestBody(buffer);

            StringBuffer response = createRequest(connection, requestBody);

            return response;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    private static HttpURLConnection createRequestHeader(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        connection.setReadTimeout(50000);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connection.setRequestProperty("X-OCR-SECRET", secretKey);
        return connection;
    }

    private static JSONObject createRequestBody(byte[] requestFile)
            throws IOException {
        JSONObject file = new JSONObject();
        // 나중에 파일폼이 pdf이면 pdf, 이미지이면 jpg, jpeg, png 등등
        file.put("format", "pdf");
        file.put("data", requestFile);
        file.put("name", "demo");

        JSONArray files = new JSONArray();
        files.put(file);

        JSONObject requestObject = new JSONObject();
        requestObject.put("version", "V2");
        requestObject.put("requestId", UUID.randomUUID().toString());
        requestObject.put("timestamp", System.currentTimeMillis());
        requestObject.put("enableTableDetection", true);

        requestObject.put("images", files);

        return requestObject;
    }

    private static StringBuffer createRequest(HttpURLConnection connection, JSONObject requestObject)
            throws IOException {
        connection.connect();

        String postParams = requestObject.toString();

        // JSON 데이터를 OutputStream에 작성
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postParams.getBytes("UTF-8");
            os.write(input, 0, input.length);
            os.flush();
        }

        int responseCode = connection.getResponseCode();
        BufferedReader br;
        if (responseCode == 200) {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        } else {
            br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
        }
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();

        return response;
    }
}