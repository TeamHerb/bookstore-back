package com.bookwhale.message.controller;

import static com.bookwhale.config.StompWebSocketConfig.SUB_PREFIX;

import com.bookwhale.common.dto.Pagination;
import com.bookwhale.message.dto.MessageRequest;
import com.bookwhale.message.dto.MessageResponse;
import com.bookwhale.message.service.MessageService;
import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {

    public static final String SUBSCRIBE = SUB_PREFIX + "/chat/room/";
    public static final String PUBLISH = "/chat/message";

    private final MessageService messageService;

    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/api/message/{roomId}")
    public ResponseEntity<List<MessageResponse>> findMessages(@PathVariable Long roomId,
        @Valid Pagination pagination) {
        return ResponseEntity.ok(messageService.findMessages(roomId, pagination));
    }

    @GetMapping("/api/message/{roomId}/last")
    public ResponseEntity<MessageResponse> findLastMessage(@PathVariable Long roomId) {
        return ResponseEntity.ok(messageService.findLastMessage(roomId));
    }

    @MessageMapping(PUBLISH)
    public void message(MessageRequest request) {
        MessageResponse response = messageService.saveMessage(request);
        messagingTemplate.convertAndSend(SUBSCRIBE + request.getRoomId(), response);
    }
}
