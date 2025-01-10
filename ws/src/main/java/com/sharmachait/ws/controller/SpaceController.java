package com.sharmachait.ws.controller;

import com.sharmachait.ws.config.jwt.JwtProvider;
import com.sharmachait.ws.models.entity.Role;
import com.sharmachait.ws.models.entity.Status;
import com.sharmachait.ws.models.entity.User;
import com.sharmachait.ws.models.messages.MessageType;
import com.sharmachait.ws.models.messages.requestMessages.joinSpace.JoinSpaceRequest;
import com.sharmachait.ws.models.messages.responseMessages.joinedSpace.JoinSpaceResponse;
import com.sharmachait.ws.models.messages.responseMessages.joinedSpace.JoinSpaceResponsePayload;
import com.sharmachait.ws.models.messages.responseMessages.joinedSpace.UserSpawn;
import com.sharmachait.ws.repository.UserRespository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SpaceController {

    @Autowired
    SimpMessageSendingOperations messagingTemplate;
    @Autowired
    private UserRespository userRespository;

    @MessageMapping("/space")
    public void joinSpace(@Payload JoinSpaceRequest request,
                                       SimpMessageHeaderAccessor headerAccessor) throws Exception {
        if(!request.getType().equals(MessageType.JOIN)){
//            return JoinSpaceResponse.builder()
//                    .type(MessageType.BAD_REQUEST)
//                    .payload(JoinSpaceResponsePayload.builder()
//                            .message("wrong type sent to wrong endpoint")
//                            .build())
//                    .build();
            return;
        }

        String token = request.getPayload().getToken();
        String userEmail = JwtProvider.getEmailFromToken(token);
        String spaceId = request.getPayload().getSpaceId();

        headerAccessor.getSessionAttributes().put("user___space",userEmail+"___"+spaceId );

        User user = User.builder()
                .status(Status.ONLINE)
                .role(Role.ROLE_USER)
                .spaceId(spaceId)
                .username(userEmail)
                .build();

        user = userRespository.save(user);
        request.getPayload().setUserId(user.getId());
        JoinSpaceResponse response = JoinSpaceResponse.builder()
                .type(MessageType.SPACE_JOINED)
                .payload(JoinSpaceResponsePayload.builder()
                        .spaceId(request.getPayload().getSpaceId())
                        .userId(request.getPayload().getUserId())
                        .build())
                .build();
//        private List<UserSpawn> users;
//        private int x;
//        private int y;
        messagingTemplate.convertAndSend("/topic/space/" + spaceId, response);

//        return JoinSpaceResponse.builder().build();
    }

}
