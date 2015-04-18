package cn.com.xd.jms;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

/**
 * @author wuliwei
 * 
 */
public class IMClient implements MessageListener, Runnable {
	private static final String exitCmd = "bye";
	private String user;
	private String password;
	private String url;
	private String sendTo;
	private boolean transacted;
	private Connection con;
	private Session session;
	private MessageConsumer consumer;
	private MessageProducer producer;

	/**
	 * @param user
	 * @param password
	 * @param url
	 * @param sendTo
	 * @param transacted
	 */
	public IMClient(String user, String password, String url, String sendTo,
			boolean transacted) {
		this.user = user;
		this.password = password;
		this.url = url;
		this.sendTo = sendTo;
		this.transacted = transacted;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (init() && receive() && send()) {
		}
		destroy();
	}

	private boolean init() {
		try {
			ConnectionFactory factory = new ActiveMQConnectionFactory(user,
					password, url);
			con = factory.createConnection();
			con.start();
			session = con.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return false;
	}

	private void destroy() {
		close(con, session, producer, consumer);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
	 */
	public void onMessage(Message msg) {
		try {
			if (msg instanceof TextMessage) {
				String text = ((TextMessage) msg).getText();
				System.out.println("received : " + text);
			}
			if (transacted) {
				session.commit();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean receive() {
		try {
			Destination destination = session.createQueue(user);
			consumer = session.createConsumer(destination);
			consumer.setMessageListener(this);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
		return false;
	}

	private boolean send() {
		BufferedReader br = null;
		try {
			Destination destination = session.createQueue(sendTo);
			producer = session.createProducer(destination);
			br = new BufferedReader(new InputStreamReader(System.in));
			String msg = null;
			TextMessage message = null;
			while (true) {
				try {
					msg = br.readLine();
					if (null != msg && !msg.isEmpty()) {
						message = session.createTextMessage(msg);
						producer.send(message);
						if (transacted) {
							session.commit();
						}
						if (exitCmd.equals(msg.toLowerCase())) {
							break;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	private void close(Connection con, Session session,
			MessageProducer producer, MessageConsumer consumer) {
		if (null != producer) {
			try {
				producer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (null != session) {
			try {
				session.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (null != con) {
			try {
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String user = args[0];// "im_client_test1";
		String password = args[1];// "im_client_test1";
		String url = args[2];// "tcp://localhost:61616";
		String sendTo = args[3];// "im_client_test2";
		boolean transacted = Boolean.valueOf(args[4]);// true;
		new Thread(new IMClient(user, password, url, sendTo, transacted))
				.start();
	}

}
