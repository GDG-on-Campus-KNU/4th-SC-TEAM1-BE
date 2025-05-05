package com.gdg.Todak.common.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiModelConfig {

    public static final String GEMINI_2_0_FLASH = "gemini-2.0-flash";

    @Value("${gemini.api.key}")
    public String GEMINI_API_KEY;

    @Bean
    public ChatLanguageModel geminiChatModel() {
        return GoogleAiGeminiChatModel.builder()
            .apiKey(GEMINI_API_KEY)
            .modelName(GEMINI_2_0_FLASH)
            .build();
    }
}
