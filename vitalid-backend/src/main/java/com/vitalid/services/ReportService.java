package com.vitalid.services;

import com.vitalid.repositories.AppointmentRepository;
import com.vitalid.repositories.MedicationRepository;
import com.vitalid.models.Patient;
import com.vitalid.repositories.PatientRepository;
import com.vitalid.repositories.TreatmentRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class ReportService {
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TreatmentRepository treatmentRepository;
    private final MedicationRepository medicationRepository;

    public ReportService(
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            TreatmentRepository treatmentRepository,
            MedicationRepository medicationRepository
    ) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.treatmentRepository = treatmentRepository;
        this.medicationRepository = medicationRepository;
    }

    public byte[] buildPatientReportForUser(Long userId) {
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));
        return buildPatientReport(patient.getId());
    }

    public byte[] buildPatientReport(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        var appointments = appointmentRepository.findByPatientId(patientId);
        var treatments = treatmentRepository.findByPatientId(patientId);
        var medications = medicationRepository.findByPatientId(patientId);

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);
            float y = 740;

            y = writeLine(content, 50, y, PDType1Font.HELVETICA_BOLD, 16, "Vitalid Medical Report");
            y = writeLine(content, 50, y, PDType1Font.HELVETICA, 12, "Date: " + LocalDate.now());
            y = writeLine(content, 50, y, PDType1Font.HELVETICA, 12, "Patient: " + patient.getUser().getName());
            y = writeLine(content, 50, y, PDType1Font.HELVETICA, 12, "Email: " + patient.getUser().getEmail());

            y = writeLine(content, 50, y - 10, PDType1Font.HELVETICA_BOLD, 13, "Appointments:");
            if (appointments.isEmpty()) {
                y = writeLine(content, 70, y, PDType1Font.HELVETICA, 12, "No appointments.");
            } else {
                for (var appointment : appointments) {
                    String line = "- " + appointment.getDate() + " " + appointment.getTime() +
                            " with Dr. " + appointment.getDoctor().getUser().getName() +
                            " (" + appointment.getStatus() + ")";
                    y = writeLine(content, 70, y, PDType1Font.HELVETICA, 11, line);
                }
            }

            y = writeLine(content, 50, y - 10, PDType1Font.HELVETICA_BOLD, 13, "Treatments:");
            if (treatments.isEmpty()) {
                y = writeLine(content, 70, y, PDType1Font.HELVETICA, 12, "No treatments.");
            } else {
                for (var treatment : treatments) {
                    String line = "- " + treatment.getTitle() + " (" + treatment.getStatus() +
                            ", " + treatment.getProgress() + "% )";
                    y = writeLine(content, 70, y, PDType1Font.HELVETICA, 11, line);
                }
            }

            y = writeLine(content, 50, y - 10, PDType1Font.HELVETICA_BOLD, 13, "Medications:");
            if (medications.isEmpty()) {
                y = writeLine(content, 70, y, PDType1Font.HELVETICA, 12, "No medications.");
            } else {
                for (var medication : medications) {
                    String line = "- " + medication.getName() + " " + medication.getDosage() +
                            " (" + medication.getFrequency() + ")";
                    y = writeLine(content, 70, y, PDType1Font.HELVETICA, 11, line);
                }
            }

            content.close();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate PDF");
        }
    }

    private float writeLine(PDPageContentStream content, float x, float y, PDType1Font font, int size, String text) throws Exception {
        content.beginText();
        content.setFont(font, size);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
        return y - 16;
    }
}


