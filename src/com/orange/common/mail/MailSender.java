package com.orange.common.mail;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;

public class MailSender {
	
	Logger log = Logger.getLogger(this.getClass().getName());
	
	String host = "smtp.163.com";
	String from = "gckj123@163.com";
	String to = "1124090522@qq.com";
	String username = from;
	String password = "gckj123456";
//	String host = "smtp.sina.com.cn";
//	String from = "gckj123@sina.com";
//	String to = "jaja.422@163.com";
//	String username = from;
//	String password = "gckj123";

	public static void main(String[] args) {
		MailSender sm = new MailSender();
		sm.send("1124090522@qq.com", "www.google.com");
	}
	
	public void send(String email, String confirmUrl) {

		try {
			Properties props = new Properties();

			props.put("mail.smtp.host", host);
			props.put("mail.smtp.auth", "true"); 

			Authenticator auth = new PopupAuthenticator(username, password);
			Session session = Session.getDefaultInstance(props, auth);

			// watch the mail commands go by to the mail server
			session.setDebug(true);

			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			message.setSubject("�̳ȿƼ�ȷ��ע��");
			message.setSentDate(new Date());
			
			BodyPart part = new MimeBodyPart();
			String content = "��ã�<br> ����������������ע�᣺<br><a href='"    
							+ confirmUrl + "'>"+ confirmUrl + "</a><br>" 
							+ "������������޷�������뽫����ĵ�ַ���Ƶ����������� ";
			part.setContent(content, "text/html;charset=gb2312");
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(part);
			message.setContent(mp);

			message.saveChanges();
			Transport transport = session.getTransport("smtp");
			transport.connect(host, username, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			System.out.println("Send mail successfully");
		} catch (Exception e) {
			System.err.println("Send mail failure:" + e.getMessage());
			e.printStackTrace(System.err);
		}

	}
}

class PopupAuthenticator extends Authenticator {
	private String username;
	private String password;

	public PopupAuthenticator(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(this.username, this.password);
	}
}
