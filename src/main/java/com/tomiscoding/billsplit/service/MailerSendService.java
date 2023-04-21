package com.tomiscoding.billsplit.service;

import com.tomiscoding.billsplit.dto.EmailAddress;
import com.tomiscoding.billsplit.dto.EmailRequest;
import com.tomiscoding.billsplit.dto.EmailSubstitution;
import com.tomiscoding.billsplit.dto.EmailVariableGroup;
import com.tomiscoding.billsplit.exceptions.EmailSendException;
import com.tomiscoding.billsplit.model.SplitGroup;
import com.tomiscoding.billsplit.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

/**
 * Class to handle api calls to MailerSend using RestTemplate to send emails to users/invited users.
 * Requires api key and url defined in properties and template/sender information for creating the email.
 */
@Service
@RequiredArgsConstructor
public class MailerSendService {

    private final RestTemplate restTemplate;

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

    /**
     * Creates an EmailRequest object including to/from email address and required variables for the email template.
     * Then calls sendEmail() passing the constructed EmailRequest object as an argument.
     * @param emailAddress a valid email address for the email to be sent to.
     * @param splitGroup the group which the user is being invited to
     * @param user the user which is sending the invite email
     * @throws EmailSendException thrown from sendEmail()
     */
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

    /**
     * Constructs an HttpEntity with correct headers and the supplied EmailRequest object, then send request and handles
     * response.
     * @param emailRequest the request body object to be sent in the request
     * @throws EmailSendException if a non-2xx status response is received with the response body in the error message
     */
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
