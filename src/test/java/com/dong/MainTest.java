package com.dong;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;


public class MainTest {

    private static final String CONFIG_FILE = "config.properties";

    private Properties loadConfig() {
        Properties prop = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("找不到配置文件: " + CONFIG_FILE);
            }
            prop.load(input);
        } catch (Exception e) {
            throw new RuntimeException("加载配置失败", e);
        }
        return prop;
    }

    @Test
    void testMain() {

        Properties config = loadConfig();

        ChatLanguageModel model = OpenAiChatModel
                .builder()
                .baseUrl(config.getProperty("api.url"))
                .apiKey(config.getProperty("api.key"))
                .modelName(config.getProperty("api.model"))
                .build();

        // 对话
        String response = model.chat("你好，你是谁？");
        System.out.println(response);
    }

    // 测试多轮对话，需要将上一次的对话记录同时返给AI
    @Test
    void testMain1() {

        Properties config = loadConfig();

        ChatLanguageModel model = OpenAiChatModel
                .builder()
                .baseUrl(config.getProperty("api.url"))
                .apiKey(config.getProperty("api.key"))
                .modelName(config.getProperty("api.model"))
                .build();

        UserMessage userMessage1 = UserMessage.userMessage("你好，我是东东");
        ChatResponse response1 = model.chat(userMessage1);
        AiMessage aiMessage1 = response1.aiMessage();

        System.out.println(aiMessage1.text());
        System.out.println("-----------------");

        ChatResponse response2 = model.chat(userMessage1, aiMessage1, UserMessage.userMessage("我叫什么？"));
        AiMessage aiMessage2 = response2.aiMessage();
        System.out.println(aiMessage2.text());
    }

}