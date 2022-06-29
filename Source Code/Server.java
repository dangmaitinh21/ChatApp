import java.io.*;
import java.util.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;

import java.awt.*;

public class Server{
	Vector<String> listNameClients = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();
	Set<String> setListClientLogin = new HashSet<>();
	Socket client;
	JFrame mainFrame;
	JButton btnStart;
	JTextPane showProcess, listenClient;
	
	public int checkAccount(String username, String password) {
		try {
			File f = new File("accounts.txt");
			InputStream is = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(is, "UTF8");
			BufferedReader br = new BufferedReader(isr);
			String data, dataUsername, dataPassword;
			while((data = br.readLine()) != null) {
				dataUsername = data.substring(0,data.indexOf(","));
				dataPassword = data.substring(data.indexOf(",") + 1);
				if(username.equals(dataUsername)) {
					if(password.equals(dataPassword)) {
						return 1;
					} else {
						return 2;
					}
				}
			}
			br.close();
			isr.close();
			is.close();
			return 0;
		}catch(Exception e) {
			System.out.println("Lỗi kiểm tra tài khoản");
			System.out.println("Chi tiết: " + e.getMessage());
		}
		return 0;
	}
	
	public int checkActive(String account) {
    	for(String user : setListClientLogin) {
			if(user.equals(account)) {
				return 1;
			}
		}
    	return 0;
    }
	
	public int addAccount(String username, String password) throws IOException {
		File file = new File("accounts.txt");		
		if(file.exists()) {
			if(checkAccount(username, password) == 1) {
				return 0;
			} else if(checkAccount(username, password) == 2) {
				return 0;
			}
			OutputStream os = new FileOutputStream(file, true);
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF8");
			osw.write(username + "," + password + "\r\n");
			osw.close();
			os.close();
			return 1;
		}
		OutputStream os = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(os, "UTF8");
		osw.write(username + "," + password + "\r\n");
		osw.close();
		os.close();
		return 1;
	}
	
	private void appendString(JTextPane jtp, String str, Color c) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
		as = sc.addAttribute(as, StyleConstants.FontFamily, "Lucida Console");
		as = sc.addAttribute(as, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
		int len = jtp.getDocument().getLength();
		jtp.setCaretPosition(len);
		jtp.setCharacterAttributes(as, false);
		jtp.replaceSelection(str);
	}
	
