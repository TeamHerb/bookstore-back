package com.bookwhale.chatroom.service;

import com.bookwhale.article.domain.Article;
import com.bookwhale.article.domain.ArticleRepository;
import com.bookwhale.article.service.ArticleService;
import com.bookwhale.chatroom.domain.ChatRoom;
import com.bookwhale.chatroom.domain.ChatRoomRepository;
import com.bookwhale.chatroom.dto.ChatRoomCreateRequest;
import com.bookwhale.chatroom.dto.ChatRoomResponse;
import com.bookwhale.common.exception.CustomException;
import com.bookwhale.common.exception.ErrorCode;
import com.bookwhale.message.domain.Message;
import com.bookwhale.message.domain.MessageRepository;
import com.bookwhale.push.service.PushProcessor;
import com.bookwhale.user.domain.User;
import com.bookwhale.user.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final ArticleService articleService;
    private final PushProcessor pushProcessor;

    public void createChatRoom(User user, ChatRoomCreateRequest request) {
        User loginUser = userService.findUserByEmail(user.getEmail());
        User seller = getSellerUser(request.getSellerId());
        Article article = articleService.getArticleByArticleId(request.getArticleId());
        if (article.validateIsNotSaleStatus()){
            throw new CustomException(ErrorCode.INVALID_ARTICLE_STATUS_FOR_CREATE_CHATROOM);
        }
        ChatRoom chatRoom = ChatRoom.create(article, loginUser, seller);
        chatRoomRepository.saveAndFlush(chatRoom);
    }

    public User getSellerUser(Long sellerId) {
        return userService.findByUserId(sellerId)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SELLER_ID));
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> findChatRooms(User user) {
        User loginUser = userService.findUserByEmail(user.getEmail());
        List<ChatRoom> rooms = chatRoomRepository.findAllByBuyerOrSellerCreatedDateDesc(loginUser);
        return checkRoomsAndGetRoomResponses(rooms, loginUser);
    }


    public ChatRoomResponse findChatRoomById(User user, Long roomId) {
        User loginUser = userService.findUserByEmail(user.getEmail());
        Optional<ChatRoom> chatRoomByRoomId = chatRoomRepository.findById(roomId);
        if (chatRoomByRoomId.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CHATROOM_ID);
        }
        Long loginUserId = loginUser.getId();
        ChatRoom chatRoom = chatRoomByRoomId.get();
        if (!loginUserId.equals(chatRoom.getBuyer().getId()) &&
        !loginUserId.equals(chatRoom.getSeller().getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        Message lastMessage = messageRepository.findTopByRoomIdOrderByCreatedDateDesc(
                chatRoom.getId())
            .orElseGet(Message::createEmptyMessage);
        return ChatRoomResponse.of(chatRoom, lastMessage, loginUser);
    }

    private List<ChatRoomResponse> checkRoomsAndGetRoomResponses(List<ChatRoom> rooms,
        User loginUser) {
        //내가 떠난 채팅방은 제외
        //상대방이 나간 채팅방인지 확인
        List<ChatRoomResponse> list = new ArrayList<>();
        for (ChatRoom room : rooms) {
            if (!room.isLoginUserDelete(loginUser)) {
                Message lastMessage = messageRepository.findTopByRoomIdOrderByCreatedDateDesc(
                        room.getId())
                    .orElseGet(Message::createEmptyMessage);
                ChatRoomResponse of = ChatRoomResponse.of(room, lastMessage, loginUser);
                list.add(of);
            }
        }
        return list;
    }

    public void deleteChatRoom(User user, Long roomId) {
        User loginUser = userService.findUserByEmail(user.getEmail());
        ChatRoom room = validateRoomIdAndGetRoom(roomId);
        room.deleteChatRoom(loginUser);
        if (room.isEmpty()) {
            chatRoomRepository.deleteById(roomId);
        }
    }

    public ChatRoom validateRoomIdAndGetRoom(Long roomId) {
        return chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CHATROOM_ID));
    }

    public void pushMessageFromChatRoom(Message message) {
        pushProcessor.pushMessageOfChatMessage(message);
    }
}