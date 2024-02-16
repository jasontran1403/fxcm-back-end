package com.something.service;

import javax.mail.MessagingException;

import com.something.domain.Email;

public interface MaillerService {
    void send(Email mail) throws MessagingException;

    void send(String to, String subject, String body) throws MessagingException;

    void queue(Email mail);

    void queue(String to, String subject, String body) throws MessagingException;
}