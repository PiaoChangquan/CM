import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Random;

import javax.swing.*;
import javax.swing.text.*;

import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import Esper.ChatEventType;
import kr.ac.konkuk.ccslab.cm.*;

public class CMWinClient extends JFrame {
	// private JTextArea m_outTextArea;
	private JTextPane m_outTextPane;
	private JTextField m_inTextField;
	private JButton m_startStopButton;
	private MyMouseListener cmMouseListener;
	private CMClientStub m_clientStub;
	private CMWinClientEventHandler m_eventHandler;

	CMWinClient() {
		MyKeyListener cmKeyListener = new MyKeyListener();
		MyActionListener cmActionListener = new MyActionListener();
		cmMouseListener = new MyMouseListener();
		setTitle("CM Client");
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new BorderLayout());

		m_outTextPane = new JTextPane();
		m_outTextPane.setEditable(false);

		StyledDocument doc = m_outTextPane.getStyledDocument();
		addStylesToDocument(doc);
		add(m_outTextPane, BorderLayout.CENTER);
		JScrollPane scroll = new JScrollPane(m_outTextPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(scroll);

		m_inTextField = new JTextField();
		m_inTextField.addKeyListener(cmKeyListener);
		add(m_inTextField, BorderLayout.SOUTH);

		JPanel topButtonPanel = new JPanel();
		topButtonPanel.setLayout(new FlowLayout());
		add(topButtonPanel, BorderLayout.NORTH);

		m_startStopButton = new JButton("Start Client CM");
		m_startStopButton.addActionListener(cmActionListener);
		// add(startStopButton, BorderLayout.NORTH);
		topButtonPanel.add(m_startStopButton);

		setVisible(true);

		m_clientStub = new CMClientStub();
		m_eventHandler = new CMWinClientEventHandler(m_clientStub, this);
	}

	private void addStylesToDocument(StyledDocument doc) {
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regularStyle = doc.addStyle("regular", defStyle);
		StyleConstants.setFontFamily(regularStyle, "SansSerif");

		Style boldStyle = doc.addStyle("bold", defStyle);
		StyleConstants.setBold(boldStyle, true);

		Style linkStyle = doc.addStyle("link", defStyle);
		StyleConstants.setForeground(linkStyle, Color.BLUE);
		StyleConstants.setUnderline(linkStyle, true);
	}

	public CMClientStub getClientStub() {
		return m_clientStub;
	}

	public CMWinClientEventHandler getClientEventHandler() {
		return m_eventHandler;
	}

	public void printMessage(String strText) {
		/*
		 * m_outTextArea.append(strText);
		 * m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength(
		 * ));
		 */
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, null);
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

