package com.company.app.gateways;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.soffa.foundation.service.data.jpa.MapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Map;


@Entity
@Getter
@Setter
@EqualsAndHashCode
@Table(name = "messages")
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {

    @EmbeddedId
    private MessageId id;
    private String requestId;
    private MessageStatus status;
    @Transient
    private String value;
    private String report;
    private long counter;
    @Convert(converter = MapConverter.class)
    @JsonIgnore
    private Map<String,Object> metadata;

    @Convert(converter = MapConverter.class)
    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private PaymentOptions paymentOptions;
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

}
