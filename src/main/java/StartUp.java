import cn.com.xd.jms.IMClient;
import cn.com.xd.jms.IMGroup;

/**
 * @author wuliwei
 * 
 */
public class StartUp {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String user = args[0];// "im_client_test1";
		String password = args[1];// "im_client_test1";
		String url = args[2];// "tcp://localhost:61616";
		String sendTo = args[3];// "im_client_test2";
		boolean transacted = Boolean.valueOf(args[4]);// true;
		String type = args[5]; // "1";
		if ("1".equals(type)) {
			new Thread(new IMClient(user, password, url, sendTo, transacted))
					.start();
		} else {
			new Thread(new IMGroup(user, password, url, sendTo, transacted))
					.start();
		}
	}

}
