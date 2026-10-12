package com.jfs.cpts.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfs.cpts.dto.ai.ChatRequest;
import com.jfs.cpts.dto.ai.ChatResponse;
import com.jfs.cpts.dto.ai.ConversationResponse;
import com.jfs.cpts.entity.AiConversation;
import com.jfs.cpts.entity.AiMessage;
import com.jfs.cpts.entity.KnowledgeDocument;
import com.jfs.cpts.entity.User;
import com.jfs.cpts.exception.ResourceNotFoundException;
import com.jfs.cpts.mapper.EntityMapper;
import com.jfs.cpts.repository.AiConversationRepository;
import com.jfs.cpts.repository.AiMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Service implementing RAG-powered Payment Learning Assistant.
 * Uses MySQL knowledge documents as context and interacts with LLM API (e.g. OpenAI),
 * with graceful local fallback when LLM is unconfigured or unreachable.
 */
@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final KnowledgeRetrievalService knowledgeRetrievalService;
    private final EntityMapper mapper;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:gpt-3.5-turbo}")
    private String model;

    @Value("${app.ai.api-url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${app.ai.temperature:0.7}")
    private double temperature;

    @Value("${app.ai.timeout-seconds:30}")
    private int timeoutSeconds;

    public AiService(AiConversationRepository conversationRepository,
                     AiMessageRepository messageRepository,
                     KnowledgeRetrievalService knowledgeRetrievalService,
                     EntityMapper mapper,
                     ObjectMapper objectMapper,
                     WebClient.Builder webClientBuilder) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.knowledgeRetrievalService = knowledgeRetrievalService;
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.build();
    }

    /**
     * Process user chat message, retrieve knowledge context, generate answer, and persist.
     */
    @Transactional
    public ChatResponse processChat(ChatRequest request, User user) {
        AiConversation conversation;
        if (request.conversationId() != null) {
            conversation = conversationRepository.findByIdAndUserId(request.conversationId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", request.conversationId()));
        } else {
            String title = generateTitle(request.message());
            conversation = new AiConversation(title, user);
            conversation = conversationRepository.save(conversation);
        }

        // Save user message
        AiMessage userMsg = new AiMessage(conversation, "user", request.message());
        messageRepository.save(userMsg);

        // Retrieve relevant knowledge documents (RAG)
        List<KnowledgeDocument> contextDocs = knowledgeRetrievalService.retrieveRelevantDocuments(request.message());
        List<String> sourceTitles = contextDocs.stream()
                .map(KnowledgeDocument::getTitle)
                .distinct()
                .collect(Collectors.toList());

        String reply;
        if (aiEnabled && apiKey != null && !apiKey.isBlank()) {
            reply = callExternalLlm(request.message(), contextDocs, conversation);
        } else {
            reply = generateLocalRagResponse(request.message(), contextDocs);
        }

        // Save assistant reply
        AiMessage assistantMsg = new AiMessage(conversation, "assistant", reply);
        messageRepository.save(assistantMsg);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return new ChatResponse(
                conversation.getId(),
                conversation.getTitle(),
                reply,
                sourceTitles,
                LocalDateTime.now()
        );
    }

    /**
     * Get all conversations for a user.
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(Long userId) {
        return conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(c -> mapper.toConversationResponse(c, false))
                .collect(Collectors.toList());
    }

    /**
     * Get a specific conversation with all messages.
     */
    @Transactional(readOnly = true)
    public ConversationResponse getConversation(Long conversationId, Long userId) {
        AiConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        return mapper.toConversationResponse(conversation, true);
    }

    /**
     * Delete a conversation.
     */
    @Transactional
    public void deleteConversation(Long conversationId, Long userId) {
        AiConversation conversation = conversationRepository.findByIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        conversationRepository.delete(conversation);
    }

    private String generateTitle(String prompt) {
        if (prompt == null || prompt.isBlank()) return "New Conversation";
        String trimmed = prompt.trim();
        if (trimmed.length() <= 35) return trimmed;
        return trimmed.substring(0, 32) + "...";
    }

    /**
     * Call external OpenAI-compatible Chat Completions API.
     */
    private String callExternalLlm(String userPrompt, List<KnowledgeDocument> docs, AiConversation conversation) {
        try {
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("You are the Payment Learning Assistant for the Card Payment Transaction Simulator.\n");
            contextBuilder.append("Use the following knowledge documents retrieved from the simulator's knowledge base to answer the user's question accurately and helpfully.\n\n");
            contextBuilder.append("=== KNOWLEDGE BASE CONTEXT ===\n");
            for (KnowledgeDocument doc : docs) {
                contextBuilder.append("[").append(doc.getTitle()).append(" (Category: ").append(doc.getCategory()).append(")]\n");
                contextBuilder.append(doc.getContent()).append("\n\n");
            }
            contextBuilder.append("=== END OF CONTEXT ===\n");
            contextBuilder.append("Always explain concepts clearly and cite the relevant payment concepts.");

            List<Map<String, String>> messagesList = new ArrayList<>();
            messagesList.add(Map.of("role", "system", "content", contextBuilder.toString()));

            // Include last 4 turns for context
            List<AiMessage> past = conversation.getMessages();
            if (past != null) {
                int start = Math.max(0, past.size() - 4);
                for (int i = start; i < past.size(); i++) {
                    AiMessage m = past.get(i);
                    messagesList.add(Map.of("role", m.getRole(), "content", m.getContent()));
                }
            }

            messagesList.add(Map.of("role", "user", "content", userPrompt));

            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", messagesList,
                    "temperature", temperature
            );

            String responseString = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            JsonNode root = objectMapper.readTree(responseString);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                return choices.get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.warn("External LLM call failed or timed out: {}. Falling back to local RAG response.", e.getMessage());
        }

        return generateLocalRagResponse(userPrompt, docs);
    }

    /**
     * Local educational RAG response synthesizer when external LLM is not configured or fails.
     */
    private String generateLocalRagResponse(String prompt, List<KnowledgeDocument> docs) {
        if (docs == null || docs.isEmpty()) {
            return "Hello! I am your Payment Learning Assistant for the Card Payment Transaction Simulator. " +
                   "I can explain payment workflows (Authorization vs Settlement), transaction outcomes (APPROVED, DECLINED, TIMEOUT), " +
                   "PCI DSS requirements, idempotency keys, and simulator architecture. What would you like to explore?";
        }

        StringBuilder response = new StringBuilder();
        response.append("### Payment Knowledge Assistant Response\n\n");

        // Highlight matching sections
        for (int i = 0; i < docs.size(); i++) {
            KnowledgeDocument doc = docs.get(i);
            response.append("#### ").append(i + 1).append(". ").append(doc.getTitle()).append("\n");
            response.append(doc.getContent()).append("\n\n");
        }

        response.append("---\n*💡 Note: Running in built-in knowledge retrieval mode. Set `AI_API_KEY` to enable external LLM reasoning.*");
        return response.toString();
    }
}
