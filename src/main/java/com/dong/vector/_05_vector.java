package com.dong.vector;

import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.output.Response;

import java.util.List;

public class _05_vector {

    public static void main(String[] args) {
        QwenEmbeddingModel embeddingModel = QwenEmbeddingModel.builder()
                .apiKey("")
                .build();

        Response<Embedding> response = embeddingModel.embed("你好，我是东东");
        System.out.println(response.content().toString());
        System.out.println(response.content().vector().length);
    }

}
