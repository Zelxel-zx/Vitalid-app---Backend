package com.vitalid.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitalid.dtos.ai.AiAppointmentAction;
import com.vitalid.dtos.ai.RagAskRequest;
import com.vitalid.dtos.ai.RagAskResponse;
import com.vitalid.dtos.ai.RagIngestResponse;
import com.vitalid.dtos.appointment.AppointmentRequest;
import com.vitalid.dtos.appointment.AppointmentResponse;
import com.vitalid.models.AppointmentType;
import com.vitalid.models.Doctor;
import com.vitalid.models.Patient;
import com.vitalid.repositories.AppointmentRepository;
import com.vitalid.repositories.DoctorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiRagService {

    private static final String KNOWLEDGE_FILE = "vitalid_medical_knowledge.txt";
    private static final String APPOINTMENT_ACTION_TYPE = "SCHEDULE_APPOINTMENT";
    private static final Pattern SECTION_SPLITTER = Pattern.compile("\\n(?=[A-ZÁÉÍÓÚÑ][^\\n]{2,80}\\n)");
    private static final Pattern WORD_SPLITTER = Pattern.compile("[^a-záéíóúñ0-9]+");

    private static final String SYSTEM_PROMPT = """
            Eres el asistente medico informativo de Vitalid.
            Responde solo con base en el contexto proporcionado.
            No diagnostiques, no recetes, no indiques dosis y no cambies tratamientos.
            Si hay senales de alarma, recomienda atencion de emergencia.
            Si el contexto no contiene informacion suficiente, dilo claramente y recomienda consultar a un profesional de salud.
            """;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final ResourceAccessService resourceAccessService;
    private final String apiKey;
    private final String chatModel;
    private List<KnowledgeChunk> knowledgeChunks = List.of();

    public AiRagService(@Value("${app.ai.groq.api-key}") String apiKey,
                        @Value("${app.ai.groq.chat-model:llama-3.1-8b-instant}") String chatModel,
                        ObjectMapper objectMapper,
                        DoctorRepository doctorRepository,
                        AppointmentRepository appointmentRepository,
                        AppointmentService appointmentService,
                        ResourceAccessService resourceAccessService) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = objectMapper;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
        this.resourceAccessService = resourceAccessService;
        this.apiKey = apiKey;
        this.chatModel = chatModel;
    }

    public RagIngestResponse ingestMedicalKnowledge() {
        try {
            this.knowledgeChunks = loadKnowledgeChunks();
            return new RagIngestResponse("Medical knowledge ingested", knowledgeChunks.size());
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not read RAG knowledge file",
                    exception
            );
        }
    }

    public RagAskResponse ask(RagAskRequest request) {
        String question = request == null ? null : request.message();
        if (question == null || question.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message is required");
        }
        if (isMissingApiKey()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Groq API key is not configured");
        }
        AiAppointmentAction pendingAction = request == null ? null : request.pendingAction();
        if (looksLikeDoctorUpcomingAppointmentsRequest(question)) {
            return handleDoctorUpcomingAppointmentsRequest();
        }
        if (pendingAction != null || looksLikeAppointmentRequest(question)) {
            return handleAppointmentRequest(question, pendingAction);
        }
        if (knowledgeChunks.isEmpty()) {
            ingestMedicalKnowledge();
        }

        List<KnowledgeChunk> relevantChunks = findRelevantChunks(question);
        if (relevantChunks.isEmpty()) {
            return new RagAskResponse(
                    "No encontre informacion suficiente en la base medica cargada. Consulta con un profesional de salud si tienes sintomas o dudas sobre tu tratamiento.",
                    false,
                    List.of(),
                    null
            );
        }

        String context = relevantChunks.stream()
                .map(KnowledgeChunk::text)
                .collect(Collectors.joining("\n\n---\n\n"));

        String reply = callGroq(context, question);
        return new RagAskResponse(reply, true, List.of(KNOWLEDGE_FILE), null);
    }

    public AppointmentResponse confirmAppointment(AiAppointmentAction action) {
        if (action == null || !APPOINTMENT_ACTION_TYPE.equals(action.type())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment action is required");
        }

        Patient patient = resourceAccessService.currentPatient();
        LocalDate date = parseDate(action.date());
        LocalTime time = parseTime(action.time());
        AppointmentType appointmentType = parseAppointmentType(action.appointmentType());

        if (action.doctorId() == null || date == null || time == null || appointmentType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment action is incomplete");
        }

        AppointmentRequest request = new AppointmentRequest();
        request.setPatientId(patient.getId());
        request.setDoctorId(action.doctorId());
        request.setDate(date);
        request.setTime(time);
        request.setReason(action.reason() == null || action.reason().isBlank()
                ? "Consulta medica"
                : action.reason());
        request.setAppointmentType(appointmentType);
        return appointmentService.createAppointment(request);
    }

    private RagAskResponse handleAppointmentRequest(String message, AiAppointmentAction pendingAction) {
        if (!resourceAccessService.isAuthenticated()) {
            return new RagAskResponse(
                    "Para agendar una cita necesito que inicies sesion como paciente.",
                    false,
                    List.of(),
                    null
            );
        }
        resourceAccessService.currentPatient();

        AppointmentExtraction extraction = extractAppointmentAction(message, pendingAction);
        if (!APPOINTMENT_ACTION_TYPE.equals(extraction.intent())) {
            return new RagAskResponse(
                    "Puedo ayudarte a agendar una cita. Indica doctor o especialidad, fecha, hora y si sera presencial o por videollamada.",
                    false,
                    List.of(),
                    null
            );
        }

        AiAppointmentAction action = buildAppointmentAction(extraction);
        return new RagAskResponse(buildAppointmentReply(action), false, List.of(), action);
    }

    private RagAskResponse handleDoctorUpcomingAppointmentsRequest() {
        if (!resourceAccessService.isAuthenticated()) {
            return new RagAskResponse(
                    "Para consultar tus proximas citas necesito que inicies sesion como doctor.",
                    false,
                    List.of(),
                    null
            );
        }

        Doctor doctor = resourceAccessService.currentDoctor();
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctor.getId())
                .stream()
                .filter(this::isUpcomingAppointment)
                .limit(5)
                .toList();

        if (appointments.isEmpty()) {
            return new RagAskResponse(
                    "No tienes citas proximas registradas.",
                    false,
                    List.of(),
                    null
            );
        }

        String appointmentsText = appointments.stream()
                .map(this::formatDoctorAppointment)
                .collect(Collectors.joining("\n"));

        return new RagAskResponse(
                "Tus proximas citas son:\n" + appointmentsText,
                false,
                List.of(),
                null
        );
    }

    private AppointmentExtraction extractAppointmentAction(String message, AiAppointmentAction pendingAction) {
        String doctorsContext = doctorRepository.findAllByOrderByIdAsc().stream()
                .map(doctor -> Map.of(
                        "id", doctor.getId(),
                        "name", doctor.getUser() != null ? doctor.getUser().getName() : "",
                        "specialty", doctor.getSpecialty() == null ? "" : doctor.getSpecialty(),
                        "availabilityStart", doctor.getAvailabilityStart() == null ? "" : doctor.getAvailabilityStart().toString(),
                        "availabilityEnd", doctor.getAvailabilityEnd() == null ? "" : doctor.getAvailabilityEnd().toString()
                ))
                .toList()
                .toString();

        String prompt = """
                Eres un extractor de acciones para Vitalid. Devuelve solo JSON valido, sin markdown.
                Fecha actual: %s.
                Doctores disponibles: %s
                Datos ya capturados de la cita, si existen: %s
                Si el usuario quiere agendar o reservar una cita, devuelve:
                {
                  "intent": "SCHEDULE_APPOINTMENT",
                  "doctorId": number|null,
                  "doctorName": string|null,
                  "specialty": string|null,
                  "date": "yyyy-MM-dd"|null,
                  "time": "HH:mm"|null,
                  "appointmentType": "IN_PERSON"|"VIDEO_CALL"|null,
                  "reason": string|null,
                  "missing": ["doctorId","date","time","appointmentType"],
                  "reply": string|null
                }
                Si no quiere agendar cita, devuelve {"intent":"OTHER"}.
                Usa solo ids de la lista. Si menciona una especialidad y hay un solo doctor de esa especialidad, usa su id.
                Si el usuario solo completa un dato faltante, conserva los datos ya capturados.
                Si dice hoy, manana o pasado manana, calcula la fecha con la fecha actual.
                """.formatted(LocalDate.now(), doctorsContext, pendingAction == null ? "ninguno" : pendingAction);

        Map<String, Object> requestBody = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role", "system", "content", prompt),
                        Map.of("role", "user", "content", message)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0,
                "max_tokens", 500
        );

        String content = callGroqRaw(requestBody);
        try {
            return objectMapper.readValue(extractJsonObject(content), AppointmentExtraction.class);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Groq appointment action format is invalid: " + summarizeProviderText(content),
                    exception);
        }
    }

    private AiAppointmentAction buildAppointmentAction(AppointmentExtraction extraction) {
        Doctor doctor = resolveDoctor(extraction);
        LocalDate date = parseDate(extraction.date());
        LocalTime time = parseTime(extraction.time());
        AppointmentType appointmentType = parseAppointmentType(extraction.appointmentType());

        List<String> missing = new ArrayList<>();
        if (doctor == null) {
            missing.add("doctorId");
        }
        if (date == null) {
            missing.add("date");
        }
        if (time == null) {
            missing.add("time");
        }
        if (appointmentType == null) {
            missing.add("appointmentType");
        }

        List<String> availableSlots = List.of();
        if (doctor != null && date != null) {
            availableSlots = availableSlots(doctor, date);
            if (time != null && !availableSlots.contains(time.toString())) {
                missing.add("time");
                time = null;
            }
        }

        return new AiAppointmentAction(
                APPOINTMENT_ACTION_TYPE,
                doctor != null ? doctor.getId() : null,
                doctor != null && doctor.getUser() != null ? doctor.getUser().getName() : extraction.doctorName(),
                doctor != null ? doctor.getSpecialty() : extraction.specialty(),
                date != null ? date.toString() : null,
                time != null ? time.toString() : null,
                appointmentType != null ? appointmentType.name() : null,
                extraction.reason() == null || extraction.reason().isBlank() ? "Consulta medica" : extraction.reason(),
                missing.stream().distinct().toList(),
                availableSlots
        );
    }

    private Doctor resolveDoctor(AppointmentExtraction extraction) {
        if (extraction.doctorId() != null) {
            return doctorRepository.findById(extraction.doctorId()).orElse(null);
        }

        if (extraction.specialty() != null && !extraction.specialty().isBlank()) {
            List<Doctor> matches = doctorRepository.findBySpecialtyIgnoreCase(extraction.specialty());
            if (matches.size() == 1) {
                return matches.get(0);
            }
        }

        String doctorName = normalize(extraction.doctorName());
        if (!doctorName.isBlank()) {
            List<Doctor> matches = doctorRepository.findAllByOrderByIdAsc().stream()
                    .filter(doctor -> doctor.getUser() != null
                            && normalize(doctor.getUser().getName()).contains(doctorName))
                    .toList();
            if (matches.size() == 1) {
                return matches.get(0);
            }
        }
        return null;
    }

    private String buildAppointmentReply(AiAppointmentAction action) {
        if (!action.missing().isEmpty()) {
            String missing = action.missing().stream()
                    .map(this::missingLabel)
                    .collect(Collectors.joining(", "));
            String slots = action.availableSlots().isEmpty()
                    ? ""
                    : " Horarios disponibles para esa fecha: " + String.join(", ", action.availableSlots()) + ".";
            return "Puedo agendar tu cita, pero falta: " + missing + "." + slots;
        }

        return "Tengo lista la cita con "
                + action.doctorName()
                + " para el "
                + action.date()
                + " a las "
                + action.time()
                + " ("
                + ("VIDEO_CALL".equals(action.appointmentType()) ? "videollamada" : "presencial")
                + "). Responde \"confirmo\" para crearla.";
    }

    private String missingLabel(String missing) {
        return switch (missing) {
            case "doctorId" -> "doctor o especialidad";
            case "date" -> "fecha";
            case "time" -> "hora";
            case "appointmentType" -> "tipo de consulta";
            default -> missing;
        };
    }

    private List<String> availableSlots(Doctor doctor, LocalDate date) {
        LocalTime start = doctor.getAvailabilityStart();
        LocalTime end = doctor.getAvailabilityEnd();
        if (start == null || end == null || date.isBefore(LocalDate.now())) {
            return List.of();
        }

        Set<LocalTime> bookedTimes = appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date)
                .stream()
                .filter(appointment -> !"CANCELLED".equalsIgnoreCase(appointment.getStatus()))
                .map(appointment -> appointment.getTime())
                .collect(Collectors.toSet());

        List<String> slots = new ArrayList<>();
        LocalTime current = start;
        while (current.isBefore(end)) {
            boolean futureSlot = !date.equals(LocalDate.now()) || current.isAfter(LocalTime.now());
            if (futureSlot && !bookedTimes.contains(current)) {
                slots.add(current.toString());
            }
            current = current.plusMinutes(30);
        }
        return slots;
    }

    private List<KnowledgeChunk> loadKnowledgeChunks() throws IOException {
        ClassPathResource resource = new ClassPathResource(KNOWLEDGE_FILE);
        String content = resource.getContentAsString(StandardCharsets.UTF_8);
        String[] sections = SECTION_SPLITTER.split(content);

        List<KnowledgeChunk> chunks = new ArrayList<>();
        for (String section : sections) {
            String normalized = section.trim();
            if (!normalized.isBlank()) {
                chunks.add(new KnowledgeChunk(normalized, tokenize(normalized)));
            }
        }
        return chunks;
    }

    private List<KnowledgeChunk> findRelevantChunks(String question) {
        Set<String> questionTokens = tokenize(question);
        return knowledgeChunks.stream()
                .map(chunk -> new ScoredChunk(chunk, score(questionTokens, chunk.tokens())))
                .filter(scored -> scored.score() > 0)
                .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                .limit(3)
                .map(ScoredChunk::chunk)
                .toList();
    }

    private int score(Set<String> questionTokens, Set<String> chunkTokens) {
        int score = 0;
        for (String token : questionTokens) {
            if (chunkTokens.contains(token)) {
                score++;
            }
        }
        return score;
    }

    private Set<String> tokenize(String text) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String rawToken : WORD_SPLITTER.split(text.toLowerCase(Locale.ROOT))) {
            if (rawToken.length() >= 4) {
                tokens.add(rawToken);
            }
        }
        return tokens;
    }

    private boolean looksLikeAppointmentRequest(String message) {
        String normalized = normalize(message);
        return Pattern.compile("\\b(agendar|agenda|reservar|reserva|programar|sacar)\\b").matcher(normalized).find()
                && Pattern.compile("\\b(cita|consulta|turno)\\b").matcher(normalized).find();
    }

    private boolean looksLikeDoctorUpcomingAppointmentsRequest(String message) {
        String normalized = normalize(message);
        return Pattern.compile("\\b(proximas|siguientes|pendientes|agenda|programadas)\\b").matcher(normalized).find()
                && Pattern.compile("\\b(citas|consultas|turnos)\\b").matcher(normalized).find();
    }

    private boolean isUpcomingAppointment(AppointmentResponse appointment) {
        if (appointment == null
                || appointment.getDate() == null
                || appointment.getTime() == null
                || "CANCELLED".equalsIgnoreCase(appointment.getStatus())
                || "COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            return false;
        }
        return appointment.getDate().isAfter(LocalDate.now())
                || (appointment.getDate().isEqual(LocalDate.now())
                && !appointment.getTime().isBefore(LocalTime.now()));
    }

    private String formatDoctorAppointment(AppointmentResponse appointment) {
        String type = appointment.getAppointmentType() == AppointmentType.VIDEO_CALL
                ? "videollamada"
                : "presencial";
        String reason = appointment.getReason() == null || appointment.getReason().isBlank()
                ? "sin motivo registrado"
                : appointment.getReason();
        return "- " + appointment.getDate()
                + " a las " + appointment.getTime()
                + " con " + appointment.getPatientName()
                + " (" + type + ", " + reason + ")";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(value);
            return date.isBefore(LocalDate.now()) ? null : date;
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private LocalTime parseTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            LocalTime time = LocalTime.parse(value);
            return time.getSecond() == 0 ? time : null;
        } catch (DateTimeParseException exception) {
            return null;
        }
    }

    private AppointmentType parseAppointmentType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return AppointmentType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String callGroq(String context, String question) {
        Map<String, Object> requestBody = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of(
                                "role",
                                "user",
                                "content",
                                "Contexto medico:\n" + context + "\n\nPregunta del usuario:\n" + question
                        )
                ),
                "temperature", 0.2,
                "max_tokens", 700
        );
        return callGroqRaw(requestBody);
    }

    private String callGroqRaw(Map<String, Object> requestBody) {
        String endpoint = "https://api.groq.com/openai/v1/chat/completions";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, Map.class);
            return extractText(response.getBody());
        } catch (HttpStatusCodeException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Groq request failed: "
                            + exception.getStatusCode().value()
                            + " "
                            + sanitizeProviderError(exception.getResponseBodyAsString()),
                    exception
            );
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groq request failed", exception);
        }
    }

    private String extractText(Map<?, ?> body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groq returned an empty response");
        }

        Object choicesValue = body.get("choices");
        if (!(choicesValue instanceof List<?> choices) || choices.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groq returned no choices");
        }

        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choice)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groq response format is invalid");
        }

        Object messageValue = choice.get("message");
        if (!(messageValue instanceof Map<?, ?> message)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groq response message is invalid");
        }

        Object content = message.get("content");
        if (content == null || content.toString().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groq response content is empty");
        }
        return content.toString();
    }

    private String extractJsonObject(String content) {
        if (content == null || content.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Groq did not return JSON");
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed
                    .replaceFirst("^```(?:json)?\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return trimmed;
        }

        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Groq did not return JSON: " + summarizeProviderText(content));
        }
        return content.substring(start, end + 1);
    }

    private String summarizeProviderText(String content) {
        if (content == null || content.isBlank()) {
            return "empty response";
        }
        String compact = content.replaceAll("\\s+", " ").trim();
        return compact.length() > 180 ? compact.substring(0, 180) + "..." : compact;
    }

    private String sanitizeProviderError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "empty error body";
        }
        String compact = responseBody
                .replaceAll("\\s+", " ")
                .replace(apiKey, "[redacted]");
        return compact.length() > 450 ? compact.substring(0, 450) + "..." : compact;
    }

    private boolean isMissingApiKey() {
        return apiKey == null || apiKey.isBlank();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("\\b(dr|dra|doctor|doctora)\\b", "")
                .trim();
    }

    private record KnowledgeChunk(String text, Set<String> tokens) {
    }

    private record ScoredChunk(KnowledgeChunk chunk, int score) {
    }

    private record AppointmentExtraction(
            String intent,
            Long doctorId,
            String doctorName,
            String specialty,
            String date,
            String time,
            String appointmentType,
            String reason,
            List<String> missing,
            String reply
    ) {
    }
}