	public void buildInterface() {
		mainFrame = new JFrame();
		mainFrame.setTitle("Server");
		JPanel mainPane = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10,0,5,0);
		JLabel processLabel = new JLabel("Tiến trình Server");
		mainPane.add(processLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(0,10,0,10);
		showProcess = new JTextPane();
		EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 10, 10));
		showProcess.setBorder(eb);
		showProcess.setMargin(new Insets(5, 5, 5, 5));
		JScrollPane scrollPaneProcess = new JScrollPane(showProcess);
		scrollPaneProcess.setPreferredSize(new Dimension(500,100));
		mainPane.add(scrollPaneProcess, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(10,0,5,0);
		JLabel listenLabel = new JLabel("Hoạt động của client");
		mainPane.add(listenLabel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.insets = new Insets(0,10,10,10);
		listenClient = new JTextPane();
		listenClient.setBorder(eb);
		listenClient.setMargin(new Insets(5, 5, 5, 5));
		listenClient.setEditable(false);
		JScrollPane scrollPaneListen = new JScrollPane(listenClient);
		scrollPaneListen.setPreferredSize(new Dimension(500,200));
		mainPane.add(scrollPaneListen, gbc);
		
		mainFrame.add(mainPane);
		
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setResizable(false);
		mainFrame.setVisible(true);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
	}
	
	
	
	public void process() throws Exception  {
		buildInterface();
		appendString(showProcess,"- Bắt đầu khởi động Server: Thành công!\n", Color.BLUE);
		ServerSocket server = new ServerSocket(3200);
		appendString(showProcess," [Khởi động Server thành công]\n", Color.BLUE);
		appendString(showProcess,"- Trạng thái Server: đang hoạt động...!\n", Color.BLUE);
		showProcess.setEditable(false);
		boolean acceptConnect;
		while(true) {
			do {
				client = server.accept();
				acceptConnect = false;
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8"));
				PrintWriter pw = new PrintWriter (new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8),true);
				String account = br.readLine();
				String type = account.substring(account.indexOf("|Type:") + 6);
				String username = account.substring(0,account.indexOf(","));
				String password = account.substring(account.indexOf(",") + 1, account.indexOf("|Type:"));
				if(type.equals("Login")) {
					if(checkAccount(username,password) == 1) {
						if(checkActive(username) == 1) {
							pw.println("|Username:" + username + "|State:Active");
						} else {
							setListClientLogin.add(username);
							pw.println("|Username:" + username + "|State:Success");
							acceptConnect = true;
						}
					} else if(checkAccount(username,password) == 2){
						pw.println("|Username:" + username + "|State:Forget");
					} else {
						pw.println("|Username:" + username + "|State:Failure");
					}
				} else if(type.equals("Logout")) {
				} else if(type.equals("SignUp")) {
					if(addAccount(username, password) == 1) {
						pw.println("|Username:" + username + "|State:SUSuccess");
					} else {
						pw.println("|Username:" + username + "|State:SUFailure");
					}		
				}
			}while(acceptConnect == false);
			HandleClient c = new HandleClient(client);
			clients.add(c);
			for (HandleClient cl : clients) {
				cl.getListClient();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		new Server().process();
	}
	
	public void broadcast(String cSour, String msg, String cDes)  {
		for (HandleClient client : clients) {
			if(cDes.equals("All")) {
				if (!client.getClientName().equals(cSour)) {
					client.sendMsg(cSour,msg);
				}
			} else {
				if(client.getClientName().equals(cDes)) {
					client.sendMsg(cSour, msg);
				}
			}
		}
	}
	
	public void logout(String cSour)  {
		for (HandleClient client : clients) {
			if (!client.getClientName().equals(cSour)) {
				client.sendLogout(cSour);
			}
		}
	}

	public void sendFileToDes(String cSour, String cDes, String URL, String extension) {
		for (HandleClient client : clients) {
			if (client.getClientName().equals(cDes)) {
				client.sendFile(cSour, cDes, URL, extension);
			}
		}
	}
	
	public void sendConfirmToDes(String cSour, String cDes, String confirm) {
		for (HandleClient client : clients) {
			if (client.getClientName().equals(cSour)) {
				client.sendConfirm(cSour, cDes, confirm);
			}
		}
	}
	
	class HandleClient extends Thread {
		String clientName = "";
		BufferedReader br;
		PrintWriter pw;
		public HandleClient(Socket client) throws Exception {
			br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8")) ;
			pw = new PrintWriter (new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8),true);
			clientName  = br.readLine();
			listenClient.setEditable(true);
			appendString(listenClient,"-> Tài khoản " + clientName + " vừa kết nối với Server!\n", Color.GREEN);
			listenClient.setEditable(false);
			listNameClients.add(clientName);
			start();
		}
		
		public void sendMsg(String cName,String msg) {
			pw.println(cName + ": " + msg + "|Type:Message");	
		}
		
		public void sendLogout(String cName) {
			pw.println(cName + "|Type:Logout");
		}
		
		public String getClientName() {  
			return clientName; 
		}
		
		public void getListClient() {
			for (String clientN : listNameClients) {
				pw.println(clientN + "|Type:ClientName");
			}
		}
		
		public void sendFile(String cSour, String cDes, String URL, String extension) {
			pw.println("|Source:" + cSour + "|Destination:" + cDes + "|URL:" + URL + "|Extension:" + extension + "|Type:File");
		}
		
		public void sendConfirm(String cSour, String cDes, String confirm) {
			pw.println("|Source:" + cSour + "|Destination:" + cDes + "|Confirm:" + confirm + "|Type:Confirm");
		}
		
		public void run() {
			String data = "", type = "", msg = "", cDes = "", dataSend, cSour, URL, extensionFile, confirm;
			try {
				while(true) {
					data = br.readLine();
					if(data.indexOf("|Type:") != -1) {
						type = data.substring(data.indexOf("|Type:") + 6);
					}
					if(type.equals("Message")) {
						msg = data.substring(0,data.indexOf("|Destination:"));
						cDes = data.substring(data.indexOf("|Destination:") + 13, data.indexOf("|Type:"));
						broadcast(clientName, msg, cDes);
					} else if (type.equals("Logout")) {
						dataSend = data.substring(0,data.indexOf("|Type:"));
						if(dataSend.equals("Client")) {
							listenClient.setEditable(true);
							appendString(listenClient,"<- Tài khoản " + clientName + " đã ngắt kết nối với Server!\n", Color.RED);
							listenClient.setEditable(false);
							logout(clientName);
							clients.remove(this);
							setListClientLogin.remove(clientName);
							listNameClients.remove(clientName);
							break;
						} else {
							break;
						}
					} else if (type.equals("File")) {
						cSour = data.substring(data.indexOf("|Source:") + 8, data.indexOf("|Destination:"));
						cDes = data.substring(data.indexOf("|Destination:") + 13, data.indexOf("|URL:"));
						URL = data.substring(data.indexOf("|URL:") + 5, data.indexOf("|Extension:"));
						extensionFile = data.substring(data.indexOf("|Extension:") + 11, data.indexOf("|Type:"));
						sendFileToDes(cSour, cDes, URL, extensionFile);
					} else if (type.equals("Confirm")) {
						cSour = data.substring(data.indexOf("|Source") + 8, data.indexOf("|Destination:"));
						cDes = data.substring(data.indexOf("|Destination:") + 13, data.indexOf("|Confirm:"));
						confirm = data.substring(data.indexOf("|Confirm:") + 9, data.indexOf("|Type:"));
						sendConfirmToDes(cSour, cDes, confirm);
					}
				}
			} 
			catch(Exception e) {
				System.out.println("Lỗi Thread");
				System.out.println("Chi tiết: " + e.getMessage());
			}
		}
	}
}