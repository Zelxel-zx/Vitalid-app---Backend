package com.vitalid.controllers;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.vitalid.models.Patient;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.repositories.TreatmentRepository;
import com.vitalid.repositories.UserRepository;
import com.vitalid.services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired private UserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private TreatmentRepository treatmentRepository;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Existing PDFBox-based report by patientId */
    @GetMapping("/patient/{patientId}/pdf")
    public ResponseEntity<byte[]> downloadPatientReport(@PathVariable Long patientId) {
        byte[] pdf = reportService.buildPatientReport(patientId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=vitalid-report-" + patientId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /** New iText 7-based medical history report by userId */
    @GetMapping("/user/{userId}/pdf")
    public ResponseEntity<byte[]> generatePatientReport(@PathVariable Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var patientOpt = patientRepository.findByUser_Id(userId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document doc = new Document(pdfDoc)) {

            // Title header
            Paragraph title = new Paragraph("Historial Médico - Vitalid")
                    .setFontSize(20).setBold()
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            doc.add(title);

            doc.add(new Paragraph("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

            doc.add(new Paragraph("\n"));

            // Patient info section
            doc.add(new Paragraph("Datos del Paciente").setFontSize(14).setBold());
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60})).useAllAvailableWidth();
            addRow(infoTable, "Nombre", user.getName());
            addRow(infoTable, "Email", user.getEmail());
            addRow(infoTable, "Teléfono", user.getPhone() != null ? user.getPhone() : "—");

            if (patientOpt.isPresent()) {
                Patient p = patientOpt.get();
                addRow(infoTable, "Fecha de Nacimiento",
                        p.getDateOfBirth() != null ? p.getDateOfBirth().toString() : "—");
                addRow(infoTable, "Grupo Sanguíneo",
                        p.getBloodType() != null ? p.getBloodType() : "—");
                addRow(infoTable, "Alergias",
                        p.getAllergies() != null ? p.getAllergies() : "Ninguna");
                addRow(infoTable, "Historial Médico",
                        p.getMedicalHistory() != null ? p.getMedicalHistory() : "—");
            }
            doc.add(infoTable);

            // Treatments section
            if (patientOpt.isPresent()) {
                var treatments = treatmentRepository.findByPatientId(patientOpt.get().getId());
                doc.add(new Paragraph("\n"));
                doc.add(new Paragraph("Tratamientos").setFontSize(14).setBold());

                if (treatments.isEmpty()) {
                    doc.add(new Paragraph("Sin tratamientos registrados.").setFontSize(10));
                } else {
                    Table tTable = new Table(UnitValue.createPercentArray(new float[]{30, 20, 15, 35}))
                            .useAllAvailableWidth();
                    tTable.addHeaderCell(new Cell().add(new Paragraph("Tratamiento").setBold()));
                    tTable.addHeaderCell(new Cell().add(new Paragraph("Estado").setBold()));
                    tTable.addHeaderCell(new Cell().add(new Paragraph("Progreso").setBold()));
                    tTable.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold()));

                    for (var t : treatments) {
                        tTable.addCell(t.getTitle() != null ? t.getTitle() : "—");
                        tTable.addCell(t.getStatus() != null ? t.getStatus() : "—");
                        tTable.addCell((t.getProgress() != null ? t.getProgress() : 0) + "%");
                        tTable.addCell(t.getDescription() != null ? t.getDescription() : "—");
                    }
                    doc.add(tTable);
                }
            }

            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Generado por Vitalid - Plataforma de Telemedicina")
                    .setFontSize(8)
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER));

        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Error generating PDF: " + e.getMessage());
        }

        byte[] pdf = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "historial-vitalid-" + userId + ".pdf");
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold().setFontSize(10)));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "—").setFontSize(10)));
    }
}
