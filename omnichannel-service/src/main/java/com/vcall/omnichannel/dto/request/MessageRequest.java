package com.vcall.omnichannel.dto.request;

import com.vcall.omnichannel.entity.Message.ContentType;
import com.vcall.omnichannel.entity.Message.SenderType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotNull
    private SenderType senderType;

    private String senderId;

    @NotNull
    private String content;

    private ContentType contentType;

    private String attachmentUrl;
}
