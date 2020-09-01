package com.mry.mail;

import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.mry.thread.IThreadFactory;
import com.sun.mail.util.MailSSLSocketFactory;

public class SendEmailUtil {

	private static ThreadPoolExecutor tpool = new ThreadPoolExecutor(1, 20, 20L, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(), new IThreadFactory("SendEmail"));
	static String token = "nackyfkofssgbbbe";
	static String sender = "yanghu1919@qq.com";
	static String host = "smtp.qq.com";
	static Properties properties = new Properties();
	static Session sesiSession;
	static String subject = "Reset your password for mry";
	static {
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.ssl.enable", "true");
		MailSSLSocketFactory sf = null;
		try {
			sf = new MailSSLSocketFactory();
		} catch (GeneralSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		properties.put("mail.smtp.ssl.socketFactory", sf);
		sf.setTrustAllHosts(true);

		if (sesiSession == null) {
			init();
		}
	}

	private synchronized static void init() {
		if (sesiSession == null) {
			sesiSession = Session.getDefaultInstance(properties, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					// TODO Auto-generated method stub
					return new PasswordAuthentication(sender, token);
				}
			});
		}
	}

	public static void SendEmail(String receiver, String content) {
		tpool.execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					MimeMessage message = new MimeMessage(sesiSession);
					message.setFrom(new InternetAddress(sender));
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(receiver));
					message.setSubject(subject);
					message.setContent("<html lang='zh-CN'><head ><meta charset='utf-8'>"
							+ "</head><body>"
							+ "<a href='" + content + "'>this is a link for yor change password  ;it will effective in 3 min ... "+content+"</a></body></html>", "text/html;charset=utf-8");
					// message.setContent("this is a link for yor change password ;it will effective
					// in 10 min ... \n"+content,"text/html;charset=UTF-8");
					Transport.send(message);
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

	}

}
