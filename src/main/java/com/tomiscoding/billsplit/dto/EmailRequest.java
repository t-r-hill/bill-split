package com.tomiscoding.billsplit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {

    private EmailAddress from;
    private List<EmailAddress> to;
    private List<EmailVariableGroup> variables;

    @JsonProperty(value = "template_id")
    private String templateId;
}
