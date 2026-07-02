package com.vitalid.services;

import com.vitalid.dtos.ai.RagAskRequest;
import com.vitalid.dtos.ai.RagAskResponse;
import com.vitalid.dtos.ai.RagIngestResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private final String apiKey;
    private final String chatModel;
    private List<KnowledgeChunk> knowledgeChunks = List.of();

    public AiRagService(@Value("${app.ai.gemini.api-key}") String apiKey,
                        @Value("${app.ai.gemini.chat-model:gemini-1.5-flash}") String chatModel) {
        this.restTemplate = new RestTemplate();
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gemini API key is not configured");
        }
        if (knowledgeChunks.isEmpty()) {
            ingestMedicalKnowledge();
        }

        List<KnowledgeChunk> relevantChunks = findRelevantChunks(question);
        if (relevantChunks.isEmpty()) {
            return new RagAskResponse(
                    "No encontre informacion suficiente en la base medica cargada. Consulta con un profesional de salud si tienes sintomas o dudas sobre tu tratamiento.",
                    false,
                    List.of()
            );
        }

        String context = relevantChunks.stream()
                .map(KnowledgeChunk::text)
                .collect(Collectors.joining("\n\n---\n\n"));

        String reply = callGemini(context, question);
        return new RagAskResponse(reply, true, List.of(KNOWLEDGE_FILE));
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
                .limit(4)
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

    private String callGemini(String context, String question) {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + chatModel
                + ":generateContent?key="
                + apiKey;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of(
                                "text", SYSTEM_PROMPT
                                        + "\n\nContexto medico:\n"
                                        + context
                                        + "\n\nPregunta del usuario:\n"
                                        + question
                        ))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 700
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, Map.class);
            return extractText(response.getBody());
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Gemini request failed",
                    exception
            );
        }
    }

    private String extractText(Map<?, ?> body) {
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned an empty response");
        }

        Object candidatesValue = body.get("candidates");
        if (!(candidatesValue instanceof List<?> candidates) || candidates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini returned no candidates");
        }

        Object firstCandidate = candidates.get(0);
        if (!(firstCandidate instanceof Map<?, ?> candidate)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini response format is invalid");
        }

        Object contentValue = candidate.get("content");
        if (!(contentValue instanceof Map<?, ?> content)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini response content is invalid");
        }

        Object partsValue = content.get("parts");
        if (!(partsValue instanceof List<?> parts) || parts.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini response parts are empty");
        }

        Object firstPart = parts.get(0);
        if (!(firstPart instanceof Map<?, ?> part)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini response part is invalid");
        }

        Object text = part.get("text");
        if (text == null || text.toString().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Gemini response text is empty");
        }
        return text.toString();
    }

    private boolean isMissingApiKey() {
        return apiKey == null || apiKey.isBlank();
    }

    private record KnowledgeChunk(String text, Set<String> tokens) {
    }

    private record ScoredChunk(KnowledgeChunk chunk, int score) {
    }
}
