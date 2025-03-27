package com.dong.memory;

import com.dong.entity.ChatMessageEntity;
import com.dong.repository.ChatMessageRepository;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MysqlChatMemoryStoreJpa implements ChatMemoryStore {

    private final ChatMessageRepository chatMessageRepository;

    public MysqlChatMemoryStoreJpa(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }


    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        List<ChatMessage> messages = new ArrayList<>();
        List<ChatMessageEntity> entities = chatMessageRepository.findByMemoryId(memoryId.toString());
        for (ChatMessageEntity entity : entities) {
            if ("USER".equals(entity.getType())) {
                messages.add(new UserMessage(entity.getContent()));
            }
            if ("AI".equals(entity.getType())) {
                messages.add(new AiMessage(entity.getContent()));
            }
        }
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> list) {
        for (ChatMessage message : list) {
            ChatMessageEntity entity = new ChatMessageEntity();
            entity.setMemoryId(memoryId.toString());
            entity.setType(message.type().name());

            // 根据消息类型设置内容
            switch (message.type()) {
                case USER:
                    entity.setContent(((UserMessage) message).singleText());
                    break;
                case AI:
                    entity.setContent(((AiMessage) message).text());
                    break;
                case SYSTEM:
                    entity.setContent(((SystemMessage) message).text());
                    break;
                case TOOL_EXECUTION_RESULT:
                    entity.setContent(((ToolExecutionResultMessage) message).text());
                    break;
            }
            boolean exist = chatMessageRepository.existsByMemoryIdAndContent(memoryId.toString(), entity.getContent());
            if (exist) {
                continue;
            }

            chatMessageRepository.save(entity);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMessageRepository.deleteByMemoryId(memoryId.toString());
    }
}