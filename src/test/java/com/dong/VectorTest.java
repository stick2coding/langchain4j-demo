package com.dong;

import com.dong.config.AiConfig;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class VectorTest {

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
    public void test() throws URISyntaxException {
        Properties config = loadConfig();

        Path documentPath = Paths.get(VectorTest.class.getClassLoader().getResource("rag/test.txt").toURI());
        DocumentParser documentParser = new TextDocumentParser();
        Document document = FileSystemDocumentLoader.loadDocument(documentPath, documentParser);

        // 每段最长字数，自然语言最大重叠字数
        DocumentByCharacterSplitter splitter = new DocumentByCharacterSplitter(20, 10);

        List<TextSegment> segments = splitter.split(document);

        for (TextSegment segment : segments) {
            System.out.println(":::" + segment.text());
        }

        // 向量模型
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey(config.getProperty("qwen.api.key"))
                .build();

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // 存储
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        System.out.println("--------------------------------");
        // 查询
        Response<Embedding> embed = embeddingModel.embed("取消费用");

        // 查询内容向量化
//        Embedding query = embeddingModel.embed("退费费用").content();

        // 构建查询
        EmbeddingSearchRequest build = EmbeddingSearchRequest.builder()
                .queryEmbedding(embed.content())
                .maxResults(1)
                .build();

        // 查询
        EmbeddingSearchResult<TextSegment> search = embeddingStore.search(build);
        search.matches().forEach(match -> {
            System.out.println(match.score());
            System.out.println(match.embedded().text());
        });


        // 聊天
        System.out.println("--------------------------------");

        ChatLanguageModel model = QwenChatModel.builder()
                .apiKey(config.getProperty("qwen.api.key"))
                .modelName("qwen-max")
                .build();

        // 内容检索，绑定向量数据库和向量模型
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();

        // 创建对话，绑定内容检索器，绑定对话模型
        AiConfig.Assistant assistant = AiServices.builder(AiConfig.Assistant.class)
                .chatLanguageModel(model)
                .contentRetriever(contentRetriever)
                .build();

        System.out.println(assistant.chat("退费费用"));



    }

}
