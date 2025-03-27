package com.dong.controller;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingLanguageModel;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.language.StreamingLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    QwenChatModel qwenChatModel;

//    @Autowired
//    OllamaChatModel ollamaChatModel;

    // 注入的时候先by类型再by名字
    @Autowired
    ChatLanguageModel ollamaChatModel;

    @RequestMapping("/chat")
    public String chat(@RequestParam(defaultValue = "你是谁？") String message) {
        return qwenChatModel.chat(message);
    }

    @RequestMapping("/chat_ollama")
    public String chatOllama(@RequestParam(defaultValue = "你是谁？") String message) {
        return ollamaChatModel.chat(message);
    }




}