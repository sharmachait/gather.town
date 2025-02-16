package com.sharmachait.ws.service;

import com.sharmachait.ws.models.entity.ChatRoom;
import com.sharmachait.ws.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatRoomService {

  @Autowired
  private final ChatRoomRepository chatRoomRepository;

  public ChatRoom getChatRoom(
      String senderId,
      String recipientId,
      boolean createNewRoomIfNotExist) throws NoSuchElementException {
    Optional<ChatRoom> chatRoom = chatRoomRepository.findBySenderAndRecipient(senderId, recipientId);
    if (chatRoom.isPresent()) {
      return chatRoom.get();
    } else {
      if (createNewRoomIfNotExist) {
        return createChat(senderId, recipientId);
      } else {
        throw new NoSuchElementException();
      }
    }
  }

  public ChatRoom createChat(String senderId, String recipientId) {
    String chatId = senderId + "_" + recipientId;
    String chatIdReverse = recipientId + "_" + senderId;

    ChatRoom senderRecipient = ChatRoom.builder()
        .chatId(chatId)
        .sender(senderId)
        .recipient(recipientId)
        .build();

    ChatRoom recipientSender = ChatRoom.builder()
        .chatId(chatIdReverse)
        .sender(recipientId)
        .recipient(senderId)
        .build();

    chatRoomRepository.save(recipientSender);

    return chatRoomRepository.save(senderRecipient);
  }
}
