package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.EmailAddress;
import com.tomiscoding.billsplit.dto.EmailRequest;
import com.tomiscoding.billsplit.dto.EmailSubstitution;
import com.tomiscoding.billsplit.dto.EmailVariableGroup;
import com.tomiscoding.billsplit.exceptions.EmailSendException;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Service
public class MailerSendService {

    @Autowired
    RestTemplate restTemplate;

    @Value(value = "${mailersend.key}")
    private String apiKey;

    @Value(value = "${mailersend.url}")
    private String url;

    @Value(value = "${myapplication.domainurl}")
    private String domainUrl;

    @Value(value = "${mailersend.invitetemplate.id}")
    private String templateId;

    @Value(value = "${myapplication.fromemail}")
    private String fromEmail;

    public void sendInviteEmail(String emailAddress, SplitGroup splitGroup, User user) throws EmailSendException {
        EmailSubstitution userName = EmailSubstitution.builder()
                .var("userName")
                .value(user.getFullName())
                .build();
        EmailSubstitution groupName = EmailSubstitution.builder()
                .var("groupName")
                .value(splitGroup.getGroupName())
                .build();
        EmailSubstitution inviteCode = EmailSubstitution.builder()
                .var("inviteCode")
                .value(splitGroup.getInviteCode())
                .build();
        EmailSubstitution inviteLink = EmailSubstitution.builder()
                .var("inviteLink")
                .value(domainUrl + "splitGroup/join/" + splitGroup.getInviteCode())
                .build();

        EmailVariableGroup emailVariableGroup = EmailVariableGroup.builder()
                .email(emailAddress)
                .substitutions(List.of(
                        userName,
                        groupName,
                        inviteCode,
                        inviteLink))
                .build();

        EmailRequest emailRequest = EmailRequest.builder()
                .from(new EmailAddress(fromEmail))
                .to(Collections.singletonList(new EmailAddress(emailAddress)))
                .variables(Collections.singletonList(emailVariableGroup))
                .templateId(templateId).build();

        sendEmail(emailRequest);
    }

    private void sendEmail(EmailRequest emailRequest) throws EmailSendException {
        // Set http request headers with apikey
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setBearerAuth(apiKey);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add("X-Requested-With", "XMLHttpRequest");

        HttpEntity<EmailRequest> httpEntity = new HttpEntity<>(emailRequest,httpHeaders);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                httpEntity,
                String.class);

        if (!responseEntity.getStatusCode().is2xxSuccessful()){
            String msg = "";
            if (responseEntity.hasBody() && responseEntity.getBody() != null) {
                msg = responseEntity.getBody();
            }
            throw new EmailSendException(msg);
        }
    }
}
