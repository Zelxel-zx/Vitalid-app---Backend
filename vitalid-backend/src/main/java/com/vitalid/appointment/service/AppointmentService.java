package com.vitalid.appointment.service;

import org.springframework.stereotype.Service;
import com.vitalid.appointment.dto.AppointmentRequest;
import com.vitalid.appointment.dto.AppointmentResponse;
import com.vitalid.appointment.dto.AppointmentListResponse;
import java.util.List;

/**
 * Appointment Service
 * Handles appointment scheduling and management
 * 
 * TODO: Implement methods:
 * - getAllAppointments(int page, int size) -> AppointmentListResponse
 * - scheduleAppointment(AppointmentRequest) -> AppointmentResponse
 * - cancelAppointment(String id) -> void
 * - getDoctorAppointments(Integer doctorId) -> List<AppointmentResponse>
 * - getPatientAppointments(String patientId, int page, int size) -> AppointmentListResponse
 * - updateAppointment(String id, AppointmentRequest) -> AppointmentResponse
 */
@Service
public class AppointmentService {

    // TODO: Implement appointment business logic

}
