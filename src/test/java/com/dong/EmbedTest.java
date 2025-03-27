package com.dong;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Properties;

public class EmbedTest {

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

        // 使用内存实现向量数据库
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey(config.getProperty("qwen.api.key"))
                .build();

        // 先将内容分段
        TextSegment segment = TextSegment.from("""
                预订航班：
                - 通过我们的网站或移动应用程序预订。
                - 预订时需要全额付款，
                - 确保个人信息（姓名、ID等）的准确性，因为更正可能会产生25的费用。
                """);
        // 向量化
        Embedding embedding = embeddingModel.embed(segment).content();
        // 存储
        embeddingStore.add(embedding, segment);

        // 第二组内容
        TextSegment segment1 = TextSegment.from("""
                取消预订：
                - 最晚在航班起飞前48小时取消。
                - 取消费用：经济舱75美元，豪华经济舱50美元，商务舱25美元。
                - 退款将在 7个工作日内处理。
                """);
        // 向量化
        Embedding embedding1 = embeddingModel.embed(segment1).content();
        // 存储
        embeddingStore.add(embedding1, segment1);

        // 查询内容向量化
        Embedding query = embeddingModel.embed("""
                如何取消预订？
                """).content();

        // 构建查询
        EmbeddingSearchRequest build = EmbeddingSearchRequest.builder()
                .queryEmbedding(query)
                .maxResults(1)
                .build();

        // 查询
        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(build);
        search.matches().forEach(match -> {
            System.out.println(match.score());
            System.out.println(match.embedded().text());
        });

    }

}