	public void printStyledMessage(String strText, String strStyleName) {
		StyledDocument doc = m_outTextPane.getStyledDocument();
		try {
			doc.insertString(doc.getLength(), strText, doc.getStyle(strStyleName));
			m_outTextPane.setCaretPosition(m_outTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return;
	}

	public void printImage(String strPath) {
		int nTextPaneWidth = m_outTextPane.getWidth();
		int nImageWidth;
		int nImageHeight;
		int nNewWidth;
		int nNewHeight;

		File f = new File(strPath);
		if (!f.exists()) {
			printMessage(strPath + "\n");
			return;
		}

		ImageIcon icon = new ImageIcon(strPath);
		Image image = icon.getImage();
		nImageWidth = image.getWidth(m_outTextPane);
		nImageHeight = image.getHeight(m_outTextPane);

		if (nImageWidth > nTextPaneWidth / 2) {
			nNewWidth = nTextPaneWidth / 2;
			float fRate = (float) nNewWidth / (float) nImageWidth;
			nNewHeight = (int) (nImageHeight * fRate);
			Image newImage = image.getScaledInstance(nNewWidth, nNewHeight, java.awt.Image.SCALE_SMOOTH);
			icon = new ImageIcon(newImage);
		}

		m_outTextPane.insertIcon(icon);
		printMessage("\n");
	}

	public void printFilePath(String strPath) {
		JLabel pathLabel = new JLabel(strPath);
		pathLabel.addMouseListener(cmMouseListener);
		m_outTextPane.insertComponent(pathLabel);
		printMessage("\n");
	}

	/*
	 * private void setMessage(String strText) { m_outTextArea.setText(strText);
	 * m_outTextArea.setCaretPosition(m_outTextArea.getDocument().getLength());
	 * }
	 */

	public void processInput(String strInput) {
		int nCommand = -1;
		try {
			nCommand = Integer.parseInt(strInput);
		} catch (NumberFormatException e) {
			// System.out.println("Incorrect command number!");
			printMessage("Incorrect command number!\n");
			return;
		}

		switch (nCommand) {
		case 0:
			/*
			 * System.out.println(
			 * "---------------------------------------------------");
			 * System.out.println(
			 * "0: help, 1: connect to default server, 2: disconnect from default server"
			 * ); System.out.println(
			 * "3: login to default server, 4: logout from default server");
			 * System.out.println(
			 * "5: request session info from default server, 6: join session of defalut server, 7: leave session of default server"
			 * ); System.out.println(
			 * "8: user position, 9: chat, 10: test CMDummyEvent, 11: test datagram message"
			 * ); System.out.println(
			 * "12: test CMUserEvent, 13: print group info, 14: print current user status"
			 * ); System.out.println(
			 * "15: change group, 16: add additional channel, 17: remove additional channel"
			 * ); System.out.println(
			 * "18: set file path, 19: request file, 20: push file");
			 * System.out.println(
			 * "21: test forwarding schemes, 22: test delay of forwarding schemes"
			 * ); System.out.println(
			 * "---------------------------------------------------");
			 * System.out.println(
			 * "23: SNS content download, 24: test repeated downloading of SNS content"
			 * ); System.out.println(
			 * "25: SNS content upload, 26: register user, 27: deregister user"
			 * ); System.out.println(
			 * "28: find registered user, 29: add a new friend, 30: remove a friend"
			 * ); System.out.println(
			 * "31: request current friend list, 32: request friend requester list"
			 * ); System.out.println("33: request bi-directional friends");
			 * System.out.println(
			 * "---------------------------------------------------");
			 * System.out.println("34: request additional server info");
			 * System.out.println(
			 * "35: connect to a designated server, 36: disconnect from a designated server"
			 * ); System.out.println(
			 * "37: log in to a designated server, 38: log out from a designated server"
			 * ); System.out.println(
			 * "39: request session info from a designated server");
			 * System.out.println(
			 * "40: join a session of a designated server, 41: leave a session of a designated server"
			 * ); System.out.println(
			 * "42: print group info of a designated server");
			 * System.out.println(
			 * "---------------------------------------------------");
			 * System.out.println(
			 * "43: pull/push multiple files, 44: split a file, 45: merge files"
			 * ); System.out.println("46: distribute a file and merge");
			 * System.out.println(
			 * "---------------------------------------------------");
			 * System.out.println("47: multicast chat in current group");
			 * System.out.println("99: terminate CM");
			 */
			printMessage("---------------------------------------------------\n");
			printMessage("0: help, 1: connect to default server, 2: disconnect from default server\n");
			printMessage("3: login to default server, 4: logout from default server\n");
			printMessage(
					"5: request session info from default server, 6: join session of defalut server, 7: leave session of default server\n");
			printMessage("8: user position, 9: chat, 10: test CMDummyEvent, 11: test datagram message\n");
			printMessage("12: test CMUserEvent, 13: print group info, 14: print current user status\n");
			printMessage("15: change group, 16: add additional channel, 17: remove additional channel\n");
			printMessage("18: set file path, 19: request file, 20: push file\n");
			printMessage("21: test forwarding schemes, 22: test delay of forwarding schemes\n");
			printMessage("---------------------------------------------------\n");
			printMessage("23: SNS content download, 50: request attached file of SNS content\n");
			printMessage("24: test repeated downloading of SNS content, 25: SNS content upload\n");
			printMessage("26: register user, 27: deregister user\n");
			printMessage("28: find registered user, 29: add a new friend, 30: remove a friend\n");
			printMessage("31: request current friend list, 32: request friend requester list\n");
			printMessage("33: request bi-directional friends\n");
			printMessage("---------------------------------------------------\n");
			printMessage("34: request additional server info\n");
			printMessage("35: connect to a designated server, 36: disconnect from a designated server\n");
			printMessage("37: log in to a designated server, 38: log out from a designated server\n");
			printMessage("39: request session info from a designated server\n");
			printMessage("40: join a session of a designated server, 41: leave a session of a designated server\n");
			printMessage("42: print group info of a designated server\n");
			printMessage("---------------------------------------------------\n");
			printMessage("43: pull/push multiple files, 44: split a file, 45: merge files\n");
			printMessage("46: distribute a file and merge\n");
			printMessage("---------------------------------------------------\n");
			printMessage("47: multicast chat in current group\n");
			printMessage("99: terminate CM\n");
			break;
		case 1: // connect to default server
			testConnectionDS();
			break;
		case 2: // disconnect from default server
			testDisconnectionDS();
			break;
		case 3: // login to default server
			testLoginDS();
			break;
		case 4: // logout from default server
			testLogoutDS();
			break;
		case 5: // request session info from default server
			testSessionInfoDS();
			break;
		case 6: // join a session
			testJoinSession();
			break;
		case 7: // leave the current session
			testLeaveSession();
			break;
		case 8: // user position
			testUserPosition();
			break;
		case 9: // chat
			testChat();
			break;
		case 10: // test CMDummyEvent
			testDummyEvent();
			break;
		case 11: // test datagram message
			testDatagram();
			break;
		case 12: // test CMUserEvent
			testUserEvent();
			break;
		case 13: // print group info
			testPrintGroupInfo();
			break;
		case 14: // print current information about the client
			testCurrentUserStatus();
			break;
		case 15: // change current group
			testChangeGroup();
			break;
		case 16: // add additional channel
			testAddChannel();
			break;
		case 17: // remove additional channel
			testRemoveChannel();
			break;
		case 18: // set file path
			testSetFilePath();
			break;
		case 19: // request a file
			testRequestFile();
			break;
		case 20: // push a file
			testPushFile();
			break;
		case 21: // test forwarding schemes (typical vs. internal)
			testForwarding();
			break;
		case 22: // test delay of forwarding schemes
			testForwardingDelay();
			break;
		case 23: // test SNS content download
			testSNSContentDownload();
			break;
		case 24: // test repeated downloading of SNS content
			testRepeatedSNSContentDownload();
			break;
		case 25: // test SNS content upload
			testSNSContentUpload();
			break;
		case 26: // register user
			testRegisterUser();
			break;
		case 27: // deregister user
			testDeregisterUser();
			break;
		case 28: // find user
			testFindRegisteredUser();
			break;
		case 29: // add a new friend
			testAddNewFriend();
			break;
		case 30: // remove a friend
			testRemoveFriend();
			break;
		case 31: // request current friends list
			testRequestFriendsList();
			break;
		case 32: // request friend requesters list
			testRequestFriendRequestersList();
			break;
		case 33: // request bi-directional friends
			testRequestBiFriendsList();
			break;
		case 34: // request additional server info
			testRequestServerInfo();
			break;
		case 35: // connect to a designated server
			testConnectToServer();
			break;
		case 36: // disconnect from a designated server
			testDisconnectFromServer();
			break;
		case 37: // log in to a designated server
			testLoginServer();
			break;
		case 38: // log out from a designated server
			testLogoutServer();
			break;
		case 39: // request session information from a designated server
			testRequestSessionInfoOfServer();
			break;
		case 40: // join a session of a designated server
			testJoinSessionOfServer();
			break;
		case 41: // leave a session of a designated server
			testLeaveSessionOfServer();
			break;
		case 42: // print current group info of a designated server
			testPrintGroupInfoOfServer();
			break;
		case 43: // pull or push multiple files
			testSendMultipleFiles();
			break;
		case 44: // split a file
			testSplitFile();
			break;
		case 45: // merge files
			testMergeFiles();
			break;
		case 46: // distribute a file and merge
			testDistFileProc();
			break;
		case 47: // test multicast chat in current group
			testMulticastChat();
			break;
		case 50: // request an attached file of SNS content
			testRequestAttachedFileOfSNSContent();
			break;
		case 99: // terminate CM
			testTermination();
			break;
		default:
			System.out.println("Unknown command.");
			break;
		}
	}

	public void testConnectionDS() {
		// System.out.println("====== connect to default server\n");
		printMessage("====== connect to default server\n");
		boolean ret = m_clientStub.connectToServer();
		if (ret) {
			printMessage("Successfully connected to the default server.\n");
		} else {
			printMessage("Cannot connect to the default server!\n");
		}
		// System.out.println("======");
		printMessage("======\n");
	}

	public void testDisconnectionDS() {
		// System.out.println("====== disconnect from default server");
		printMessage("====== disconnect from default server\n");
		boolean ret = m_clientStub.disconnectFromServer();
		if (ret) {
			printMessage("Successfully disconnected from the default server.\n");
		} else {
			printMessage("Error while disconnecting from the default server!");
		}
		// System.out.println("======");
		printMessage("======\n");

		setTitle("CM Client");
	}

	public void testLoginDS() {
		String strUserName = null;
		String strPassword = null;
		String strEncPassword = null;
		Console console = System.console();
		if (console == null) {
			// System.err.println("Unable to obtain console.");
		}

		// System.out.println("====== login to default server");
		printMessage("====== login to default server\n");
		/*
		 * System.out.print("user name: "); BufferedReader br = new
		 * BufferedReader(new InputStreamReader(System.in)); try { strUserName =
		 * br.readLine(); if(console == null) { System.out.print("password: ");
		 * strPassword = br.readLine(); } else strPassword = new
		 * String(console.readPassword("password: ")); } catch (IOException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		JTextField userNameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		Object[] message = { "User Name:", userNameField, "Password:", passwordField };
		int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			strUserName = userNameField.getText();
			strPassword = new String(passwordField.getPassword()); // security
																	// problem?
			// encrypt password
			strEncPassword = CMUtil.getSHA1Hash(strPassword);

			// m_clientStub.loginCM(strUserName, strPassword);
			m_clientStub.loginCM(strUserName, strEncPassword);
		}

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testLogoutDS() {
		// System.out.println("====== logout from default server");
		printMessage("====== logout from default server\n");
		m_clientStub.logoutCM();
		// System.out.println("======");
		printMessage("======\n");

		setTitle("CM Client");
	}

	public void testTermination() {
		m_clientStub.disconnectFromServer();
		m_clientStub.terminateCM();
		// change button to "start CM"
		m_startStopButton.setText("Start Client CM");

		setTitle("CM Client");
	}

	public void testSessionInfoDS() {
		// System.out.println("====== request session info from default
		// server");
		printMessage("====== request session info from default server\n");
		m_clientStub.requestSessionInfo();
		// System.out.println("======");
		printMessage("======\n");
	}

	public void testJoinSession() {
		String strSessionName = null;
		// System.out.println("====== join a session");
		printMessage("====== join a session\n");
		/*
		 * System.out.print("session name: "); BufferedReader br = new
		 * BufferedReader(new InputStreamReader(System.in)); try {
		 * strSessionName = br.readLine(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		strSessionName = JOptionPane.showInputDialog("Session Name:");
		if (strSessionName != null)
			m_clientStub.joinSession(strSessionName);
		// System.out.println("======");
		printMessage("======\n");
	}

	public void testLeaveSession() {
		// System.out.println("====== leave the current session");
		printMessage("====== leave the current session\n");
		m_clientStub.leaveSession();
		// System.out.println("======");
		printMessage("======\n");
	}

	public void testUserPosition() {
		CMPosition position = new CMPosition();
		// String strLine = null;
		// String strDelim = "\\s+";
		// String[] strTokens;
		// System.out.println("====== send user position");
		printMessage("====== send user position\n");
		/*
		 * System.out.print("pos (x,y,z): "); BufferedReader br = new
		 * BufferedReader(new InputStreamReader(System.in)); try { strLine =
		 * br.readLine(); } catch (IOException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } strLine.trim(); strTokens =
		 * strLine.split(strDelim); position.m_p.m_x =
		 * Float.parseFloat(strTokens[0]); position.m_p.m_y =
		 * Float.parseFloat(strTokens[1]); position.m_p.m_z =
		 * Float.parseFloat(strTokens[2]); System.out.println("Pos input: ("
		 * +position.m_p.m_x+", "+position.m_p.m_y+", "+position.m_p.m_z+")");
		 */
		JTextField xField = new JTextField();
		JTextField yField = new JTextField();
		JTextField zField = new JTextField();
		JTextField quatWField = new JTextField();
		JTextField quatXField = new JTextField();
		JTextField quatYField = new JTextField();
		JTextField quatZField = new JTextField();
		Object[] message = { "pos(x): ", xField, "pos(y): ", yField, "pos(z): ", zField, "quat(w): ", quatWField,
				"quat(x): ", quatXField, "quat(y): ", quatYField, "quat(z): ", quatZField };
		int option = JOptionPane.showConfirmDialog(null, message, "Position (x,y,z), Quat (w,x,y,z) Input",
				JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.CANCEL_OPTION)
			return;
		position.m_p.m_x = Float.parseFloat(xField.getText());
		position.m_p.m_y = Float.parseFloat(yField.getText());
		position.m_p.m_z = Float.parseFloat(zField.getText());
		position.m_q.m_w = Float.parseFloat(quatWField.getText());
		position.m_q.m_x = Float.parseFloat(quatXField.getText());
		position.m_q.m_y = Float.parseFloat(quatYField.getText());
		position.m_q.m_z = Float.parseFloat(quatZField.getText());
		printMessage("Pos input: (" + position.m_p.m_x + ", " + position.m_p.m_y + ", " + position.m_p.m_z + ")\n");
		printMessage("Quat input: (" + position.m_q.m_w + ", " + position.m_q.m_x + ", " + position.m_q.m_y + ", "
				+ position.m_q.m_z + ")\n");
		/*
		 * System.out.print("quat (w,x,y,z): "); try { strLine = br.readLine();
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } strLine.trim(); strTokens =
		 * strLine.split(strDelim); position.m_q.m_w =
		 * Float.parseFloat(strTokens[0]); position.m_q.m_x =
		 * Float.parseFloat(strTokens[1]); position.m_q.m_y =
		 * Float.parseFloat(strTokens[2]); position.m_q.m_z =
		 * Float.parseFloat(strTokens[3]); System.out.println("Quat input: ("
		 * +position.m_q.m_w+", "+position.m_q.m_x+", "+position.m_q.m_y+", "
		 * +position.m_q.m_z+")");
		 */

		m_clientStub.sendUserPosition(position);

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testChat() {
		String strTarget = null;
		String strMessage = null;
		// System.out.println("====== chat");
		printMessage("====== chat\n");
		/*
		 * System.out.print("target(/b, /s, /g, or /username): ");
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); try { strTarget = br.readLine();
		 * strTarget = strTarget.trim(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } System.out.print(
		 * "message: "); try { strMessage = br.readLine(); } catch (IOException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		JTextField targetField = new JTextField();
		JTextField messageField = new JTextField();
		Object[] message = { "Target(/b, /s, /g, or /username): ", targetField, "Message: ", messageField };
		int option = JOptionPane.showConfirmDialog(null, message, "Chat Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			strTarget = targetField.getText();
			strMessage = messageField.getText();
			m_clientStub.chat(strTarget, strMessage);
		}

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testDummyEvent() {
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		String strInput = null;

		if (myself.getState() != CMInfo.CM_SESSION_JOIN) {
			// System.out.println("You should join a session and a group!");
			printMessage("You should join a session and a group!\n");
			return;
		}

		// System.out.println("====== test CMDummyEvent in current group");
		printMessage("====== test CMDummyEvent in current group\n");
		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.print("input message: ");
		 * try { strInput = br.readLine(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */
		strInput = JOptionPane.showInputDialog("Input Message: ");
		if (strInput == null)
			return;

		CMDummyEvent due = new CMDummyEvent();
		due.setHandlerSession(myself.getCurrentSession());
		due.setHandlerGroup(myself.getCurrentGroup());
		due.setDummyInfo(strInput);
		m_clientStub.cast(due, myself.getCurrentSession(), myself.getCurrentGroup());
		due = null;

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testDatagram() {
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();

		if (myself.getState() != CMInfo.CM_SESSION_JOIN) {
			// System.out.println("You should join a session and a group!");
			printMessage("You should join a session and a group!\n");
			return;
		}

		String strReceiver = null;
		String strMessage = null;
		// System.out.println("====== test unicast chatting with datagram");
		printMessage("====== test unicast chatting with datagram\n");
		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.print("receiver: "); try {
		 * strReceiver = br.readLine(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); } System.out.print(
		 * "message: "); try { strMessage = br.readLine(); } catch (IOException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */
		JTextField receiverField = new JTextField();
		JTextField messageField = new JTextField();
		Object[] message = { "Receiver: ", receiverField, "Message: ", messageField };
		int option = JOptionPane.showConfirmDialog(null, message, "Message Input", JOptionPane.OK_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			strReceiver = receiverField.getText();
			strMessage = messageField.getText();

			CMInterestEvent ie = new CMInterestEvent();
			ie.setID(CMInterestEvent.USER_TALK);
			ie.setHandlerSession(myself.getCurrentSession());
			ie.setHandlerGroup(myself.getCurrentGroup());
			ie.setUserName(myself.getName());
			ie.setTalk(strMessage);
			m_clientStub.send(ie, strReceiver, CMInfo.CM_DATAGRAM);
			ie = null;
		}

		// System.out.println("======");
		printMessage("======\n");
		return;
	}

	public void testUserEvent() {
		// String strInput = null;
		String strReceiver = null;
		// boolean bEnd = false;
		// String[] strTokens = null;
		int nValueByteNum = -1;
		CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();

		if (myself.getState() != CMInfo.CM_SESSION_JOIN) {
			// System.out.println("You should join a session and a group!");
			printMessage("You should join a session and a group!\n");
			return;
		}

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== test CMUserEvent"); System.out.println(
		 * "data type: CM_INT(0) CM_LONG(1) CM_FLOAT(2) CM_DOUBLE(3) CM_CHAR(4) CM_STR(5) CM_BYTES(6)"
		 * ); System.out.println("Type \"end\" to stop.");
		 * 
		 * CMUserEvent ue = new CMUserEvent(); ue.setStringID("testID");
		 * ue.setHandlerSession(myself.getCurrentSession());
		 * ue.setHandlerGroup(myself.getCurrentGroup()); while(!bEnd) {
		 * System.out.println(
		 * "If the data type is CM_BYTES, the number of bytes must be given " +
		 * "in the third parameter."); System.out.print(
		 * "(data type, field name, value): "); try { strInput = br.readLine();
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); ue.removeAllEventFields(); ue = null; return; }
		 * 
		 * if(strInput.equals("end")) { bEnd = true; } else { strInput.trim();
		 * strTokens = strInput.split("\\s+"); if(Integer.parseInt(strTokens[0])
		 * == CMInfo.CM_BYTES) { nValueByteNum = Integer.parseInt(strTokens[2]);
		 * if(nValueByteNum < 0) { System.out.println(
		 * "CMClientApp.testUserEvent(), Invalid nValueByteNum("
		 * +nValueByteNum+")"); ue.removeAllEventFields(); ue = null; return; }
		 * byte[] valueBytes = new byte[nValueByteNum]; for(int i = 0; i <
		 * nValueByteNum; i++) valueBytes[i] = 1; // dummy data
		 * ue.setEventBytesField(strTokens[1], nValueByteNum, valueBytes); }
		 * else ue.setEventField(Integer.parseInt(strTokens[0]), strTokens[1],
		 * strTokens[2]); } }
		 * 
		 * System.out.print("receiver: "); try { strReceiver = br.readLine(); }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		printMessage("====== test CMUserEvent\n");

		String strFieldNum = null;
		int nFieldNum = -1;

		strFieldNum = JOptionPane.showInputDialog("Field Numbers:");
		if (strFieldNum == null)
			return;
		try {
			nFieldNum = Integer.parseInt(strFieldNum);
		} catch (NumberFormatException e) {
			printMessage("Input must be an integer number greater than 0!");
			return;
		}

		String strID = null;
		JTextField strIDField = new JTextField();
		JTextField strReceiverField = new JTextField();
		String[] dataTypes = { "CM_INT", "CM_LONG", "CM_FLOAT", "CM_DOUBLE", "CM_CHAR", "CH_STR", "CM_BYTES" };
		JComboBox<String>[] dataTypeBoxes = new JComboBox[nFieldNum];
		JTextField[] eventFields = new JTextField[nFieldNum * 2];
		Object[] message = new Object[4 + nFieldNum * 3 * 2];

		for (int i = 0; i < nFieldNum; i++) {
			dataTypeBoxes[i] = new JComboBox<String>(dataTypes);
		}

		for (int i = 0; i < nFieldNum * 2; i++) {
			eventFields[i] = new JTextField();
		}

		message[0] = "event ID: ";
		message[1] = strIDField;
		message[2] = "Receiver Name: ";
		message[3] = strReceiverField;
		for (int i = 4, j = 0, k = 1; i < 4 + nFieldNum * 3 * 2; i += 6, j += 2, k++) {
			message[i] = "Data type " + k + ":";
			message[i + 1] = dataTypeBoxes[k - 1];
			message[i + 2] = "Field Name " + k + ":";
			message[i + 3] = eventFields[j];
			message[i + 4] = "Field Value " + k + ":";
			message[i + 5] = eventFields[j + 1];
		}
		int option = JOptionPane.showConfirmDialog(null, message, "User Event Input", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			strID = strIDField.getText();
			strReceiver = strReceiverField.getText();

			CMUserEvent ue = new CMUserEvent();
			ue.setStringID(strID);
			ue.setHandlerSession(myself.getCurrentSession());
			ue.setHandlerGroup(myself.getCurrentGroup());

			for (int i = 0, j = 0; i < nFieldNum * 2; i += 2, j++) {
				// if(Integer.parseInt(eventFields[i].getText()) ==
				// CMInfo.CM_BYTES)
				if (dataTypeBoxes[j].getSelectedIndex() == CMInfo.CM_BYTES) {
					nValueByteNum = Integer.parseInt(eventFields[i + 1].getText());
					if (nValueByteNum < 0) {
						// System.out.println("CMClientApp.testUserEvent(),
						// Invalid nValueByteNum("
						// +nValueByteNum+")");
						printMessage("CMClientApp.testUserEvent(), Invalid nValueByteNum(" + nValueByteNum + ")\n");
						ue.removeAllEventFields();
						ue = null;
						return;
					}
					byte[] valueBytes = new byte[nValueByteNum];
					for (int k = 0; k < nValueByteNum; k++)
						valueBytes[k] = 1; // dummy data
					ue.setEventBytesField(eventFields[i].getText(), nValueByteNum, valueBytes);
				} else {
					ue.setEventField(dataTypeBoxes[j].getSelectedIndex(), eventFields[i].getText(),
							eventFields[i + 1].getText());
				}

			}

			m_clientStub.send(ue, strReceiver);
			ue.removeAllEventFields();
			ue = null;
		}

		// System.out.println("======");
		printMessage("======\n");

		return;
	}

	// print group information provided by the default server
	public void testPrintGroupInfo() {
		// check local state
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();

		if (myself.getState() != CMInfo.CM_SESSION_JOIN) {
			// System.out.println("You should join a session and a group.");
			printMessage("You should join a session and a group.\n");
			return;
		}

		CMSession session = interInfo.findSession(myself.getCurrentSession());
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		// System.out.println("---------------------------------------------------------");
		// System.out.format("%-20s%-20s%-20s%n", "group name", "multicast
		// addr", "multicast port");
		// System.out.println("---------------------------------------------------------");
		printMessage("---------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port"));
		printMessage("---------------------------------------------------------\n");

		while (iter.hasNext()) {
			CMGroupInfo gInfo = iter.next();
			// System.out.format("%-20s%-20s%-20d%n", gInfo.getGroupName(),
			// gInfo.getGroupAddress()
			// , gInfo.getGroupPort());
			printMessage(String.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress(),
					gInfo.getGroupPort()));
		}

		return;
	}

	

	public void testCurrentUserStatus() {
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		// System.out.println("------ for the default server");
		printMessage("------ for the default server\n");
		// System.out.println("name("+myself.getName()+"),
		// session("+myself.getCurrentSession()+"), group("
		// +myself.getCurrentGroup()+"), udp port("+myself.getUDPPort()+"),
		// state("
		// +myself.getState()+").");
		printMessage("name(" + myself.getName() + "), session(" + myself.getCurrentSession() + "), group("
				+ myself.getCurrentGroup() + "), udp port(" + myself.getUDPPort() + "), state(" + myself.getState()
				+ "), attachment download scheme(" + confInfo.getAttachDownloadScheme() + ").\n");

		// for additional servers
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while (iter.hasNext()) {
			CMServer tserver = iter.next();
			if (tserver.getSocketChannelInfo().findChannel(0) != null) {
				// System.out.println("------ for additional
				// server["+tserver.getServerName()+"]");
				printMessage("------ for additional server[" + tserver.getServerName() + "]\n");
				// System.out.println("current
				// session("+tserver.getCurrentSessionName()+
				// "), current group("+tserver.getCurrentGroupName()+"), state("
				// +tserver.getClientState()+").");
				printMessage("current session(" + tserver.getCurrentSessionName() + "), current group("
						+ tserver.getCurrentGroupName() + "), state(" + tserver.getClientState() + ").");

			}
		}

		return;
	}

	public void testChangeGroup() {
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));
		String strGroupName = null;
		// System.out.println("====== change group");
		printMessage("====== change group\n");
		/*
		 * try { System.out.print("group name: "); strGroupName = br.readLine();
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		strGroupName = JOptionPane.showInputDialog("Group Name: ");
		if (strGroupName != null)
			m_clientStub.changeGroup(strGroupName);

		// System.out.println("======");
		printMessage("======\n");
		return;
	}

	// ServerSocketChannel is not supported.
	// A server cannot add SocketChannel.
	// For the SocketChannel, available server name must be given as well.
	// For the MulticastChannel, session name and group name known by this
	// client/server must be given.
	public void testAddChannel() {
		int nChType = -1;
		int nChIndex = -1;
		String strServerName = null;
		String strChAddress = null;
		int nChPort = -1;
		String strSessionName = null;
		String strGroupName = null;
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));

		if (confInfo.getSystemType().equals("CLIENT")) {
			CMUser myself = interInfo.getMyself();
			if (myself.getState() != CMInfo.CM_SESSION_JOIN && myself.getState() != CMInfo.CM_LOGIN) {
				// System.out.println("You should login to the default
				// server.");
				printMessage("You should login to the default server.\n");
				return;
			}
		}

		// System.out.println("====== add additional channel");
		printMessage("====== add additional channel\n");

		// ask channel type, (server name), channel index (integer greater than
		// 0), addr, port
		/*
		 * try { System.out.print(
		 * "Select channel type (SocketChannel:2, DatagramChannel:3, MulticastChannel:4): "
		 * ); nChType = Integer.parseInt(br.readLine()); System.out.print(
		 * "Channel Index(integer greater than 0): "); nChIndex =
		 * Integer.parseInt(br.readLine()); if(nChType ==
		 * CMInfo.CM_SOCKET_CHANNEL) { System.out.print(
		 * "Server name(\"SERVER\" for the default server): "); strServerName =
		 * br.readLine(); } else if(nChType == CMInfo.CM_DATAGRAM_CHANNEL) {
		 * System.out.print("Channel udp port: "); nChPort =
		 * Integer.parseInt(br.readLine()); } else if(nChType ==
		 * CMInfo.CM_MULTICAST_CHANNEL) { System.out.print(
		 * "Target session name: "); strSessionName = br.readLine();
		 * System.out.print("Target group name: "); strGroupName =
		 * br.readLine(); System.out.print("Channel multicast address: ");
		 * strChAddress = br.readLine(); System.out.print(
		 * "Channel multicast port: "); nChPort =
		 * Integer.parseInt(br.readLine()); } } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		String[] chTypes = { "SocketChannel", "DatagramChannel", "MulticastChannel" };
		JComboBox<String> chTypeBox = new JComboBox<String>(chTypes);
		JTextField chIndexField = new JTextField();
		Object[] message = { "Channel Type: ", chTypeBox, "Channel Index (> 0): ", chIndexField };
		int option = JOptionPane.showConfirmDialog(null, message, "Channel type & index", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		nChType = chTypeBox.getSelectedIndex() + 2;
		String strChIndex = chIndexField.getText();
		try {
			nChIndex = Integer.parseInt(strChIndex);
		} catch (NumberFormatException e) {
			printMessage("A channel index must be an integer!");
			return;
		}

		if (nChType == CMInfo.CM_SOCKET_CHANNEL) {
			strServerName = JOptionPane.showInputDialog("Server name(\"SERVER\" for the default server): ");
			if (strServerName == null)
				return;
		} else if (nChType == CMInfo.CM_DATAGRAM_CHANNEL) {
			String strUDP = JOptionPane.showInputDialog("Channel UDP Port: ");
			if (strUDP == null)
				return;
			try {
				nChPort = Integer.parseInt(strUDP);
			} catch (NumberFormatException e) {
				printMessage("A channel UDP port must be an integer!");
				return;
			}

		} else if (nChType == CMInfo.CM_MULTICAST_CHANNEL) {
			JTextField snameField = new JTextField();
			JTextField gnameField = new JTextField();
			JTextField chAddrField = new JTextField();
			JTextField chPortField = new JTextField();
			Object[] multicastMessage = { "Target Session Name: ", snameField, "Target Group Name: ", gnameField,
					"Channel Multicast Address: ", chAddrField, "Channel Multicast Port: ", chPortField };
			int multicastResponse = JOptionPane.showConfirmDialog(null, multicastMessage, "Additional Multicast Input",
					JOptionPane.OK_CANCEL_OPTION);
			if (multicastResponse != JOptionPane.OK_OPTION)
				return;

			strSessionName = snameField.getText();
			strGroupName = gnameField.getText();
			strChAddress = chAddrField.getText();
			nChPort = Integer.parseInt(chPortField.getText());
		}

		switch (nChType) {
		case CMInfo.CM_SOCKET_CHANNEL:
			m_clientStub.addSocketChannel(nChIndex, strServerName);
			break;
		case CMInfo.CM_DATAGRAM_CHANNEL:
			m_clientStub.addDatagramChannel(nChIndex, nChPort);
			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			m_clientStub.addMulticastChannel(nChIndex, strSessionName, strGroupName, strChAddress, nChPort);
			break;
		default:
			// System.out.println("Channel type is incorrect!");
			printMessage("Channel type is incorrect!\n");
			break;
		}

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testRemoveChannel() {
		int nChType = -1;
		int nChIndex = -1;
		String strServerName = null;
		String strSessionName = null;
		String strGroupName = null;
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));

		if (confInfo.getSystemType().equals("CLIENT")) {
			CMUser myself = interInfo.getMyself();
			if (myself.getState() != CMInfo.CM_SESSION_JOIN && myself.getState() != CMInfo.CM_LOGIN) {
				// System.out.println("You should login to the default
				// server.");
				printMessage("You should login to the default server.\n");
				return;
			}
		}

		// System.out.println("====== remove additional channel");
		printMessage("====== remove additional channel\n");

		/*
		 * try { System.out.print(
		 * "Select channel type (SocketChannel:2, DatagramChannel:3, MulticastChannel:4): "
		 * ); nChType = Integer.parseInt(br.readLine()); System.out.print(
		 * "Channel Index(integer greater than 0): "); nChIndex =
		 * Integer.parseInt(br.readLine()); if(nChType ==
		 * CMInfo.CM_SOCKET_CHANNEL) { System.out.print(
		 * "Server name(\"SERVER\" for the default server): "); strServerName =
		 * br.readLine(); } else if(nChType == CMInfo.CM_MULTICAST_CHANNEL) {
		 * System.out.print("Target session name: "); strSessionName =
		 * br.readLine(); System.out.print("Target group name: "); strGroupName
		 * = br.readLine(); }
		 * 
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		String[] chTypes = { "SocketChannel", "DatagramChannel", "MulticastChannel" };
		JComboBox<String> chTypeBox = new JComboBox<String>(chTypes);
		JTextField chIndexField = new JTextField();
		Object[] message = { "Channel Type: ", chTypeBox, "Channel Index(> 0): ", chIndexField };
		int option = JOptionPane.showConfirmDialog(null, message, "Removal of Additional Channel",
				JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		nChType = chTypeBox.getSelectedIndex() + 2;
		try {
			nChIndex = Integer.parseInt(chIndexField.getText());
		} catch (NumberFormatException e) {
			printMessage("Channel index must be an integer!");
			return;
		}

		if (nChType == CMInfo.CM_SOCKET_CHANNEL) {
			strServerName = JOptionPane.showInputDialog("Server name(\"SERVER\" for the default server): ");
			if (strServerName == null)
				return;
		} else if (nChType == CMInfo.CM_MULTICAST_CHANNEL) {
			JTextField snameField = new JTextField();
			JTextField gnameField = new JTextField();
			Object[] sgMessage = { "Target Session Name: ", snameField, "Target Group Name: ", gnameField };
			int sgOption = JOptionPane.showConfirmDialog(null, sgMessage, "Target Session and Group",
					JOptionPane.OK_CANCEL_OPTION);
			if (sgOption != JOptionPane.OK_OPTION)
				return;
			strSessionName = snameField.getText();
			strGroupName = gnameField.getText();
		}

		switch (nChType) {
		case CMInfo.CM_SOCKET_CHANNEL:
			m_clientStub.removeAdditionalSocketChannel(nChIndex, strServerName);
			break;
		case CMInfo.CM_DATAGRAM_CHANNEL:
			m_clientStub.removeAdditionalDatagramChannel(nChIndex);
			break;
		case CMInfo.CM_MULTICAST_CHANNEL:
			m_clientStub.removeAdditionalMulticastChannel(nChIndex, strSessionName, strGroupName);
			break;
		default:
			// System.out.println("Channel type is incorrect!");
			printMessage("Channel type is incorrect!\n");
			break;
		}

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testSetFilePath() {
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));
		// System.out.println("====== set file path");
		printMessage("====== set file path\n");
		String strPath = null;

		/*
		 * System.out.print("file path (must end with \'/\'): "); try { strPath
		 * = br.readLine(); } catch (IOException e) { // TODO Auto-generated
		 * catch block e.printStackTrace(); }
		 */

