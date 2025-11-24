package career.exploration.service;


import career.exploration.config.GeminiConfig;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {
    private final GeminiConfig geminiConfig;
    private final WebClient webClient;
    private final Gson gson = new Gson();

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/{modelName}:generateContent";
    private static final String JSON_KEY_IS_CORRECT = "iscorrect";
    private static final String JSON_KEY_REASON = "reason";

    public boolean evaluateEssayAnswer(String question, String correctAnswer, String userAnswer) {
        try {
            log.info("============= 주관식 문제 평가 시작 ============");
            log.info("문제: {}", question);
            log.info("모범 답안: {}", correctAnswer);
            log.info("학생 답변: {}", userAnswer);

            String response = callGeminiApi(buildPrompt(question, correctAnswer, userAnswer));

            boolean isCorrect = parseResponse(response);
            String reason = parseReason(response);

            log.info("Gemini 평가 결과: {}", (isCorrect ? "정답" : "오답"));
            log.info("평가 이유: {}", reason);
            log.info("============= 주관식 문제 평가 완료 ============");

            return isCorrect;
        } catch (Exception e) {
            log.error("주관식 답변 평가 중 Gemini API 오류 발생", e);
            log.error("============= 주관식 문제 평가 오류 ============\n오류 내용: {}", e.getMessage());
            return false; // API 호출 실패 시 기본값으로 오답 처리
        }
    }

    private String buildPrompt(String question, String correctAnswer, String userAnswer) {
        return String.format("""
            당신은 학생들의 주관식 문제 답변을 평가하는 AI 조교입니다.
            
            문제와 주어진 정답과 학생의 답변을 비교하여 학생의 답변이 맞는지 평가해주세요.
            답변은 다른 방식으로 표현되었더라도 의미적으로 같다면 정답으로 인정합니다.
            사소한 표현 차이는 관대하게 평가해 주세요.
            학생이 핵심 개념을 이해하고 있는지가 중요합니다.
            
            다음은 채점 예시입니다.
            
            ---
            예시 1:
            문제: 스프링 부트의 핵심 애노테이션은 무엇인가요?
            정답: @SpringBootApplication
            학생 답변: 스프링부트어플리케이션
            평가:
            {
            "iscorrect": true,
            "reason": "학생이 '@' 기호와 대소문자를 정확히 쓰지는 않았지만, 핵심 용어인 'SpringBootApplication'을 정확히 알고 있으므로 정답으로 인정합니다."
            }
            ---
            예시 2:
            문제: 스프링 부트 애플리케이션의 내장 웹 서버 기본 포트는 무엇인가요?
            정답: 8080
            학생 답변: 80
            평가:
            {
            "iscorrect": false,
            "reason": "학생의 답변 '80'은 정답 '8080'과 일치하지 않고 주어진 문제의 스프링 부트 애플리케이션의 내장 웹 서버 기본 포트는 8080이므로 오답입니다."
            }
            ---
            
            문제: %s
            
            정답: %s
            
            학생 답변: %s
            
            다음 형식의 JSON으로 응답해주세요:
            JSON 블록 외에 다른 설명이나 불필요한 텍스트를 절대 추가하지 마세요.
            {
              "%s": true/false,
              "%s": "평가 이유를 자세히 설명해주세요. 왜 맞았는지 또는 왜 틀렸는지 구체적으로 작성해주세요."
            }
            """, question, correctAnswer, userAnswer, JSON_KEY_IS_CORRECT, JSON_KEY_REASON);
    }

    private Optional<JsonObject> extractJson(String response) {
        if (response == null) {
            return Optional.empty();
        }
        int firstBrace = response.indexOf('{');
        int lastBrace = response.lastIndexOf('}');

        if (firstBrace != -1 && lastBrace > firstBrace) {
            String jsonBlock = response.substring(firstBrace, lastBrace + 1);
            try {
                return Optional.of(gson.fromJson(jsonBlock, JsonObject.class));
            } catch (JsonSyntaxException e) {
                log.warn("추출된 JSON 블록 파싱 실패: {}. 원인: {}", jsonBlock, e.getMessage());
            }
        }
        return Optional.empty();
    }

    private String parseReason(String response) {
        return extractJson(response)
                .filter(json -> json.has(JSON_KEY_REASON))
                .map(json -> json.get(JSON_KEY_REASON).getAsString())
                .orElse(response); // JSON에서 이유를 못찾으면 원본 응답 반환
    }

    private String callGeminiApi(String prompt) {
        String apiUrl = GEMINI_API_URL.replace("{modelName}", geminiConfig.getModel());
        log.debug("API URL: {}", apiUrl);

        JsonObject requestBody = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonObject textPart = new JsonObject();

        textPart.addProperty("text", prompt);
        content.add("parts", new JsonArray());
        content.getAsJsonArray("parts").add(textPart);
        contents.add(content);
        requestBody.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("maxOutputTokens", geminiConfig.getMaxTokens());
        generationConfig.addProperty("temperature", geminiConfig.getTemperature());
        generationConfig.addProperty("topK", geminiConfig.getTopK());
        generationConfig.addProperty("topP", geminiConfig.getTopP());
        requestBody.add("generationConfig", generationConfig);

        String requestBodyString = gson.toJson(requestBody);
        log.debug("API 요청 본문: {}", requestBodyString);

        String responseBody = webClient.post()
                .uri(apiUrl, uriBuilder -> uriBuilder.queryParam("key", geminiConfig.getKey()).build())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBodyString)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                    clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                log.error("API 응답 오류: 상태 코드 = {}, 본문 = {}", clientResponse.statusCode(), errorBody);
                                return Mono.error(new RuntimeException("Gemini API call failed with status: " + clientResponse.statusCode()));
                            })
                )
                .bodyToMono(String.class)
                .block();

        if (responseBody != null) {
            log.debug("API 응답 본문 일부: {}", responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody);
        } else {
            log.debug("API 응답 본문 없음");
        }

        return extractContentFromResponse(responseBody);
    }

    private String extractContentFromResponse(String responseBody) {
        try {
            if (responseBody == null || responseBody.isBlank()) {
                 log.warn("Gemini API 응답 본문이 비어있습니다.");
                 return "{}";
            }
            JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
            JsonArray candidates = responseJson.getAsJsonArray("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
                JsonObject content = firstCandidate.getAsJsonObject("content");
                JsonArray parts = content.getAsJsonArray("parts");
                if (parts != null && !parts.isEmpty()) {
                    String text = parts.get(0).getAsJsonObject().get("text").getAsString();
                    log.debug("추출된 응답 텍스트: {}", text);
                    return text;
                }
            }
            log.warn("Gemini API 응답에서 텍스트 콘텐츠를 찾을 수 없습니다. 응답: {}", responseBody);
            return responseBody; // 파싱 실패 시 원본 반환
        } catch (Exception e) {
            log.error("Gemini API 응답 파싱 오류", e);
            log.warn("원본 응답: {}", responseBody);
            return responseBody; // 파싱 실패 시 원본 반환
        }
    }

    private boolean parseResponse(String response) {
        // 1. JSON 블록을 먼저 찾아서 파싱 시도
        Optional<JsonObject> jsonOptional = extractJson(response);
        if (jsonOptional.isPresent()) {
            JsonObject jsonResponse = jsonOptional.get();
            if (jsonResponse.has(JSON_KEY_IS_CORRECT)) {
                boolean result = jsonResponse.get(JSON_KEY_IS_CORRECT).getAsBoolean();
                log.info("JSON 파싱 결과: isCorrect = {}", result);
                return result;
            }
        }

        // 2. JSON 파싱에 실패하면 텍스트 분석으로 대체
        log.warn("응답에서 유효한 JSON을 찾지 못했습니다. 텍스트 기반 분석을 시도합니다.");
        String lowerCaseResponse = response.toLowerCase();
        if (lowerCaseResponse.contains("\"" + JSON_KEY_IS_CORRECT + "\":false") ||
            lowerCaseResponse.contains("incorrect") ||
            lowerCaseResponse.contains("틀렸") ||
            lowerCaseResponse.contains("오답")) {
            log.info("텍스트 분석으로 오답 판정");
            return false;
        }

        if (lowerCaseResponse.contains("\"" + JSON_KEY_IS_CORRECT + "\":true") ||
            lowerCaseResponse.contains("정답") ||
            lowerCaseResponse.contains("맞습니다") ||
            lowerCaseResponse.contains("correct") ||
            (lowerCaseResponse.contains("일치") && !lowerCaseResponse.contains("불일치"))) {
            log.info("텍스트 분석으로 정답 판정");
            return true;
        }

        log.info("정확한 판정 불가, 오답으로 처리");
        return false;
    }
}
