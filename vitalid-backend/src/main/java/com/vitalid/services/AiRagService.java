package com.vitalid.services;

import com.vitalid.dtos.ai.RagAskRequest;
import com.vitalid.dtos.ai.RagAskResponse;
import com.vitalid.dtos.ai.RagIngestResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AiRagService {

    private static final String KNOWLEDGE_FILE = "vitalid_medical_knowledge.txt";

    private static final String SYSTEM_PROMPT = """
            Eres el asistente medico informativo de Vitalid.
            Responde solo con base en el contexto proporcionado.
            No diagnostiques, no recetes, no indiques dosis y no cambies tratamientos.
            Si hay senales de alarma, recomienda atencion de emergencia.
            Si el contexto no contiene informacion suficiente, dilo claramente y recomienda consultar a un profesional de salud.
            """;

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public AiRagService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
        this.vectorStore = vectorStore;
    }

    public RagIngestResponse ingestMedicalKnowledge() {
        try {
            ClassPathResource resource = new ClassPathResource(KNOWLEDGE_FILE);
            String content = resource.getContentAsString(StandardCharsets.UTF_8);

            Document sourceDocument = new Document(
                    content,
                    Map.of("source", KNOWLEDGE_FILE)
            );
            List<Document> chunks = TokenTextSplitter.builder()
                    .build()
                    .apply(List.of(sourceDocument));

            vectorStore.add(chunks);

            return new RagIngestResponse("Medical knowledge ingested", chunks.size());
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

        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(4)
                .build());

        if (documents == null || documents.isEmpty()) {
            return new RagAskResponse(
                    "No encontre informacion suficiente en la base medica cargada. Consulta con un profesional de salud si tienes sintomas o dudas sobre tu tratamiento.",
                    false,
                    List.of()
            );
        }

        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        String reply = chatClient.prompt()
                .user("""
                        Contexto medico:
                        %s

                        Pregunta del usuario:
                        %s
                        """.formatted(context, question))
                .call()
                .content();

        return new RagAskResponse(reply, true, extractSources(documents));
    }

    private List<String> extractSources(List<Document> documents) {
        Set<String> sources = new LinkedHashSet<>();
        for (Document document : documents) {
            Object source = document.getMetadata().get("source");
            if (source != null) {
                sources.add(source.toString());
            }
        }
        return List.copyOf(sources);
    }
}
