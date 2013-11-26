package org.one.stone.soup.javascript.helper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class MailService implements TransportListener{

    public static final String VERSION="Mailer 0.1";
    private String user;
    private String password;
    private String smtpHostName;
    private String smtpPort = "465";
    private static final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";   
    
    //Add this
    //https://code.google.com/p/open-forum/source/browse/trunk/Wiki%20POP3%20Client/org/one/stone/soup/wiki/pop3/client/WikiPOP3Client.java
    
    public MailService() {
    }
    
    public MailService(String smtpHostName,String user,String password) {
    	this.smtpHostName = smtpHostName;
    	this.user = user;
    	this.password = password;
    }
    
    public class MailerThread implements Runnable
    { 	
        private String mailFrom;
        private String[] mailTo;
        private String subject;
        private String textMessage;
        private String htmlMessage;
        private String attachmentFileName;
        private String attachmentMimeType;
        private String attachmentURL;
       
        private MailerThread(String mailFrom,String[] mailTo,String subject,String htmlMessage,String textMessage,String attachmentFileName,String attachmentMimeType,String attachmentURL)
        {
            this.mailFrom=mailFrom;
            this.mailTo=mailTo;
            this.subject=subject;
            this.htmlMessage=htmlMessage;
            this.textMessage=textMessage;
            this.attachmentMimeType=attachmentMimeType;
            this.attachmentFileName=attachmentFileName;
            this.attachmentURL=attachmentURL;
           
            Thread thread = new Thread(this,"Sending Mail: "+subject);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        public void run()
        {
            int retry = 0;
            while(retry<5)
            {
                try{
                    _sendMail(mailTo,mailFrom,subject,htmlMessage,textMessage,attachmentFileName,attachmentMimeType,attachmentURL);
                    break;
                }
                catch(Throwable th)
                {
                    th.printStackTrace();
                    try{ Thread.sleep(5000); }catch(Exception e){}
                    retry++;
                }
            }
            try{ Thread.sleep(1000); }catch(Exception e){}
        }       
    }

    public void sendMail(String mailFrom,String[] mailTo,String subject,String textMessage) throws Exception //IOException,AuthenticationException
    {
        new MailerThread(mailFrom,mailTo,subject,null,textMessage,null,null,null);   	
    }
    
    public void sendMail(String mailFrom,String[] mailTo,String subject,String htmlMessage,String textMessage,String attachmentFileName,String attachmentMimeType,String attachmentURL) throws Exception //IOException,AuthenticationException
    {
        new MailerThread(mailFrom,mailTo,subject,htmlMessage,textMessage,attachmentFileName,attachmentMimeType,attachmentURL);
    }
   
    public void _sendMail(String[] mailTo,String mailFrom,String subject,String htmlMessage,String textMessage,String attachmentFileName,String attachmentMimeType,String attachmentURL) throws Exception //IOException,AuthenticationException
    {                  
        sendSSLMessage(mailTo,subject,htmlMessage,textMessage,attachmentFileName,attachmentMimeType,attachmentURL,mailFrom);
    }
   
    private void sendSSLMessage(String recipients[], String subject,String htmlMessage,String textMessage,String attachmentFileName,String attachmentMimeType,String attachmentURL, String from) throws MalformedURLException,IOException, MessagingException {
        boolean debug = true;
       
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHostName);
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "false");
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.socketFactory.port", smtpPort);
        props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.put("mail.smtp.socketFactory.fallback", "false");
       
        Session session = Session.getDefaultInstance(props,
        new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user,password);
                }
            }
        );
       
        session.setDebug(debug);
       
        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(from);
        msg.setFrom(addressFrom);
        msg.setReplyTo(new Address[]{addressFrom});
       
        InternetAddress[] addressTo = new InternetAddress[recipients.length];
        for (int i = 0; i < recipients.length; i++) {
            addressTo[i] = new InternetAddress(recipients[i]);
        }
        //msg.setRecipients(Message.RecipientType.BCC, addressTo);
   
        MimeMultipart multipart = new MimeMultipart("alternative");
        
        if(attachmentURL!=null) {
	        MimeBodyPart filePart = new MimeBodyPart();
	        filePart.setFileName( attachmentFileName );
	        
			URL url = new URL(attachmentURL);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty( "User-Agent","OpenForum");
	        
	        ByteArrayDataSource ds = new ByteArrayDataSource(connection.getInputStream(), attachmentMimeType);
	        filePart.setDataHandler(new DataHandler(ds));
	        multipart.addBodyPart(filePart);
        }
        
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(textMessage, "text/plain");
        multipart.addBodyPart(textPart);
        
        if(htmlMessage!=null) {
	        MimeBodyPart htmlPart = new MimeBodyPart();
	        String htmlData = htmlMessage;
	        htmlPart.setContent(htmlData, "text/html");
	        multipart.addBodyPart(htmlPart);
        }
        
        // Setting the Subject and Content Type
        msg.setSubject(subject);
        msg.setContent(multipart);
        
        Transport transport = session.getTransport(addressTo[0]);
        transport.addTransportListener(this);
        transport.connect();
        // Sending Message
        for(Address to: addressTo) {
        	try{
        	//msg.setRecipients(RecipientType.TO,new Address[]{to});
        	//Transport.send(msg);
        		msg.setRecipients(RecipientType.TO,new Address[]{to});
        		transport.sendMessage(msg,new Address[]{to});
        	} catch (Exception e) {
        		//messageSendFailed(e,msg);
        		transport.close();
                transport.connect();
        	}
        }
        transport.close();
    }

	public String getSmtpHostName() {
		return smtpHostName;
	}

	public void setSmtpHostName(String smtpHostName) {
		this.smtpHostName = smtpHostName;
	}

	public String getSmtpPort() {
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public void messageDelivered(TransportEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageNotDelivered(TransportEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messagePartiallyDelivered(TransportEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}

