import java.io.*;
import java.util.*;
import java.util.List;
import java.net.*;
import java.nio.charset.StandardCharsets;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.*;

public class  Client extends JFrame implements ActionListener {
	String clientName;
	PrintWriter pw;
	BufferedReader br;
	JFrame loginFrame, signUpFrame, clientFrame;
	JTextPane  taMessages, taClient;
	JTextField tfInput, tfUsername, tfPassword;
	JButton btnSend,btnExit, btnLoginLFrame, btnSignUpLFrame, btnSignUpSUFrame, btnLoginSUFrame, btnSendFile;
	Socket client;
	JComboBox<String> jcb;
	Set<String> setListClient;
    
	public Client(String clientName, InetAddress serverName) throws Exception {		
		super(clientName);
		int sigupAccount = JOptionPane.NO_OPTION;
		do {
			client  = new Socket(serverName,3200);
			br = new BufferedReader(new InputStreamReader(client.getInputStream(), "UTF-8")) ;
			pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8),true);
			if(sigupAccount == JOptionPane.YES_OPTION) {
				buildInterfaceSignUp("","");
			} else {
				buildInterfaceLogin("","");
			}
			String stateLogin = br.readLine();
			String userName = stateLogin.substring(stateLogin.indexOf("|Username:") + 10, stateLogin.indexOf("|State:"));
			String state = stateLogin.substring(stateLogin.indexOf("|State:") + 7);
			if(state.equals("Success")) {
				loginFrame.dispose();
				clientName = userName;
				this.clientName = clientName;
				pw.println(clientName);
				buildInterfaceClient();
				new MessagesThread().start();
			} else if (state.equals("Forget")){
				loginFrame.dispose();
				client.close();
				JOptionPane.showMessageDialog(null, "Tên đăng nhập hoặc mật khẩu không đúng");
			} else if (state.equals("SUSuccess")) {
				signUpFrame.dispose();
				JOptionPane.showMessageDialog(null, "Đăng ký tài khoản thành công!");
			} else if (state.equals("SUFailure")) {
				signUpFrame.dispose();
				JOptionPane.showMessageDialog(null, "Tài khoản này đã tồn tại!");
			} else if (state.equals("Failure")){
				loginFrame.dispose();
				client.close();
				JOptionPane.showMessageDialog(null, "Bạn chưa có tài khoản");
				sigupAccount = JOptionPane.showConfirmDialog (null, "Bạn có muốn đăng ký tài khoản hay không?","?",JOptionPane.YES_NO_OPTION);
			} else if (state.equals("Active")) {
				loginFrame.dispose();
				client.close();
				JOptionPane.showMessageDialog(null, "Tài khoản này đang hoạt động!");
			}
		}while(clientName.equals(""));
	}
	
	public String getFileExtension(File file) {
		String fileName = file.getName();
		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			return fileName.substring(fileName.lastIndexOf(".") + 1);
		} else {
			return "";
		}
	}
	
	private void appendString(JTextPane jtp, String str, Color c) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet as = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);
		as = sc.addAttribute(as, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
		int len = jtp.getDocument().getLength();
		jtp.setCaretPosition(len);
		jtp.setCharacterAttributes(as, false);
		jtp.replaceSelection(str);
	}
	
	public void buildInterfaceSignUp(String username, String password) {
		signUpFrame = new JFrame();
		signUpFrame.setTitle("Đăng Ký");
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10,10,10,5);
		JLabel usernameTitle = new JLabel("Tên đăng nhập: ");
		panel.add(usernameTitle, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5,10,10,10);
		JLabel passwordTitle = new JLabel("Mật khẩu: ");
		panel.add(passwordTitle, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(10,10,10,10);
		tfUsername = new JTextField(20);
		panel.add(tfUsername, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		tfPassword = new JTextField(20);
		panel.add(tfPassword, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		btnLoginSUFrame = new JButton("Quay lại");
		btnLoginSUFrame.setPreferredSize(new Dimension(100,25));
		panel.add(btnLoginSUFrame, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		btnSignUpSUFrame = new JButton("Đăng Ký");
		btnSignUpSUFrame.setPreferredSize(new Dimension(100,25));
		panel.add(btnSignUpSUFrame, gbc);
		
		signUpFrame.add(panel,"Center");
		
		btnSignUpSUFrame.addActionListener(this);
		btnLoginSUFrame.addActionListener(this);
		signUpFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				pw.println(",|Type:Logout");
				System.exit(0);
			}
		});
		
		signUpFrame.setResizable(false);
		signUpFrame.setVisible(true);
		signUpFrame.pack();
		signUpFrame.setLocationRelativeTo(null);
	}
	
	public void buildInterfaceLogin(String username, String password) {
		loginFrame = new JFrame();
		loginFrame.setTitle("Đăng nhập");
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(10,10,10,5);
		JLabel usernameTitle = new JLabel("Tên đăng nhập: ");
		panel.add(usernameTitle, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.insets = new Insets(5,10,10,10);
		JLabel passwordTitle = new JLabel("Mật khẩu: ");
		panel.add(passwordTitle, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(10,10,10,10);
		tfUsername = new JTextField(20);
		panel.add(tfUsername, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 1;
		tfPassword = new JTextField(20);
		panel.add(tfPassword, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		btnSignUpLFrame = new JButton("Đăng Ký");
		btnSignUpLFrame.setPreferredSize(new Dimension(100,25));
		panel.add(btnSignUpLFrame, gbc);
		
		gbc.gridx = 2;
		gbc.gridy = 2;
		btnLoginLFrame = new JButton("Đăng Nhập");
		btnLoginLFrame.setPreferredSize(new Dimension(100,25));
		panel.add(btnLoginLFrame, gbc);
		
		loginFrame.add(panel,"Center");
		
		btnSignUpLFrame.addActionListener(this);
		btnLoginLFrame.addActionListener(this);
		loginFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				pw.println(",|Type:Logout");
				System.exit(0);
			}
		});

		loginFrame.setResizable(false);
		loginFrame.setVisible(true);
		loginFrame.pack();
		loginFrame.setLocationRelativeTo(null);
	}
	
	public void buildInterfaceClient() {
		clientFrame = new JFrame();
		setListClient = new HashSet<>();
		Font font = new Font("SansSerif", Font.PLAIN, 15);
		
		JPanel panelHeader = new JPanel(new GridBagLayout());
		GridBagConstraints gbcHeader = new GridBagConstraints();
		gbcHeader.anchor = GridBagConstraints.WEST;
		gbcHeader.fill = GridBagConstraints.HORIZONTAL;
		gbcHeader.weightx = 1;
		gbcHeader.insets = new Insets(10,10,5,0);
		gbcHeader.gridx = 0;
		gbcHeader.gridy = 0;
		JLabel conversationTitle = new JLabel("Phòng chat");
		panelHeader.add(conversationTitle, gbcHeader);
		
		gbcHeader.gridx = 1;
		gbcHeader.gridy = 0;
		gbcHeader.insets = new Insets(10,500,5,10);
		JLabel clientStateHeader = new JLabel("User đang online");
		panelHeader.add(clientStateHeader, gbcHeader);
		
		JPanel panelBody = new JPanel(new GridBagLayout());
		GridBagConstraints gbcBody = new GridBagConstraints();
		gbcBody.gridx = 0;
		gbcBody.gridy = 0;
		gbcBody.insets = new Insets(0,10,5,5);
		taMessages = new JTextPane();
		taMessages.setPreferredSize(new Dimension(550, 400));
		taMessages.setFont(font);
		taMessages.setEditable(false);
		EmptyBorder eb = new EmptyBorder(new Insets(10, 10, 10, 10));
		taMessages.setBorder(eb);
		taMessages.setMargin(new Insets(5,5,5,5));
		JPanel noWrapPanelMess = new JPanel(new BorderLayout());
		noWrapPanelMess.add(taMessages);
		JScrollPane scrollPane = new JScrollPane(noWrapPanelMess, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panelBody.add(scrollPane, gbcBody);
		
		
		gbcBody.gridx = 1;
		gbcBody.gridy = 0;
		gbcBody.insets = new Insets(0,10,5,10);
		taClient = new JTextPane();
		taClient.setPreferredSize(new Dimension(200, 400));
		taClient.setFont(font);
		taClient.setEditable(false);
		taClient.setBorder(eb);
		taClient.setMargin(new Insets(5,5,5,5));
		taClient.setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.GRAY), eb));
		JPanel noWrapPanelClient = new JPanel(new BorderLayout());
		noWrapPanelClient.add(taClient);
		panelBody.add(noWrapPanelClient, gbcBody);
		
		JPanel panelFooter = new JPanel(new GridBagLayout());
		GridBagConstraints gbcFooter = new GridBagConstraints();
		gbcFooter.anchor = GridBagConstraints.WEST;
		gbcFooter.fill = GridBagConstraints.HORIZONTAL;
		gbcFooter.weightx = 1;
		gbcFooter.gridx = 0;
		gbcFooter.gridy = 0;
		gbcFooter.insets = new Insets(10,10,0,0);
		JLabel entryTitle = new JLabel("Nhập tin nhắn gửi:");
		panelFooter.add(entryTitle, gbcFooter);
		
		gbcFooter.gridx = 1;
		gbcFooter.gridy = 0;
		JLabel clientSelectionTitle = new JLabel("Đến User:");
		panelFooter.add(clientSelectionTitle, gbcFooter);
		
		gbcFooter.gridx = 0;
		gbcFooter.gridy = 1;
		gbcFooter.insets = new Insets(5,10,10,0);
		tfInput  = new JTextField(40);
		tfInput.setFont(font);
		panelFooter.add(tfInput, gbcFooter);
	
		gbcFooter.gridx = 1;
		gbcFooter.gridy = 1;
		gbcFooter.insets = new Insets(5,10,10,0);
		jcb = new JComboBox();
		jcb.setPreferredSize(new Dimension(50, 25));
		panelFooter.add(jcb, gbcFooter);
		
		gbcFooter.gridx = 2;
		gbcFooter.gridy = 1;
		gbcFooter.insets = new Insets(5,5,10,10);
		btnSend = new JButton("Gửi");
		btnSend.setFocusable(false);
		btnSend.setPreferredSize(new Dimension(50, 25));
		panelFooter.add(btnSend, gbcFooter);
		
		gbcFooter.gridx = 2;
		gbcFooter.gridy = 2;
		gbcFooter.insets = new Insets(5,5,10,10);
		btnSendFile = new JButton("Gửi File");
		btnSendFile.setFocusable(false);
		btnSendFile.setPreferredSize(new Dimension(50, 25));
		panelFooter.add(btnSendFile, gbcFooter);
		
		clientFrame.add(panelHeader,"North");
		clientFrame.add(panelBody,"West");
		clientFrame.add(panelFooter,"South");
		
		btnSendFile.addActionListener(this);
		btnSend.addActionListener(this);
		clientFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				pw.println("Client|Type:Logout");
				System.exit(0);
			}
		});
		
		clientFrame.setTitle(clientName);
		clientFrame.setResizable(false);
		clientFrame.setSize(500,300);
		clientFrame.setVisible(true);
		clientFrame.pack();
		clientFrame.setLocationRelativeTo(null);
    }
    
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnSend) {
			if (tfInput.getText().equals("")) {
				JOptionPane.showMessageDialog(clientFrame, "Vui lòng nhập nội dung trước khi gửi");
			} else {
				taMessages.setEditable(true);
				appendString(taMessages, "Bạn: " + tfInput.getText() + "\n", Color.BLACK);
				taMessages.setEditable(false);
				pw.println(tfInput.getText() + "|Destination:" + jcb.getSelectedItem() + "|Type:Message");
				tfInput.setText("");
			}
		} else if(e.getSource() == btnLoginLFrame) {
			if(tfUsername.getText().equals("")) {
				JOptionPane.showMessageDialog(loginFrame, "Tên đăng nhập không được để trống!");
			} else if(tfPassword.getText().equals("")) {
				JOptionPane.showMessageDialog(loginFrame, "Mật khẩu không được để trống!");
			} else {
				pw.println(tfUsername.getText() + "," + tfPassword.getText() + "|Type:Login");
			}
		} else if(e.getSource() == btnSignUpLFrame) {
			loginFrame.dispose();
			buildInterfaceSignUp("","");
		} else if(e.getSource() == btnLoginSUFrame) {
			signUpFrame.dispose();
			buildInterfaceLogin("","");
		} else if(e.getSource() == btnSignUpSUFrame) {
			if(tfUsername.getText().equals("")) {
				JOptionPane.showMessageDialog(signUpFrame, "Tên đăng nhập không được để trống!");
			} else if(tfPassword.getText().equals("")) {
				JOptionPane.showMessageDialog(signUpFrame, "Mật khẩu không được để trống!");
			} else {
				pw.println(tfUsername.getText() + "," + tfPassword.getText() + "|Type:SignUp");
			}
		} else if(e.getSource() == btnSendFile) {
			if(jcb.getSelectedItem().equals("All")) {
				JOptionPane.showMessageDialog(clientFrame, "Chỉ cho phép gửi đến 1 tài khoản");
			} else {
				try {
					UIManager.put("FileChooser.openButtonText","Chọn");
					UIManager.put("FileChooser.cancelButtonText","Hủy bỏ");
					UIManager.put("FileChooser.openDialogTitleText", "Chọn file");
					UIManager.put("FileChooser.lookInLabelText", "Đường dẫn:");
					UIManager.put("FileChooser.fileNameHeaderText", "Tên");
					UIManager.put("FileChooser.fileSizeHeaderText", "Dung lượng");
					UIManager.put("FileChooser.fileTypeHeaderText", "Loại");
					UIManager.put("FileChooser.fileDateHeaderText", "Thời gian tạo");
					UIManager.put("FileChooser.fileNameLabelText", "Tên file:");
					UIManager.put("FileChooser.filesOfTypeLabelText", "Loại file:");
					UIManager.put("FileChooser.newFolderToolTipText", "Tạo folder mới");
					UIManager.put("FileChooser.homeFolderToolTipText", "Desktop");
					UIManager.put("FileChooser.upFolderToolTipText", "Quay lại");
					UIManager.put("FileChooser.listViewButtonToolTipText", "Xem dưới dạng danh sách");
					UIManager.put("FileChooser.detailsViewButtonToolTipText", "Xem dưới dạng chi tiết");
					JFileChooser fileChooser  = new JFileChooser();
					SwingUtilities.updateComponentTreeUI(fileChooser);
					fileChooser.setMultiSelectionEnabled(false);
					int choose = fileChooser.showOpenDialog(clientFrame);
					if(choose == JFileChooser.APPROVE_OPTION) {
						pw.println("|Source:" + clientName 
								+ "|Destination:" + jcb.getSelectedItem()
								+ "|URL:" + fileChooser.getSelectedFile().getAbsolutePath()
								+ "|Extension:" + getFileExtension(fileChooser.getSelectedFile())
								+ "|Type:File"
								);
					}
				} catch(Exception ex) {
					System.out.println("Lỗi sự kiện");
					System.out.println("Chi tiết: " + ex.getMessage());
				}
			}
		}
	}
    
	public static void main(String[] args) throws UnknownHostException {
		InetAddress serverName = InetAddress.getLocalHost();  
		try {
			new Client("", serverName);
		} catch(Exception e) {
			if(e.getMessage().equals("Connection refused: connect")) {
				JOptionPane.showMessageDialog(null, "Server chưa được bật!");
			} else {
				System.out.println("Lỗi tạo client");
			    System.out.println("Chi tiết: " + e.getMessage());
			}	
		}
	}
	
	public void processData(String data) throws IOException {
		String dataReceived = data.substring(0,data.indexOf("|Type:"));
		String type = data.substring(data.indexOf("|Type:") + 6);
		if(type.equals("Message")) {
			taMessages.setEditable(true);
			appendString(taMessages,dataReceived + "\n",Color.BLUE);
			taMessages.setEditable(false);
		}
		else if(type.equals("ClientName")) {
			setListClient.add("All");
			if(!dataReceived.equals(clientName)){
				setListClient.add(dataReceived);
			}
			taClient.setText(null);
			jcb.removeAllItems();
			for(String client : setListClient) {
				jcb.addItem(client);
				if(!client.equals("All")) {
					taClient.setEditable(true);
					appendString(taClient," - " + client + " đang online\n", Color.MAGENTA);
					taClient.setEditable(false);
				}
			}
		}
		else if(type.equals("Logout")) {
			List<String> removeClientList = new ArrayList<>();
			for(String client : setListClient) {
				if(client.equals(dataReceived)) {
					removeClientList.add(client);
				} 
			}
			setListClient.removeAll(removeClientList);
			jcb.removeAllItems();
			for(String client : setListClient) {
				jcb.addItem(client);
			}
		} else if(type.equals("File")) {
			String cSour = dataReceived.substring(dataReceived.indexOf("|Source:") + 8, dataReceived.indexOf("|Destination:"));
			String cDes = dataReceived.substring(dataReceived.indexOf("|Destination:") + 13, dataReceived.indexOf("|URL:"));
			String URL = dataReceived.substring(dataReceived.indexOf("|URL:") + 5, dataReceived.indexOf("|Extension:"));
			String extension = dataReceived.substring(dataReceived.indexOf("|Extension:") + 11);
			clientFrame.setVisible(true);
			int confirmDownload = JOptionPane.showConfirmDialog(clientFrame, cSour + " vừa gửi cho bạn 1 file, bạn có muốn nhận nó không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
			if(confirmDownload == 0) {
				UIManager.put("FileChooser.saveButtonText","Lưu");
				UIManager.put("FileChooser.cancelButtonText","Hủy bỏ");
				UIManager.put("FileChooser.saveDialogTitleText", "Lưu file");
				UIManager.put("FileChooser.lookInLabelText", "Đường dẫn:");
				UIManager.put("FileChooser.fileNameHeaderText", "Tên");
				UIManager.put("FileChooser.fileSizeHeaderText", "Dung lượng");
				UIManager.put("FileChooser.fileTypeHeaderText", "Loại");
				UIManager.put("FileChooser.fileDateHeaderText", "Thời gian tạo");
				UIManager.put("FileChooser.fileNameLabelText", "Tên file:");
				UIManager.put("FileChooser.filesOfTypeLabelText", "Loại file:");
				UIManager.put("FileChooser.newFolderToolTipText", "Tạo folder mới");
				UIManager.put("FileChooser.homeFolderToolTipText", "Desktop");
				UIManager.put("FileChooser.upFolderToolTipText", "Quay lại");
				UIManager.put("FileChooser.listViewButtonToolTipText", "Xem dưới dạng danh sách");
				UIManager.put("FileChooser.detailsViewButtonToolTipText", "Xem dưới dạng chi tiết");
				JFileChooser fileChooser  = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter("." + extension, extension);
				SwingUtilities.updateComponentTreeUI(fileChooser);
				fileChooser.setAcceptAllFileFilterUsed(false);
				fileChooser.setFileFilter(filter);
				int choose = fileChooser.showSaveDialog(clientFrame);
				if(choose == JFileChooser.APPROVE_OPTION) {
					File file = new File(URL);
					UploadFile uploadFile = new UploadFile(file);
					Thread uploadThread = new Thread(uploadFile);
					uploadThread.start();
					pw.println("|Source:" + cSour + "|Destination:" + cDes + "|Confirm:Yes" + "|Type:Confirm");
					DownloadFile downloadFile = new DownloadFile(fileChooser.getSelectedFile().getPath(), extension);
					Thread downloadThread = new Thread(downloadFile);
					downloadThread.start();
					taMessages.setEditable(true);
					appendString(taMessages,"Bạn vừa nhận được 1 file từ " + cSour + "\n",Color.BLUE);
					taMessages.setEditable(false);
				} else {
					pw.println("|Source:" + cSour + "|Destination:" + cDes + "|Confirm:No" + "|Type:Confirm");
				}
			} else {
				pw.println("|Source:" + cSour + "|Destination:" + cDes + "|Confirm:No" + "|Type:Confirm");
			}
		} else if(type.equals("Confirm")) {
//			String cSour = dataReceived.substring(dataReceived.indexOf("|Source:") + 8, dataReceived.indexOf("|Destination:"));
			String cDes = dataReceived.substring(dataReceived.indexOf("|Destination:") + 13, dataReceived.indexOf("|Confirm:"));
			String confirm = dataReceived.substring(dataReceived.indexOf("|Confirm:") + 9);
			if(confirm.equals("Yes")) {
				clientFrame.setVisible(true);
				JOptionPane.showMessageDialog(clientFrame, cDes + " đã nhận được file của bạn!");
				taMessages.setEditable(true);
				appendString(taMessages,"Gửi file thành công!\n",Color.GREEN);
				taMessages.setEditable(false);
			} else {
				clientFrame.setVisible(true);
				JOptionPane.showMessageDialog(clientFrame, cDes + " vừa từ chối nhận file của bạn!");
				taMessages.setEditable(true);
				appendString(taMessages,"Gửi file thất bại!\n",Color.RED);
				taMessages.setEditable(false);
			}
		}
	}
	
	class  MessagesThread extends Thread {
		public void run() {
			String data;
			try {
				while(true) {
					data = br.readLine();
					processData(data);
				}
			} catch(Exception e) {
				if(e.getMessage().equals("Connection reset")) {
					JOptionPane.showMessageDialog(clientFrame, "Server đã đóng!");
					clientFrame.dispose();
				} else {
					System.out.println("Lỗi Thread tin nhắn");
					System.out.println("Chi tiết: " + e.getMessage());
				}
			}
		}
	}
	
	class DownloadFile extends Thread implements Runnable{
		private Socket sock;
		private String path = "";
		private InputStream is;
		private FileOutputStream fos;
		private InetAddress ip;
		public DownloadFile(String path, String extension) {
			this.path = path + "." + extension;
		}

		@Override
		public void run() {
			try {
				ip = InetAddress.getLocalHost();
				sock = new Socket(ip, 3500);
				is = sock.getInputStream();
				fos = new FileOutputStream(path);
				byte[] buffer = new byte[5012];
				int read;

				while ((read = is.read(buffer)) >= 0) {
					fos.write(buffer, 0, read);
				}
				fos.flush();
				if (fos != null) {
					fos.close();
				}
				if (is != null) {
					is.close();
				}
				if (sock != null) {
					sock.close();
				}
			} catch (Exception e) {
				System.out.println("Lỗi Thread download");
				System.out.println("Chi tiết: " + e.getMessage());
			}
		}
	}
	
	class UploadFile extends Thread implements Runnable {
		private Socket sock;
		private FileInputStream fis;
		private OutputStream os;
		private File file;
		private ServerSocket server;

		public UploadFile(File file) {
			try {
				this.file = file;
				server = new ServerSocket(3500);
			} catch (Exception e) {
				System.out.println("Lỗi tạo server socket trên Thread upload");
				System.out.println("Chi tiết: " + e.getMessage());
			}
		}

		@Override
		public void run() {
			try {
				sock = server.accept();
				os = sock.getOutputStream();
				fis = new FileInputStream(file);
				byte[] buffer = new byte[5120];
				int read;

				while ((read = fis.read(buffer)) >= 0) {
					os.write(buffer, 0, read);
				}
				os.flush();
				if (fis != null) {
					fis.close();
				}
				if (os != null) {
					os.close();
				}
				if (sock != null) {
					sock.close();
				}
				if (server!=null) {
					server.close();
				}
			} catch (Exception e) {
				System.out.println("Lỗi Thread upload");
				System.out.println("Chi tiết: " + e.getMessage());
			}
		}

	}
}


