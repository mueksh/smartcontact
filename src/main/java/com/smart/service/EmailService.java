package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	//this message responsible to send mail
		public boolean sendEmail(String subject, String message,String to) {
			
			boolean f=false;
			String from="neetu8475875660@gmail.com";
			//variable for mail
			
		
			
		
		//
		Properties properties= System.getProperties();
		System.out.println("PROPERTIES "+properties);
		
		//setting important information to properties object
		  properties.put("mail.smtp.auth", "true");
		   properties.put("mail.smtp.starttls.enable", "true");
		   properties.put("mail.smtp.host", "smtp.gmail.com");
		   properties.put("mail.smtp.port", "587");
		
		//step1: to get the session object
		Session session=Session.getInstance(properties,new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("neetu8475875660@gmail.com", "7618660727");
				
			}
			
		});
		session.setDebug(true);
		
		//step 2: compose the message(text,multi,media)
				MimeMessage m=new MimeMessage(session);
				
				try {
				//from mail
				m.setFrom(from);
				
				//adding recipient
				m.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
				
				//adding subject to message
				m.setSubject(subject);
				
				//text to message
				m.setText(message);
				m.setContent(message,"text/html");
				
				//step3: send the message using transport
				Transport.send(m);
				
				System.out.println("Send success.............");
				f=true;
				
				}catch (Exception e) {

	              e.printStackTrace();
				}
				return f;
		
		
		}
		
	
	

}
