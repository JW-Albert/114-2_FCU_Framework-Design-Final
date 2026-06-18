package com.vehicle.management.api.controller;

import com.vehicle.management.domain.model.BorrowingRequest;
import com.vehicle.management.domain.model.Vehicle;
import com.vehicle.management.repository.IBorrowingRepository;
import com.vehicle.management.repository.IVehicleRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final IBorrowingRepository borrowingRepository;
    private final IVehicleRepository vehicleRepository;

    public ReportController(IBorrowingRepository borrowingRepository,
                            IVehicleRepository vehicleRepository) {
        this.borrowingRepository = borrowingRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/borrowings")
    public ResponseEntity<byte[]> exportBorrowings(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        List<BorrowingRequest> requests = borrowingRepository.findAll();

        // Filter by date range on createdAt if provided
        if (from != null) {
            requests = requests.stream()
                    .filter(r -> !r.getCreatedAt().isBefore(from))
                    .collect(Collectors.toList());
        }
        if (to != null) {
            requests = requests.stream()
                    .filter(r -> !r.getCreatedAt().isAfter(to))
                    .collect(Collectors.toList());
        }

        // Build vehicle lookup map
        Map<UUID, Vehicle> vehicleMap = vehicleRepository.findAll().stream()
                .collect(Collectors.toMap(Vehicle::getId, v -> v));

        byte[] excel = buildExcel(requests, vehicleMap);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "borrowings_export.xlsx");

        return ResponseEntity.ok().headers(headers).body(excel);
    }

    private byte[] buildExcel(List<BorrowingRequest> requests, Map<UUID, Vehicle> vehicleMap)
            throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("借用記錄");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = wb.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);

            // Header row
            String[] headers = {"編號", "申請人ID", "車牌", "車型", "借用開始", "借用結束", "用途", "狀態", "審核備註", "建立時間"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.of("Asia/Taipei"));
            int rowNum = 1;
            for (BorrowingRequest req : requests) {
                Row row = sheet.createRow(rowNum++);
                Vehicle v = vehicleMap.get(req.getVehicleId());
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue(req.getUserId().toString());
                row.createCell(2).setCellValue(v != null ? v.getPlate() : "");
                row.createCell(3).setCellValue(v != null ? v.getModel() : "");
                row.createCell(4).setCellValue(fmt.format(req.getPeriodStart()));
                row.createCell(5).setCellValue(fmt.format(req.getPeriodEnd()));
                row.createCell(6).setCellValue(req.getPurpose());
                row.createCell(7).setCellValue(translateState(req.getStateName()));
                row.createCell(8).setCellValue(req.getReviewNote() != null ? req.getReviewNote() : "");
                row.createCell(9).setCellValue(fmt.format(req.getCreatedAt()));
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        }
    }

    private String translateState(String state) {
        return switch (state) {
            case "PENDING" -> "待審核";
            case "APPROVED" -> "已核准";
            case "REJECTED" -> "已拒絕";
            case "IN_USE" -> "使用中";
            case "RETURNED" -> "已還車";
            default -> state;
        };
    }
}
