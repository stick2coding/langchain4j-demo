package com.dong.memory;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MysqlChatMemoryStoreJdbc implements ChatMemoryStore {

    private final JdbcTemplate jdbcTemplate;

    public MysqlChatMemoryStoreJdbc(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        List<ChatMessage> messages = new ArrayList<>();
        String sql = "SELECT type, content FROM chat_messages WHERE memory_id = ?";
        List<Map<String,Object>> result = jdbcTemplate.queryForList(sql, memoryId.toString());
        for (Map<String, Object> resultSet : result){
            String type = (String) resultSet.get("type");
            String content = (String) resultSet.get("content");
            if ("USER".equals(type)){
                messages.add(new UserMessage(content));
            }
            if ("AI".equals(type)){
                messages.add(new AiMessage(content));
            }
        }
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        String sql = "INSERT INTO chat_messages (memory_Id, type, content) VALUES (?, ?, ?)";

        for (ChatMessage message : list) {
            if(message.type().equals(ChatMessageType.USER)){
                String type = "USER";
                String content = ((UserMessage) message).singleText();
                jdbcTemplate.update(sql, memoryId.toString(), type, content);
            }

            if(message.type().equals(ChatMessageType.AI)){
                String type = "AI";
                String content = ((AiMessage) message).text();
                jdbcTemplate.update(sql, memoryId.toString(), type, content);
            }
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String sql = "DELETE FROM chat_messages WHERE memeory_id = ?";
        jdbcTemplate.update(sql, memoryId.toString());
    }
}