		strPath = JOptionPane.showInputDialog("file path (must end with \'/\'): ");
		if (strPath == null)
			return;

		if (!strPath.endsWith("/")) {
			// System.out.println("Invalid file path!");
			printMessage("Invalid file path! (must end with \'/\')");
			return;
		}

		// CMFileTransferManager.setFilePath(strPath, m_clientStub.getCMInfo());
		m_clientStub.setFilePath(strPath);

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testRequestFile() {
		String strFileName = null;
		String strFileOwner = null;
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));
		// System.out.println("====== request a file");
		printMessage("====== request a file\n");
		/*
		 * try { System.out.print("File name: "); strFileName = br.readLine();
		 * System.out.print("File owner(server name): "); strFileOwner =
		 * br.readLine(); } catch (IOException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 */

		JTextField fnameField = new JTextField();
		JTextField fownerField = new JTextField();
		Object[] message = { "File Name: ", fnameField, "File Owner: ", fownerField };
		int option = JOptionPane.showConfirmDialog(null, message, "File Request", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			strFileName = fnameField.getText();
			strFileOwner = fownerField.getText();
			// CMFileTransferManager.requestFile(strFileName, strFileOwner,
			// m_clientStub.getCMInfo());
			m_clientStub.requestFile(strFileName, strFileOwner);
		}

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testPushFile() {
		String strFilePath = null;
		File[] files = null;
		String strReceiver = null;
		printMessage("====== push a file\n");

		/*
		 * JTextField fnameField = new JTextField(); JTextField freceiverField =
		 * new JTextField(); Object[] message = { "File Path Name: ",
		 * fnameField, "File Receiver: ", freceiverField }; int option =
		 * JOptionPane.showConfirmDialog(null, message, "File Push",
		 * JOptionPane.OK_CANCEL_OPTION); if(option == JOptionPane.OK_OPTION) {
		 * strFilePath = fnameField.getText(); strReceiver =
		 * freceiverField.getText(); CMFileTransferManager.pushFile(strFilePath,
		 * strReceiver, m_clientStub.getCMInfo()); }
		 */

		strReceiver = JOptionPane.showInputDialog("Receiver Name: ");
		if (strReceiver == null)
			return;
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		CMFileTransferInfo fInfo = m_clientStub.getCMInfo().getFileTransferInfo();
		File curDir = new File(fInfo.getFilePath());
		fc.setCurrentDirectory(curDir);
		int fcRet = fc.showOpenDialog(this);
		if (fcRet != JFileChooser.APPROVE_OPTION)
			return;
		files = fc.getSelectedFiles();
		if (files.length < 1)
			return;
		for (int i = 0; i < files.length; i++) {
			strFilePath = files[i].getPath();
			// CMFileTransferManager.pushFile(strFilePath, strReceiver,
			// m_clientStub.getCMInfo());
			m_clientStub.pushFile(strFilePath, strReceiver);
		}

		printMessage("======\n");
	}

	public void testForwarding() {
		int nForwardType = 0;
		float fForwardRate = 0;
		int nSimNum = 0;
		int nEventTypeNum = 10;
		int nEventRange = -1;
		int nEventID = -1;
		String strUserName = null;
		CMUserEvent ue = null;

		int nUserState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();
		if (nUserState != CMInfo.CM_LOGIN && nUserState != CMInfo.CM_SESSION_JOIN) {
			// System.out.println("You must log in to the default server.");
			printMessage("You must log in to the default server.\n");
			return;
		}

		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));

		// System.out.println("====== typical/internal forwarding test");
		printMessage("====== typical/internal forwarding test\n");

		/*
		 * try { System.out.print("Forwarding type (0: typical, 1: internal): "
		 * ); nForwardType = Integer.parseInt(br.readLine()); System.out.print(
		 * "Forwarding rate (0 ~ 1): "); fForwardRate =
		 * Float.parseFloat(br.readLine()); System.out.print("Simulation num: "
		 * ); nSimNum = Integer.parseInt(br.readLine()); } catch
		 * (NumberFormatException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); return; } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); return; }
		 */
		String[] ftypes = { "Typical", "Internal" };
		JComboBox<String> ftypeBox = new JComboBox<String>(ftypes);
		JTextField frateField = new JTextField();
		JTextField simnumField = new JTextField();
		Object[] message = { "Forwarding Type: ", ftypeBox, "Forwarding Rate (0 ~ 1): ", frateField,
				"Simulation Number: ", simnumField };
		int option = JOptionPane.showConfirmDialog(null, message, "Event Forwarding Test",
				JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		nForwardType = ftypeBox.getSelectedIndex();
		fForwardRate = Float.parseFloat(frateField.getText());
		nSimNum = Integer.parseInt(simnumField.getText());

		nEventRange = (int) (nEventTypeNum * fForwardRate); // number of event
															// types which must
															// be forwarded
		strUserName = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
		Random rnd = new Random();
		ue = new CMUserEvent();

		for (int i = 0; i < nSimNum; i++) {
			for (int j = 0; j < 100; j++) {
				ue = new CMUserEvent();
				nEventID = rnd.nextInt(10); // 0 ~ 9
				if (nEventID >= 0 && nEventID < nEventRange)
					ue.setStringID("testForward");
				else
					ue.setStringID("testNotForward");
				ue.setEventField(CMInfo.CM_INT, "id", String.valueOf(nEventID));
				ue.setEventField(CMInfo.CM_INT, "ftype", String.valueOf(nForwardType));
				ue.setEventField(CMInfo.CM_STR, "user", strUserName);

				// send the event to a server
				if (nForwardType == 0)
					m_clientStub.send(ue, "SERVER");
				else if (nForwardType == 1) {
					if (ue.getStringID().equals("testForward"))
						m_clientStub.send(ue, strUserName);
					else
						m_clientStub.send(ue, "SERVER");
				} else {
					// System.out.println("Invalid forwarding type:
					// "+nForwardType);
					printMessage("Invalid forwarding type: " + nForwardType + "\n");
					return;
				}
			}
		}

		// send an end event to a server (id: EndSim, int: simnum)
		ue = new CMUserEvent();
		ue.setStringID("EndSim");
		ue.setEventField(CMInfo.CM_INT, "simnum", String.valueOf(nSimNum));
		m_clientStub.send(ue, "SERVER");

		ue = null;
		return;
	}

	public void testForwardingDelay() {
		int nForwardType = 0;
		int nSendNum = 0;
		String strUserName = null;
		long lSendTime = 0;
		CMUserEvent ue = null;

		int nUserState = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getState();
		if (nUserState != CMInfo.CM_LOGIN && nUserState != CMInfo.CM_SESSION_JOIN) {
			// System.out.println("You must log in to the default server.");
			printMessage("You must log in to the default server.\n");
			return;
		}

		// System.out.println("====== test delay of forwarding schemes (typical
		// vs. internal");
		printMessage("====== test delay of forwarding schemes (typical vs. internal\n");

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); try { System.out.print(
		 * "forward type(0:typical, 1:internal): "); nForwardType =
		 * Integer.parseInt(br.readLine()); System.out.print("Send num: ");
		 * nSendNum = Integer.parseInt(br.readLine()); } catch
		 * (NumberFormatException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); return; } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); return; }
		 */
		String[] fTypes = { "Typical", "Internal" };
		JComboBox<String> forwardTypeBox = new JComboBox<String>(fTypes);
		JTextField sendNumField = new JTextField();
		Object[] message = { "Forward Type: ", forwardTypeBox, "Number of Transmission: ", sendNumField };
		int option = JOptionPane.showConfirmDialog(null, message, "Test Forwarding Delay",
				JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		nForwardType = forwardTypeBox.getSelectedIndex();
		nSendNum = Integer.parseInt(sendNumField.getText());

		strUserName = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();

		for (int i = 0; i < nSendNum; i++) {

			// generate a test event
			ue = new CMUserEvent();
			ue.setStringID("testForwardDelay");
			ue.setEventField(CMInfo.CM_INT, "id", String.valueOf(i));
			ue.setEventField(CMInfo.CM_INT, "ftype", String.valueOf(nForwardType));
			ue.setEventField(CMInfo.CM_STR, "user", strUserName);

			lSendTime = System.currentTimeMillis();
			ue.setEventField(CMInfo.CM_LONG, "stime", String.valueOf(lSendTime));

			// send an event to a server
			if (nForwardType == 0)
				m_clientStub.send(ue, "SERVER");
			else if (nForwardType == 1) {
				m_clientStub.send(ue, strUserName);
			} else {
				// System.out.println("Invalid forward type: "+nForwardType);
				printMessage("Invalid forward type: " + nForwardType + "\n");
				return;
			}
		}

		// send end event to a server (id: EndSim, int: simnum)
		ue = new CMUserEvent();
		ue.setStringID("EndForwardDelay");
		ue.setEventField(CMInfo.CM_INT, "ftype", String.valueOf(nForwardType));
		ue.setEventField(CMInfo.CM_STR, "user", strUserName);
		ue.setEventField(CMInfo.CM_INT, "sendnum", String.valueOf(nSendNum));

		if (nForwardType == 0)
			m_clientStub.send(ue, "SERVER");
		else
			m_clientStub.send(ue, strUserName);

		// System.out.println("======");
		printMessage("======\n");

		ue = null;
		return;
	}

	public void testSNSContentDownload() {
		// System.out.println("====== request downloading of SNS content (offset
		// 0)");
		printMessage("====== request downloading of SNS content (offset 0)\n");

		int nContentOffset = 0;
		String strWriterName = null;
		String strUserName = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();

		JTextField offsetField = new JTextField();
		JTextField writerField = new JTextField();
		Object[] message = { "Offset ( >= 0 ): ", offsetField,
				"Content Writer (Empty for no designation, CM_MY_FRIEND for my friends, CM_BI_FRIEND for my bi-friends, "
						+ "or a specific name): ",
				writerField };
		int option = JOptionPane.showConfirmDialog(null, message, "Request content download",
				JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		if (offsetField.getText().isEmpty())
			nContentOffset = 0;
		else
			nContentOffset = Integer.parseInt(offsetField.getText());
		strWriterName = writerField.getText();

		// start time of downloading contents
		m_eventHandler.setStartTime(System.currentTimeMillis());

		m_clientStub.requestSNSContent(strUserName, strWriterName, nContentOffset);
		if (CMInfo._CM_DEBUG) {
			printMessage("[" + strUserName + "] requests content of writer[" + strWriterName + "] with offset("
					+ nContentOffset + ").\n");
		}

		printMessage("======\n");
		return;
	}

	public void testRequestAttachedFileOfSNSContent() {
		printMessage("====== request an attached file of SNS content\n");
		// int nContentID = 0;
		// String strWriterName = null;
		String strFileName = null;

		// JTextField contentIDField = new JTextField();
		// JTextField writerField = new JTextField();
		JTextField fileNameField = new JTextField();
		Object[] message = {
				// "Content ID: ", contentIDField,
				// "Content Writer Name: ", writerField,
				"Attachment File Name: ", fileNameField };
		int option = JOptionPane.showConfirmDialog(null, message, "Request an attached file of SNS content",
				JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;

		// nContentID = Integer.parseInt(contentIDField.getText());
		// strWriterName = writerField.getText();
		strFileName = fileNameField.getText();

		// m_clientStub.requestAttachedFileOfSNSContent(nContentID,
		// strWriterName, strFileName);
		m_clientStub.requestAttachedFileOfSNSContent(strFileName);
		return;
	}

	public void testRepeatedSNSContentDownload() {
		// System.out.println("====== Repeated downloading of SNS content");
		printMessage("====== Repeated downloading of SNS content\n");

		// open a file for writing the access delay and # downloaded contents
		FileOutputStream fos = null;
		PrintWriter pw = null;
		try {
			fos = new FileOutputStream("SNSContentDownload.txt");
			pw = new PrintWriter(fos);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_eventHandler.setFileOutputStream(fos);
		m_eventHandler.setPrintWriter(pw);
		m_eventHandler.setSimNum(100);

		String strUserName = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
		m_clientStub.requestSNSContent(strUserName, "", 0); // no specific
															// writer, offset =
															// 0

		return;
	}

	public void testSNSContentUpload() {
		String strMessage = null;
		ArrayList<String> filePathList = null;
		int nNumAttachedFiles = 0;
		int nReplyOf = 0;
		int nLevelOfDisclosure = 0;
		File[] files = null;

		printMessage("====== test SNS content upload\n");

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); try { System.out.print(
		 * "Input message: "); strMessage = br.readLine(); System.out.print(
		 * "Input attached file path (0 for no attachment): "); strFilePath =
		 * br.readLine(); if(strFilePath.equals("0")) strFilePath=""; } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		JTextField msgField = new JTextField();
		JCheckBox attachedFilesBox = new JCheckBox();
		JTextField replyOfField = new JTextField();
		String[] lod = { "Everyone", "My Followers", "Bi-Friends", "Nobody" };
		JComboBox<String> lodBox = new JComboBox<String>(lod);
		// JTextField lodField = new JTextField();
		Object[] message = { "Input Message: ", msgField, "File Attachment: ", attachedFilesBox,
				"Content ID to which this content replies(0 for no reply): ", replyOfField,
				// "Level of Disclosure(0: to everyone, 1: to my followers, 2:
				// to bi-friends, 3: nobody): ", lodField
				"Level of Disclosure: ", lodBox };
		int option = JOptionPane.showConfirmDialog(null, message, "SNS Content Upload", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			strMessage = msgField.getText();
			String strReplyOf = replyOfField.getText();
			if (!strReplyOf.isEmpty())
				nReplyOf = Integer.parseInt(strReplyOf);
			else
				nReplyOf = 0;
			/*
			 * String strLod = lodField.getText(); if(strLod != null)
			 * nLevelOfDisclosure = Integer.parseInt(strLod); else
			 * nLevelOfDisclosure = 0;
			 */
			nLevelOfDisclosure = lodBox.getSelectedIndex();
			System.out.println("selected lod: " + nLevelOfDisclosure);

			if (attachedFilesBox.isSelected()) {
				JFileChooser fc = new JFileChooser();
				fc.setMultiSelectionEnabled(true);
				CMFileTransferInfo fInfo = m_clientStub.getCMInfo().getFileTransferInfo();
				File curDir = new File(fInfo.getFilePath());
				fc.setCurrentDirectory(curDir);
				int fcRet = fc.showOpenDialog(this);
				if (fcRet == JFileChooser.APPROVE_OPTION) {
					files = fc.getSelectedFiles();
					if (files.length > 0) {
						nNumAttachedFiles = files.length;
						filePathList = new ArrayList<String>();
						for (int i = 0; i < nNumAttachedFiles; i++) {
							String strPath = files[i].getPath();
							filePathList.add(strPath);
						}
					}
				}
			}

			String strUser = m_clientStub.getCMInfo().getInteractionInfo().getMyself().getName();
			m_clientStub.requestSNSContentUpload(strUser, strMessage, nNumAttachedFiles, nReplyOf, nLevelOfDisclosure,
					filePathList);
		}

		return;
	}

	public void testRegisterUser() {
		String strName = null;
		String strPasswd = null;
		String strRePasswd = null;
		String strEncPasswd = null;

		/*
		 * Console console = System.console(); if(console == null) {
		 * System.err.println("Unable to obtain console."); }
		 */

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== register a user"); try { System.out.print("Input user name: "
		 * ); strName = br.readLine(); if(console == null) { System.out.print(
		 * "Input password: "); strPasswd = br.readLine(); System.out.print(
		 * "Retype password: "); strRePasswd = br.readLine(); } else { strPasswd
		 * = new String(console.readPassword("Input password: ")); strRePasswd =
		 * new String(console.readPassword("Retype password: ")); }
		 * 
		 * if(!strPasswd.equals(strRePasswd)) { System.err.println(
		 * "Password input error"); return; }
		 * 
		 * } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		printMessage("====== register a user\n");
		JTextField nameField = new JTextField();
		JPasswordField passwordField = new JPasswordField();
		JPasswordField rePasswordField = new JPasswordField();
		Object[] message = { "Input User Name: ", nameField, "Input Password: ", passwordField, "Retype Password: ",
				rePasswordField };
		int option = JOptionPane.showConfirmDialog(null, message, "User Registration", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		strName = nameField.getText();
		strPasswd = new String(passwordField.getPassword()); // security
																// problem?
		strRePasswd = new String(rePasswordField.getPassword());// security
																// problem?

		if (!strPasswd.equals(strRePasswd)) {
			// System.err.println("Password input error");
			printMessage("Password input error!\n");
			return;
		}

		// encrypt password
		strEncPasswd = CMUtil.getSHA1Hash(strPasswd);

		// m_clientStub.registerUser(strName, strPasswd);
		m_clientStub.registerUser(strName, strEncPasswd);
		// System.out.println("======");
		printMessage("======\n");

		return;
	}

	public void testDeregisterUser() {
		String strName = null;
		String strPasswd = null;
		String strEncPasswd = null;

		/*
		 * Console console = System.console(); if(console == null) {
		 * System.err.println("Unable to obtain console."); }
		 * 
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in));
		 * 
		 * System.out.println("====== Deregister a user"); try {
		 * System.out.print("Input user name: "); strName = br.readLine();
		 * if(console == null) { System.out.print("Input password: "); strPasswd
		 * = br.readLine(); } else { strPasswd = new
		 * String(console.readPassword("Input password: ")); } } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		printMessage("====== Deregister a user\n");
		JTextField nameField = new JTextField();
		JPasswordField passwdField = new JPasswordField();
		Object[] message = { "Input User Name: ", nameField, "Input Password: ", passwdField };
		int option = JOptionPane.showConfirmDialog(null, message, "User Deregistration", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		strName = nameField.getText();
		strPasswd = new String(passwdField.getPassword()); // security problem?

		// encrypt password
		strEncPasswd = CMUtil.getSHA1Hash(strPasswd);
		// m_clientStub.deregisterUser(strName, strPasswd);
		m_clientStub.deregisterUser(strName, strEncPasswd);

		// System.out.println("======");
		printMessage("======\n");

		return;
	}

	public void testFindRegisteredUser() {
		String strName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== search for a registered user"); try { System.out.print(
		 * "Input user name: "); strName = br.readLine(); } catch (IOException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */

		printMessage("====== search for a registered user\n");
		strName = JOptionPane.showInputDialog("Input User Name: ");
		if (strName != null)
			m_clientStub.findRegisteredUser(strName);

		// System.out.println("======");
		printMessage("======\n");

		return;
	}

	public void testAddNewFriend() {
		String strFriendName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== add a new friend"); System.out.println(
		 * "A friend must be a registered user in CM"); try { System.out.print(
		 * "Input a friend name: "); strFriendName = br.readLine(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); return; }
		 */

		printMessage("====== add a new friend\n");
		printMessage("A friend must be a registered user in CM\n");
		strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
		if (strFriendName != null)
			m_clientStub.addNewFriend(strFriendName);

		return;
	}

	public void testRemoveFriend() {
		String strFriendName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== remove a friend"); try { System.out.print(
		 * "Input a friend name: "); strFriendName = br.readLine(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); return; }
		 */

		printMessage("====== remove a friend\n");
		strFriendName = JOptionPane.showInputDialog("Input a friend name: ");
		if (strFriendName != null)
			m_clientStub.removeFriend(strFriendName);

		return;
	}

	public void testRequestFriendsList() {
		// System.out.println("====== request current friends list");
		printMessage("====== request current friends list\n");
		m_clientStub.requestFriendsList();
		return;
	}

	public void testRequestFriendRequestersList() {
		// System.out.println("====== request friend requesters list");
		printMessage("====== request friend requesters list\n");
		m_clientStub.requestFriendRequestersList();
		return;
	}

	public void testRequestBiFriendsList() {
		// System.out.println("====== request bi-directional friends list");
		printMessage("====== request bi-directional friends list\n");
		m_clientStub.requestBiFriendsList();
		return;
	}

	public void testRequestServerInfo() {
		// System.out.println("====== request additional server information");
		printMessage("====== request additional server information\n");
		m_clientStub.requestServerInfo();
	}

	public void testConnectToServer() {
		// System.out.println("====== connect to a designated server");
		printMessage("====== connect to a designated server\n");
		String strServerName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.print(
		 * "Input a server name: "); try { strServerName = br.readLine(); }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if (strServerName != null)
			m_clientStub.connectToServer(strServerName);

		return;
	}

	public void testDisconnectFromServer() {
		// System.out.println("===== disconnect from a designated server");
		printMessage("===== disconnect from a designated server\n");

		String strServerName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.print(
		 * "Input a server name: "); try { strServerName = br.readLine(); }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if (strServerName != null)
			m_clientStub.disconnectFromServer(strServerName);

		return;
	}

	public void testLoginServer() {
		String strServerName = null;
		String user = null;
		String password = null;
		String strEncPasswd = null;

		/*
		 * Console console = System.console(); if(console == null) {
		 * System.err.println("Unable to obtain console."); }
		 * 
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in));
		 * 
		 * System.out.println("====== log in to a designated server"); try {
		 * System.out.print("Input server name: "); strServerName =
		 * br.readLine(); if( strServerName.equals("SERVER") ) // login to a
		 * default server { System.out.print("User name: "); user =
		 * br.readLine(); if(console == null) { System.out.print("Password: ");
		 * password = br.readLine(); } else { password = new
		 * String(console.readPassword("Password: ")); } // encrypt password
		 * strEncPasswd = CMUtil.getSHA1Hash(password);
		 * 
		 * //m_clientStub.loginCM(user, password); m_clientStub.loginCM(user,
		 * strEncPasswd); } else // use the login info for the default server {
		 * CMUser myself =
		 * m_clientStub.getCMInfo().getInteractionInfo().getMyself(); user =
		 * myself.getName(); password = myself.getPasswd();
		 * m_clientStub.loginCM(strServerName, user, password); } } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		printMessage("====== log in to a designated server\n");
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if (strServerName == null)
			return;

		if (strServerName.equals("SERVER")) // login to a default server
		{
			JTextField userNameField = new JTextField();
			JPasswordField passwordField = new JPasswordField();
			Object[] message = { "User Name:", userNameField, "Password:", passwordField };
			int option = JOptionPane.showConfirmDialog(null, message, "Login Input", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				user = userNameField.getText();
				String strPassword = new String(passwordField.getPassword()); // security
																				// problem?
				// encrypt password
				strEncPasswd = CMUtil.getSHA1Hash(strPassword);

				m_clientStub.loginCM(user, strEncPasswd);
			}
		} else // use the login info for the default server
		{
			CMUser myself = m_clientStub.getCMInfo().getInteractionInfo().getMyself();
			user = myself.getName();
			password = myself.getPasswd();
			m_clientStub.loginCM(strServerName, user, password);
		}

		// System.out.println("======");
		printMessage("======\n");

		return;
	}

	public void testLogoutServer() {
		String strServerName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== log out from a designated server"); System.out.print(
		 * "Input server name: "); try { strServerName = br.readLine(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		printMessage("====== log out from a designated server\n");
		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if (strServerName != null)
			m_clientStub.logoutCM(strServerName);

		// System.out.println("======");
		printMessage("======\n");
	}

	public void testRequestSessionInfoOfServer() {
		String strServerName = null;
		// System.out.println("====== request session informatino of a
		// designated server");
		printMessage("====== request session informatino of a designated server\n");

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.print("Input server name: "
		 * ); try { strServerName = br.readLine(); } catch (IOException e) { //
		 * TODO Auto-generated catch block e.printStackTrace(); }
		 */

		strServerName = JOptionPane.showInputDialog("Server Name: ");
		if (strServerName != null)
			m_clientStub.requestSessionInfo(strServerName);

		// System.out.println("======");
		printMessage("======\n");

		return;
	}

	public void testJoinSessionOfServer() {
		String strServerName = null;
		String strSessionName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== join a session of a designated server"); try {
		 * System.out.print("Input server name: "); strServerName =
		 * br.readLine(); System.out.print("Input session name: ");
		 * strSessionName = br.readLine(); } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */

		printMessage("====== join a session of a designated server\n");
		JTextField serverField = new JTextField();
		JTextField sessionField = new JTextField();
		Object[] message = { "Server Name", serverField, "Session Name", sessionField };
		int option = JOptionPane.showConfirmDialog(null, message, "Join Session", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			strServerName = serverField.getText();
			strSessionName = sessionField.getText();
			m_clientStub.joinSession(strServerName, strSessionName);
		}

		// System.out.println("======");
		printMessage("======\n");

		return;
	}

	public void testLeaveSessionOfServer() {
		String strServerName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== leave a session of a designated server"); System.out.print(
		 * "Input server name: "); try { strServerName = br.readLine(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		printMessage("====== leave a session of a designated server\n");
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if (strServerName != null)
			m_clientStub.leaveSession(strServerName);

		// System.out.println("======");
		printMessage("======\n");

		return;
	}

	public void testPrintGroupInfoOfServer() {
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMUser myself = interInfo.getMyself();

		String strServerName = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== print group information a designated server");
		 * System.out.print("Input server name: "); try { strServerName =
		 * br.readLine(); } catch (IOException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 */

		printMessage("====== print group information a designated server\n");
		strServerName = JOptionPane.showInputDialog("Input a server name: ");
		if (strServerName == null)
			return;

		if (strServerName.equals("SERVER")) {
			testPrintGroupInfo();
			return;
		}

		CMServer server = interInfo.findAddServer(strServerName);
		if (server == null) {
			// System.out.println("server("+strServerName+") not found in the
			// add-server list!");
			printMessage("server(" + strServerName + ") not found in the add-server list!\n");
			return;
		}

		CMSession session = server.findSession(myself.getCurrentSession());
		Iterator<CMGroup> iter = session.getGroupList().iterator();
		// System.out.println("---------------------------------------------------------");
		// System.out.format("%-20s%-20s%-20s%n", "group name", "multicast
		// addr", "multicast port");
		// System.out.println("---------------------------------------------------------");
		printMessage("---------------------------------------------------------\n");
		printMessage(String.format("%-20s%-20s%-20s%n", "group name", "multicast addr", "multicast port"));
		printMessage("---------------------------------------------------------\n");

		while (iter.hasNext()) {
			CMGroupInfo gInfo = iter.next();
			// System.out.format("%-20s%-20s%-20d%n", gInfo.getGroupName(),
			// gInfo.getGroupAddress()
			// , gInfo.getGroupPort());

			printMessage(String.format("%-20s%-20s%-20d%n", gInfo.getGroupName(), gInfo.getGroupAddress(),
					gInfo.getGroupPort()));
		}

		return;
	}

	public void testSendMultipleFiles() {
		String[] strFiles = null;
		String strFileList = null;
		int nMode = -1; // 1: push, 2: pull
		int nFileNum = -1;
		String strTarget = null;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.println(
		 * "====== pull/push multiple files"); try { System.out.print(
		 * "Select mode (1: push, 2: pull): "); nMode =
		 * Integer.parseInt(br.readLine()); if(nMode == 1) { System.out.print(
		 * "Input receiver name: "); strTarget = br.readLine(); } else if(nMode
		 * == 2) { System.out.print("Input file owner name: "); strTarget =
		 * br.readLine(); } else { System.out.println(
		 * "Incorrect transmission mode!"); return; }
		 * 
		 * System.out.print("Number of files: "); nFileNum =
		 * Integer.parseInt(br.readLine()); System.out.print(
		 * "Input file names separated with space: "); strFileList =
		 * br.readLine();
		 * 
		 * } catch (NumberFormatException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); return; } catch (IOException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); return; }
		 */

		printMessage("====== pull/push multiple files\n");

		String[] modes = { "Push", "Pull" };
		JComboBox<String> modeBox = new JComboBox<String>(modes);
		JTextField targetField = new JTextField();
		JTextField fileNumField = new JTextField();
		JTextField fileNamesField = new JTextField();
		Object[] message = { "Transmission Mode", modeBox, "File Receiver or Owner", targetField, "Number of Files",
				fileNumField, "File Names Separated with Space", fileNamesField };
		int option = JOptionPane.showConfirmDialog(null, message, "Push/Pull Multiple Files",
				JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		nMode = modeBox.getSelectedIndex();
		strTarget = targetField.getText();
		try {
			nFileNum = Integer.parseInt(fileNumField.getText());
		} catch (NumberFormatException e) {
			printMessage("Number of files must be an integer!\n");
			return;
		}
		strFileList = fileNamesField.getText();

		strFileList.trim();
		strFiles = strFileList.split("\\s+");
		if (strFiles.length != nFileNum) {
			// System.out.println("The number of files incorrect!");
			printMessage("The number of files incorrect!\n");
			return;
		}

		for (int i = 0; i < nFileNum; i++) {
			switch (nMode) {
			case 0: // push
				CMFileTransferManager.pushFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
				break;
			case 1: // pull
				CMFileTransferManager.requestFile(strFiles[i], strTarget, m_clientStub.getCMInfo());
				break;
			}
		}

		return;
	}

	public void testSplitFile() {
		String strSrcFile = null;
		String strSplitFile = null;
		long lFileSize = -1;
		long lFileOffset = 0;
		long lSplitSize = -1;
		long lSplitRemainder = -1;
		int nSplitNum = -1;
		RandomAccessFile raf = null;
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));
		// System.out.println("====== split a file");

		printMessage("====== split a file\n");

		try {
			/*
			 * System.out.print("Input source file name: "); strSrcFile =
			 * br.readLine(); System.out.print(
			 * "Input the number of split files: "); nSplitNum =
			 * Integer.parseInt(br.readLine());
			 */

			JTextField fileField = new JTextField();
			JTextField splitNumField = new JTextField();
			Object[] message = { "Source File Name", fileField, "Number of Split Files", splitNumField };
			int option = JOptionPane.showConfirmDialog(null, message, "Split a File", JOptionPane.OK_CANCEL_OPTION);
			if (option != JOptionPane.OK_OPTION)
				return;
			strSrcFile = fileField.getText();
			try {
				nSplitNum = Integer.parseInt(splitNumField.getText());
			} catch (NumberFormatException ne) {
				printMessage("Number of split files must be an integer!");
				return;
			}

			raf = new RandomAccessFile(strSrcFile, "r");
			lFileSize = raf.length();

			lSplitSize = lFileSize / nSplitNum;
			lSplitRemainder = lFileSize % lSplitSize;

			for (int i = 0; i < nSplitNum; i++) {
				// get the name of split file ('srcfile'-i.split)
				int index = strSrcFile.lastIndexOf(".");
				strSplitFile = strSrcFile.substring(0, index) + "-" + (i + 1) + ".split";

				// update offset
				lFileOffset = i * lSplitSize;

				if (i + 1 != nSplitNum)
					CMFileTransferManager.splitFile(raf, lFileOffset, lSplitSize, strSplitFile);
				else
					CMFileTransferManager.splitFile(raf, lFileOffset, lSplitSize + lSplitRemainder, strSplitFile);

			}

			raf.close();
		} catch (FileNotFoundException fe) {
			fe.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		return;
	}

	public void testMergeFiles() {
		String[] strFiles = null;
		// String strFileList = null;
		String strFilePrefix = null;
		String strMergeFileName = null;
		int nFileNum = -1;
		long lMergeFileSize = -1;

		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in));
		 * 
		 * System.out.println("====== merge split files"); try {
		 * System.out.print("Number of split files: "); nFileNum =
		 * Integer.parseInt(br.readLine()); //System.out.print(
		 * "Input split files in order: "); //strFileList = br.readLine();
		 * System.out.print("Input prefix of split files: "); strFilePrefix =
		 * br.readLine(); System.out.print("Input merged file name: ");
		 * strMergeFileName = br.readLine(); } catch (NumberFormatException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); return; }
		 * catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); return; }
		 */

		printMessage("====== merge split files\n");

		JTextField splitNumField = new JTextField();
		JTextField prefixField = new JTextField();
		JTextField mergeFileNameField = new JTextField();
		Object[] message = { "Number of split files", splitNumField, "Prefix name of split files", prefixField,
				"Merge file name", mergeFileNameField };
		int option = JOptionPane.showConfirmDialog(null, message, "Merge Split Files", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;
		try {
			nFileNum = Integer.parseInt(splitNumField.getText());
		} catch (NumberFormatException e) {
			printMessage("Number of split files must be an integer!");
			return;
		}
		strFilePrefix = prefixField.getText();
		strMergeFileName = mergeFileNameField.getText();

		// make list of split file names
		strFiles = new String[nFileNum];
		for (int i = 0; i < nFileNum; i++) {
			strFiles[i] = strFilePrefix + "-" + (i + 1) + ".split";
		}

		lMergeFileSize = CMFileTransferManager.mergeFiles(strFiles, nFileNum, strMergeFileName);
		// System.out.println("Size of merged file("+strMergeFileName+"):
		// "+lMergeFileSize+" Bytes.");
		printMessage("Size of merged file(" + strMergeFileName + "): " + lMergeFileSize + " Bytes.\n");
		return;
	}

	public void testDistFileProc() {
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMFileTransferInfo fileInfo = m_clientStub.getCMInfo().getFileTransferInfo();
		String strFile = null;
		long lFileSize = 0;
		CMFileEvent fe = null;
		// BufferedReader br = new BufferedReader(new
		// InputStreamReader(System.in));

		// System.out.println("====== split a file, distribute to multiple
		// servers, and merge");
		printMessage("====== split a file, distribute to multiple servers, and merge\n");

		// check if the client logs in to all available servers
		int nClientState = interInfo.getMyself().getState();
		if (nClientState == CMInfo.CM_INIT || nClientState == CMInfo.CM_CONNECT) {
			// System.out.println("You must log in the default server!");
			printMessage("You must log in the default server!\n");
			return;
		}
		Iterator<CMServer> iter = interInfo.getAddServerList().iterator();
		while (iter.hasNext()) {
			CMServer tserver = iter.next();
			nClientState = tserver.getClientState();
			if (nClientState == CMInfo.CM_INIT || nClientState == CMInfo.CM_CONNECT) {
				// System.out.println("You must log in the additional
				// server("+tserver.getServerName()
				// +")!");
				printMessage("You must log in the additional server(" + tserver.getServerName() + ")!\n");
				return;
			}
		}

		// input file name
		/*
		 * try { System.out.println(
		 * "A source file must exists in the file path configured in CM");
		 * System.out.print("Input a source file name: "); strFile =
		 * br.readLine(); } catch (IOException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); }
		 */
		// printMessage("A source file must exists in the file path configured
		// in CM\n");
		strFile = JOptionPane.showInputDialog("Source file path");
		if (strFile == null)
			return;

		// print the file size
		// strFile = fileInfo.getFilePath()+"/"+strFile;
		File srcFile = new File(strFile);
		lFileSize = srcFile.length();
		// System.out.println("Source file ("+strFile+"): "+lFileSize+"
		// Bytes.");
		printMessage("Source file (" + strFile + "): " + lFileSize + " Bytes.\n");

		// get current number of servers ( default server + add servers )
		m_eventHandler.setCurrentServerNum(interInfo.getAddServerList().size() + 1);
		String[] filePieces = new String[interInfo.getAddServerList().size() + 1];
		m_eventHandler.setFilePieces(filePieces);

		// initialize the number of modified pieces
		m_eventHandler.setRecvPieceNum(0);

		// set m_bDistSendRecv to true
		m_eventHandler.setDistFileProc(true);

		// set send time
		m_eventHandler.setStartTime(System.currentTimeMillis());

		// extract the extension of the file
		String strPrefix = null;
		String strExt = null;
		int index = strFile.lastIndexOf(".");
		strPrefix = strFile.substring(0, index);
		strExt = strFile.substring(index + 1);
		m_eventHandler.setFileExtension(strExt);
		// System.out.println("Source file extension:
		// "+m_eventHandler.getFileExtension());
		printMessage("Source file extension: " + m_eventHandler.getFileExtension() + "\n");

		// split a file into pieces with the number of servers. each piece has
		// the name of 'file name'-x.split
		// and send each piece to different server
		long lPieceSize = lFileSize / m_eventHandler.getCurrentServerNum();
		int i = 0;
		String strPieceName = null;
		long lOffset = 0;
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(strFile, "r");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		// make a file event (REQUEST_DIST_FILE_PROC)
		fe = new CMFileEvent();
		fe.setID(CMFileEvent.REQUEST_DIST_FILE_PROC);
		fe.setUserName(interInfo.getMyself().getName());

		// for pieces except the last piece
		for (i = 0; i < m_eventHandler.getCurrentServerNum() - 1; i++) {
			// get the piece name
			strPieceName = strPrefix + "-" + (i + 1) + ".split";
			// System.out.println("File piece name: "+strPieceName);
			printMessage("File piece name: " + strPieceName + "\n");

			// split the file with a piece
			CMFileTransferManager.splitFile(raf, lOffset, lPieceSize, strPieceName);
			// update offset
			lOffset += lPieceSize;

			// send piece to the corresponding additional server
			String strAddServer = interInfo.getAddServerList().elementAt(i).getServerName();

			m_clientStub.send(fe, strAddServer);

			CMFileTransferManager.pushFile(strPieceName, strAddServer, m_clientStub.getCMInfo());
		}
		// for the last piece
		if (i == 0) {
			// no split
			strPieceName = strFile;
		} else {
			// get the last piece name
			strPieceName = strPrefix + "-" + (i + 1) + ".split";
			// System.out.println("File piece name: "+strPieceName);
			printMessage("File piece name: " + strPieceName + "\n");

			// get the last piece
			CMFileTransferManager.splitFile(raf, lOffset, lFileSize - lPieceSize * i, strPieceName);
		}
		// send the last piece to the default server
		m_clientStub.send(fe, "SERVER");
		CMFileTransferManager.pushFile(strPieceName, "SERVER", m_clientStub.getCMInfo());

		try {
			raf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// The next process proceeds when a modified piece is transferred from a
		// server.

		// Whenever a modified piece(m-'file name'-x.split) is transferred, if
		// m_bDistSendRecv is true,
		// increase the number of pieces and its name is stored in an array.
		// When all modified pieces arrive, they are merged to a file (m-'file
		// name').
		// After the file is merged, set the received time, calculate the
		// elapsed time, set m_bDistSendRecv to false
		// and print the result.

		fe = null;
		return;
	}

	public void testMulticastChat() {
		CMInteractionInfo interInfo = m_clientStub.getCMInfo().getInteractionInfo();
		CMConfigurationInfo confInfo = m_clientStub.getCMInfo().getConfigurationInfo();
		// System.out.println("====== test multicast chat in current group");
		printMessage("====== test multicast chat in current group\n");

		// check user state
		CMUser myself = interInfo.getMyself();
		if (myself.getState() != CMInfo.CM_SESSION_JOIN) {
			// System.out.println("You must join a session and a group for
			// multicasting.");
			printMessage("You must join a session and a group for multicasting.\n");
			return;
		}

		// check communication architecture
		if (!confInfo.getCommArch().equals("CM_PS")) {
			// System.out.println("CM must start with CM_PS mode which enables
			// multicast per group!");
			printMessage("CM must start with CM_PS mode which enables multicast per group!\n");
			return;
		}

		// receive a user input message
		/*
		 * BufferedReader br = new BufferedReader(new
		 * InputStreamReader(System.in)); System.out.print("Input message: ");
		 * String strMessage = null; try { strMessage = br.readLine(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		String strMessage = JOptionPane.showInputDialog("Chat Message");
		if (strMessage == null)
			return;

		// make a CMInterestEvent.USER_TALK event
		CMInterestEvent ie = new CMInterestEvent();
		ie.setID(CMInterestEvent.USER_TALK);
		ie.setHandlerSession(myself.getCurrentSession());
		ie.setHandlerGroup(myself.getCurrentGroup());
		ie.setUserName(myself.getName());
		ie.setTalk(strMessage);

		m_clientStub.multicast(ie, myself.getCurrentSession(), myself.getCurrentGroup());

		ie = null;
		return;
	}

	private void requestAttachedFile(String strFileName) {
		/*
		 * int nContentID = -1; String strWriterName = null; // A downloaded
		 * file name may be a thumbnail file name instead of original name int
		 * index = strFileName.lastIndexOf("."); String strThumbnail =
		 * strFileName.substring(0, index) + "-thumbnail" +
		 * strFileName.substring(index, strFileName.length()); // search for
		 * content ID and writer name CMSNSInfo snsInfo =
		 * m_clientStub.getCMInfo().getSNSInfo(); CMSNSContentList contentList =
		 * snsInfo.getSNSContentList(); Vector<CMSNSContent> contentVector =
		 * contentList.getContentList(); Iterator<CMSNSContent> iter =
		 * contentVector.iterator(); boolean bFound = false;
		 * while(iter.hasNext() && !bFound) { CMSNSContent content =
		 * iter.next(); if(content.containsFileName(strFileName) ||
		 * content.containsFileName(strThumbnail)) { nContentID =
		 * content.getContentID(); strWriterName = content.getWriterName();
		 * bFound = true; } }
		 * 
		 * if(bFound) { // set a flag of the request
		 * m_eventHandler.setReqAttachedFile(true); // send request for the
		 * attachment download
		 * m_clientStub.requestAttachedFileOfSNSContent(nContentID,
		 * strWriterName, strFileName); } else { printMessage(strFileName+
		 * " not found in the downloaded content list!\n"); }
		 */

		boolean bRet = m_clientStub.requestAttachedFileOfSNSContent(strFileName);
		if (bRet)
			m_eventHandler.setReqAttachedFile(true);
		else
			printMessage(strFileName + " not found in the downloaded content list!\n");

		return;
	}

	private void accessAttachedFile(String strFileName) {
		boolean bRet = m_clientStub.accessAttachedFileOfSNSContent(strFileName);
		if (!bRet)
			printMessage(strFileName + " not found in the downloaded content list!\n");

		return;
	}

	public class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			int key = e.getKeyCode();
			if (key == KeyEvent.VK_ENTER) {
				JTextField input = (JTextField) e.getSource();
				String strText = input.getText();
				printMessage(strText + "\n");
				// parse and call CM API
				processInput(strText);
				input.setText("");
				input.requestFocus();
			}
		}

		public void keyReleased(KeyEvent e) {
		}

		public void keyTyped(KeyEvent e) {
		}
	}

	public class MyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			if (button.getText().equals("Start Client CM")) {
				// start cm
				boolean bRet = m_clientStub.startCM();
				if (!bRet) {
					printStyledMessage("CM initialization error!\n", "bold");
				} else {
					printStyledMessage("Client CM starts.\n", "bold");
					printStyledMessage("Type \"0\" for menu.\n", "regular");
					// change button to "stop CM"
					button.setText("Stop Client CM");
				}
				m_inTextField.requestFocus();
			} else if (button.getText().equals("Stop Client CM")) {
				m_clientStub.disconnectFromServer();
				// stop cm
				m_clientStub.terminateCM();
				printMessage("Client CM terminates.\n");
				// change button to "start CM"
				button.setText("Start Client CM");

				setTitle("CM Client");
			}
		}
	}

	public class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.getSource() instanceof JLabel) {
				JLabel pathLabel = (JLabel) e.getSource();
				String strPath = pathLabel.getText();
				File fPath = new File(strPath);
				try {
					int index = strPath.lastIndexOf("/");
					String strFileName = strPath.substring(index + 1, strPath.length());
					if (fPath.exists()) {
						accessAttachedFile(strFileName);
						Desktop.getDesktop().open(fPath);
					} else {
						requestAttachedFile(strFileName);
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.getSource() instanceof JLabel) {
				Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				setCursor(cursor);
			}

		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			if (e.getSource() instanceof JLabel) {
				Cursor cursor = Cursor.getDefaultCursor();
				setCursor(cursor);
			}
		}

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
		EPAdministrator admin = epService.getEPAdministrator();

		String ChatEvents = ChatEventType.class.getName();
		admin.getConfiguration().addEventType("ChatEvents", ChatEvents);

		String creatContext = "create context Chats partition by userName "
				+ "and chatMessage from ChatEvents";
		String selectInfo = "context Chats select userName, chatMessage, groupName, "
				+ "count(c) from ChatEvents.win:time(24 hour) as c";
		
		admin.createEPL(creatContext);
		EPStatement state = admin.createEPL(selectInfo);
		state.addListener(new CMChatMessageListener());
		
		
		CMWinClient client = new CMWinClient();
		CMClientStub cmStub = client.getClientStub();
		cmStub.setEventHandler(client.getClientEventHandler());

	}

}
