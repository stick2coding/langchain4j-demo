package com.dong.controller;

import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai_flux")
public class AiFluxController {

    @Autowired
    OllamaStreamingChatModel ollamaStreamingChatModel;

    @Autowired
    StreamingChatLanguageModel qwenStreamingChatModel;


    @RequestMapping(value = "/chat_qwen_stream", produces = "text/stream;charset=UTF-8")
    public Flux<String> chatQwenStream(@RequestParam(defaultValue = "你是谁？") String message) {
        return Flux.create(sink -> {
            qwenStreamingChatModel.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String s) {
                    sink.next(s);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    sink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    sink.error(throwable);
                }
            });
        });
    }

    @RequestMapping(value = "/chat_ollama_stream", produces = "text/stream;charset=UTF-8")
    public Flux<String> chatOllamaStream(@RequestParam(defaultValue = "你是谁？") String message) {
        return Flux.create(sink -> {
            qwenStreamingChatModel.chat(message, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String s) {
                    sink.next(s);
                }

                @Override
                public void onCompleteResponse(ChatResponse chatResponse) {
                    sink.complete();
                }

                @Override
                public void onError(Throwable throwable) {
                    sink.error(throwable);
                }
            });
        });
    }


}