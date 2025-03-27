package com.dong.repository;

import com.dong.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {

    List<ChatMessageEntity> findByMemoryId(String memoryId);

    void deleteByMemoryId(String memoryId);

    @Query("SELECT COUNT(c) > 0 FROM ChatMessageEntity c WHERE c.memoryId = :memoryId AND c.content = :content")
    boolean existsByMemoryIdAndContent(@Param("memoryId") String memoryId, @Param("content") String content);
}