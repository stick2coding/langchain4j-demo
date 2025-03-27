package com.dong.controller;

import com.dong.config.AiConfig;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@RestController
@RequestMapping("/ai_memory")
public class AiMemoryController {

    @Autowired
    AiConfig.Assistant assistant;

    @Autowired
    private AiConfig.AssistantUnique assistantUnique;

    @Autowired
    private AiConfig.AssistantMysql assistantMysql;

    @Autowired
    private AiConfig.AssistantRag assistantRag;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping(value = "memory_chat")
    public String memoryChat(@RequestParam(defaultValue = "我叫东东") String message) {
        return assistant.chat(message);
    }

    @RequestMapping(value = "memory_chat_stream", produces = "text/stream;charset=UTF-8")
    public Flux<String> memoryChatStream(@RequestParam(defaultValue = "我是谁？") String message) {
        TokenStream stream = assistant.stream(message);
        return Flux.create(sink -> {
            stream.onPartialResponse(sink::next)
                    .onCompleteResponse(chatResponse -> sink.complete())
                    .onError(sink::error)
                    .start();
        });
    }

    @RequestMapping(value = "memory_chat_embed", produces = "text/stream;charset=UTF-8")
    public Flux<String> memoryChatEmbed(@RequestParam(defaultValue = "我是谁？") String message) {
        TokenStream stream = assistantRag.stream(message);
        return Flux.create(sink -> {
            stream.onPartialResponse(sink::next)
                    .onCompleteResponse(chatResponse -> sink.complete())
                    .onError(sink::error)
                    .start();
        });
    }

    @RequestMapping(value = "memory_chat_system", produces = "text/stream;charset=UTF-8")
    public Flux<String> memoryChatStreamSystem(@RequestParam(defaultValue = "你是谁？") String message) {
        TokenStream stream = assistant.stream(message, LocalDate.now().toString());
        return Flux.create(sink -> {
            stream.onPartialResponse(sink::next)
                    .onCompleteResponse(chatResponse -> sink.complete())
                    .onError(sink::error)
                    .start();
        });
    }

    @RequestMapping(value = "memoryId_chat")
    public String memoryChat(@RequestParam(defaultValue = "我叫东东") String message, Integer userId) {
        return assistantUnique.chat(userId, message);
    }

    @RequestMapping(value = "memoryId_chat_mysql")
    public String memoryChatMysql(@RequestParam(defaultValue = "我叫东东") String message, Integer userId) {
        return assistantMysql.chat(userId, message);
    }



}
