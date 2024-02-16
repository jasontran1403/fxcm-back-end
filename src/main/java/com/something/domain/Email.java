package com.something.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Email {
	private String from;
	private String to;
	private String[] cc;
	private String[] bcc;
	private String subject;
	private String body;
	private String[] attachments;

	public Email(String to, String subject, String body) {
		this.from = "Holdings";
		this.to = to;
		this.subject = subject;
		this.body = body;
	}
}
