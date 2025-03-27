package com.dong.config;

import com.dong.Langchain4jDemoApplication;
import com.dong.memory.MysqlChatMemoryStoreJpa;
import com.dong.repository.ChatMessageRepository;
import com.dong.service.ToolsService;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.*;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class AiConfig {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    //InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    public interface Assistant {
        String chat(String message);
        // 流式
        TokenStream stream(String message);

        // 预设角色
        @SystemMessage("""
                你是一名小管家，正在通过聊天的方式和用户进行沟通，你先从用户这里获取以下信息：用户姓名。
                请讲中文。
                今天的日期是{{current_date}}
                """)
        TokenStream stream(@UserMessage String message, @V("current_date") String currentDate);
    }

    public interface AssistantRag {
        String chat(String message);
        // 流式
        TokenStream stream(String message);
    }

    public interface AssistantUnique {
        String chat(@MemoryId int memoryId, @UserMessage String message);
        // 流式
        TokenStream stream(@MemoryId int memoryId, @UserMessage String message);
    }

    public interface AssistantMysql {
        String chat(@MemoryId int memoryId, @UserMessage String message);
        // 流式
        TokenStream stream(@MemoryId int memoryId, @UserMessage String message);
    }

    @Bean
    public Assistant assistant(ChatLanguageModel qwenChatModel, StreamingChatLanguageModel qwenStreamingChatModel, ToolsService toolsService) {

        // 定义一个对话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .tools(toolsService)
                .chatMemory(chatMemory)
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    @Bean
    public AssistantRag assistantRag(ChatLanguageModel qwenChatModel,
                                  StreamingChatLanguageModel qwenStreamingChatModel,
                                  ToolsService toolsService,
                                  EmbeddingStore<TextSegment> embeddingStore,
                                  QwenEmbeddingModel qwenEmbeddingModel) {

        // 定义一个对话记忆
        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 定义一个搜索器
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5)
                .minScore(0.6)
                .build();

        // 加入搜索器
        return AiServices.builder(AssistantRag.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .tools(toolsService)
                .contentRetriever(contentRetriever)
                .chatMemory(chatMemory)
                .build();
    }

    @Bean
    public AssistantUnique assistantUnique(ChatLanguageModel qwenChatModel, StreamingChatLanguageModel qwenStreamingChatModel){
        return AiServices.builder(AssistantUnique.class)
                .chatLanguageModel(qwenChatModel)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder().maxMessages(10).id(memoryId).build())
                .build();
    }


    @Bean
    public AssistantMysql assistantMysql(ChatLanguageModel qwenChatModel, StreamingChatLanguageModel qwenStreamingChatModel){

        MysqlChatMemoryStoreJpa mysqlChatMemoryStoreJpa = new MysqlChatMemoryStoreJpa(chatMessageRepository);

        ChatMemoryProvider memoryProvider = memoryId -> MessageWindowChatMemory
                .builder().maxMessages(10).id(memoryId).chatMemoryStore(mysqlChatMemoryStoreJpa).build();;

        return AiServices.builder(AssistantMysql.class)
                .chatLanguageModel(qwenChatModel)
                .chatMemoryProvider(memoryProvider)
                .streamingChatLanguageModel(qwenStreamingChatModel)
                .build();
    }

    @Bean
    CommandLineRunner insertTermOfServiceToVectorStore(QwenEmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) throws URISyntaxException {
        // 读取
        Path documentPath = Paths.get(Langchain4jDemoApplication.class.getClassLoader().getResource("rag/test.txt").toURI());

        return args -> {
            DocumentParser documentParser = new TextDocumentParser();
            Document document = FileSystemDocumentLoader.loadDocument(documentPath, documentParser);

            DocumentByCharacterSplitter splitter = new DocumentByCharacterSplitter(20, 10);

            List<TextSegment> segments = splitter.split(document);

            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

            embeddingStore.addAll(embeddings, segments);
        };
    }


}
