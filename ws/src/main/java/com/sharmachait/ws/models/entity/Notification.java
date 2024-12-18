package com.sharmachait.ws.models.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Notification {
    private Long id;
    private Long senderId;
    private Long recipientId;
    private String message;
}