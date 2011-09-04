package com.orange.common.mail;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;

public class CommonMailSender {

	Logger log = Logger.getLogger(this.getClass().getName());
	
	String host = "smtp.163.com";
	String from = "gckj123@163.com";
	String to = "";
	String username = from;
	String password = "gckj123456";
	String title;
	Object content;

//	String host = "smtp.sina.com.cn";
//	String from = "gckj_dev@sina.com";
//	String to = "";
//	String username = from;
//	String password = "gckjdev123";

//	For Test
//	public static void main(String[] args) {
//		MailSender sm = new MailSender();
//		sm.send("gckj123@163.com", "www.google.com");
//	}
	public void send() {

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
			message.setSubject(title);
			message.setSentDate(new Date());
			
			BodyPart part = new MimeBodyPart();
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

public CommonMailSender(String to, String title, Object content) {
	super();
	this.to = to;
	this.title = title;
	this.content = content;
}


}

