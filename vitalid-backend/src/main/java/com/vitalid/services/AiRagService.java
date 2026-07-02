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
import org.springframework.web.client.HttpStatusCodeException;
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

    public AiRagService(@Value("${app.ai.groq.api-key}") String apiKey,
                        @Value("${app.ai.groq.chat-model:llama-3.1-8b-instant}") String chatModel) {
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Groq API key is not configured");
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

        String reply = callGroq(context, question);
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

    private String callGroq(String context, String question) {
        String endpoint = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", chatModel,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", SYSTEM_PROMPT
                        ),
                        Map.of(
                                "role", "user",
                                "content", "Contexto medico:\n"
                                        + context
                                        + "\n\nPregunta del usuario:\n"
                                        + question
                        )
                ),
                "temperature", 0.2,
                "max_tokens", 700
        );

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
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Groq request failed",
                    exception
            );
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

    private record KnowledgeChunk(String text, Set<String> tokens) {
    }

    private record ScoredChunk(KnowledgeChunk chunk, int score) {
    }
}
