package com.sharmachait.ws.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharmachait.ws.models.dto.*;
import com.sharmachait.ws.models.entity.Role;
import com.sharmachait.ws.models.messages.requestMessages.joinSpace.JoinSpaceMessage;
import com.sharmachait.ws.models.messages.requestMessages.joinSpace.JoinSpacePayload;
import com.sharmachait.ws.models.messages.MessageType;
import com.sharmachait.ws.models.messages.responseMessages.joinedSpace.JoinedSpaceResponse;
import com.sharmachait.ws.models.response.AuthResponse;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SpaceControllerTest {
    @Value("${local.server.port}")
    void setServerPort(int serverPort) {
        SpaceControllerTest.serverPort = serverPort;
    }

    private static int serverPort;
    private static int apiPort = 5455;
    static String adminToken;
    static String userToken;
    static String adminId;
    static String element1Id;
    static String element2Id;
    static String userId;
    static String mapId;
    static String spaceId;
    private static RestTemplate restTemplate;
    private static HttpHeaders headers = new HttpHeaders();


    @BeforeAll
    static void setUp() throws ExecutionException, InterruptedException, TimeoutException {
        setUpHttp();
        setUpWs();
    }

    static void setUpHttp(){
        // Step 1: Signup as admin
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("wsspacecontrolleradmin");
        loginDto.setPassword("password");
        loginDto.setRole(Role.ROLE_ADMIN);
        restTemplate = new RestTemplate();
        String signupUrl = "http://localhost:" + apiPort + "/auth/signup";

        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDto> signupRequest = new HttpEntity<>(loginDto, headers);
        ResponseEntity<AuthResponse> signupResponse = restTemplate.postForEntity(signupUrl, signupRequest, AuthResponse.class);
        AuthResponse authResponse = signupResponse.getBody();
        assert authResponse != null : "Signup failed, response is null.";
        adminToken = authResponse.getJwt();
        adminId = authResponse.getUserId();

        // Step 1: Signup as User
        loginDto.setUsername("wsspacecontrolleruser");
        loginDto.setRole(Role.ROLE_USER);
        HttpEntity<LoginDto> signupRequestUser = new HttpEntity<>(loginDto, headers);
        ResponseEntity<AuthResponse> signupResponseUser = restTemplate.postForEntity(signupUrl, signupRequestUser, AuthResponse.class);
        AuthResponse authResponseUser = signupResponseUser.getBody();
        assert authResponseUser != null : "Signup failed, response is null.";
        userToken = authResponseUser.getJwt();
        userId = authResponseUser.getUserId();

        // Step 2: Create Elements
        String elementUrl = "http://localhost:" + apiPort + "/api/v1/admin/element";
        headers.add("Authorization", "Bearer " + adminToken);
        ElementDto elementDto = new ElementDto();
        elementDto.setImageUrl("https://encrypted-tbn0.gstatic.com/shopping?q=tbn:ANd9GcRCRca3wAR4zjPPTzeIY9rSwbbqB6bB2hVkoTXN4eerXOIkJTG1GpZ9ZqSGYafQPToWy_JTcmV5RHXsAsWQC3tKnMlH_CsibsSZ5oJtbakq&usqp=CAE");
        elementDto.setWidth(1);
        elementDto.setHeight(1);
        elementDto.setIsStatic(true);
        HttpEntity<ElementDto> elementRequest = new HttpEntity<>(elementDto, headers);
        ResponseEntity<ElementDto> elementResponse = restTemplate.postForEntity(elementUrl, elementRequest, ElementDto.class);
        ResponseEntity<ElementDto> elementResponse2 = restTemplate.postForEntity(elementUrl, elementRequest, ElementDto.class);
        element1Id = elementResponse.getBody().getId();
        element2Id = elementResponse2.getBody().getId();

        // Step 3: Create Map
        String mapUrl = "http://localhost:" + apiPort + "/api/v1/admin/map";
        GameMapDto mapDto = new GameMapDto();
        mapDto.setThumbnail("https://thumbnail.com/a.png");
        mapDto.setDimensions("100x200");
        MapElementDto mapElement1 = MapElementDto.builder()
                .elementId(element1Id)
                .x(20)
                .y(20)
                .build();
        MapElementDto mapElement2 = MapElementDto.builder()
                .elementId(element1Id)
                .x(18)
                .y(20)
                .build();
        MapElementDto mapElement3 = MapElementDto.builder()
                .elementId(element2Id)
                .x(19)
                .y(20)
                .build();
        MapElementDto mapElement4 = MapElementDto.builder()
                .elementId(element2Id)
                .x(19)
                .y(20)
                .build();
        List<MapElementDto> l = List.of(mapElement1, mapElement2, mapElement3, mapElement4);
        mapDto.setMapElements(l);
        HttpEntity<GameMapDto> mapRequest = new HttpEntity<>(mapDto, headers);
        ResponseEntity<GameMapDto> mapResponse = restTemplate.postForEntity(mapUrl, mapRequest, GameMapDto.class);
        mapId = mapResponse.getBody().getId();

        //step 4: create a Space
        String spaceUrl = "http://localhost:" + apiPort + "/api/v1/space";
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", "Bearer " + userToken);
        SpaceDto spaceDto = SpaceDto.builder()
                .name("Test Space")
                .dimensions("100x200")
                .mapId(mapId)
                .build();
        HttpEntity<SpaceDto> request = new HttpEntity<>(spaceDto, headers);
        ResponseEntity<SpaceDto> spaceResponse = restTemplate.postForEntity(spaceUrl, request, SpaceDto.class);
        spaceId = spaceResponse.getBody().getId();
    }

    private static String getWsPath() {
        return "ws://localhost:" + serverPort + "/ws";
    }

    private static WebSocketStompClient stompClient;
    private static StompSession ws1,ws2;
    private static List<Object> ws1Messages, ws2Messages;

    static void setUpWs() throws ExecutionException, InterruptedException, TimeoutException {

        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
//        ws1Future= new CompletableFuture<>();
        ws1Messages = new ArrayList<>();
//        ws2Future= new CompletableFuture<>();
        ws2Messages = new ArrayList<>();

        ws1 = stompClient.connectAsync(getWsPath(), new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
        ws2 = stompClient.connectAsync(getWsPath(), new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);

        ws1.subscribe("/topic/space", new StompFrameHandler() {
            @Override
            @NonNull
            public Type getPayloadType(@Nullable StompHeaders headers) {
                return String.class; // Assume the payload is a JSON string
            }

            @Override
            public void handleFrame(@Nullable StompHeaders headers, Object payload) {
                Object message = parseMessage(payload);
                if(message!=null)
                    addMessage(ws1Messages, message);
            }
        });
        ws2.subscribe("/topic/space", new StompFrameHandler() {
            @Override
            @NonNull
            public Type getPayloadType(@Nullable StompHeaders headers) {
                return String.class; // Assume the payload is a JSON string
            }

            @Override
            public void handleFrame(@Nullable StompHeaders headers, Object payload) {
                Object message = parseMessage(payload);
                if(message!=null)
                    addMessage(ws2Messages, message);
            }
        });
    }

    private static Object parseMessage(Object payload){
        String jsonPayload = (String) payload;
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonPayload);
            String typeString = jsonNode.get("type").asText();
            MessageType type = MessageType.valueOf(typeString);
            switch (type) {
                case SPACE_JOINED:
                    return objectMapper.readValue(jsonPayload, JoinedSpaceResponse.class);
                case JOIN:
                    return objectMapper.readValue(jsonPayload, JoinSpaceMessage.class);
                default:
                    return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static final Object lock = new Object();
    // To safely add a message and notify waiting threads
    static void addMessage(List<Object> messages, Object message) {
        synchronized (lock) {
            messages.add(message);
            lock.notifyAll(); // Notify all waiting threads
        }
    }

    private CompletableFuture<Object> waitForAndPopLatestMessages(List<Object> messages) {
        return CompletableFuture.supplyAsync(() -> {
            synchronized (lock) {
                try {
                    // Wait with a timeout to prevent infinite waiting
                    long waitTime = 100;
                    long startTime = System.currentTimeMillis();

                    while (messages.isEmpty()) {
                        long remainingTime = waitTime - (System.currentTimeMillis() - startTime);
                        if (remainingTime <= 0) {
                            throw new TimeoutException("No message received within timeout period");
                        }
                        lock.wait(remainingTime);
                    }
                    // Extra safety check
                    return messages.remove(0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted", e);
                } catch (TimeoutException e) {
                    throw new RuntimeException("Waiting for message timed out", e);
                }
            }
        });
    }

    @Order(1)
    @Test
    void getAckOnJoiningSpace() throws ExecutionException, InterruptedException, TimeoutException {
        //join a room
        JoinSpaceMessage ws1Message = JoinSpaceMessage.builder()
                .type(MessageType.JOIN)
                .payload(JoinSpacePayload.builder()
                        .spaceId(spaceId)
                        .token(adminToken)
                        .build())
                .build();

        JoinSpaceMessage ws2Message = JoinSpaceMessage.builder()
                .type(MessageType.JOIN)
                .payload(JoinSpacePayload.builder()
                        .spaceId(spaceId)
                        .token(userToken)
                        .build())
                .build();

        ws1.send("/topic/space",ws1Message);
        ws2.send("/topic/space",ws2Message);

        CompletableFuture<Object> ws1future = waitForAndPopLatestMessages(ws1Messages);
        JoinedSpaceResponse ws1response = (JoinedSpaceResponse)ws1future.get(100,TimeUnit.MILLISECONDS);
        assertEquals(MessageType.SPACE_JOINED, ws1response.getType());
    }
}

