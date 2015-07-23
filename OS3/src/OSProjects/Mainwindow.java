package OSProjects;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;

public class Mainwindow {
	public static void main(String[] args) {
		FileFrame frame = new FileFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.show();
	}
}

class FileFrame extends JFrame {
	public FileManager fileManager = new FileManager();
	public FileFrame() {
		setSize(Parameter.WIDTH, Parameter.HEIGHT);
		setTitle("OS document system management 1352977 liwendi");

		FilePanel filePanel = new FilePanel(fileManager);
		Container contentPane = getContentPane();
		contentPane.add(filePanel);

		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent arg0) {
				try {
					DataOutputStream out =
						new DataOutputStream(
							new BufferedOutputStream(
								new FileOutputStream("deploy.ini")));
					for (int i = 0; i < Parameter.NUM_OF_ROOTFILE; i++) {
						for (int j = 0; j < Parameter.SIZE_OF_FILEINFO; j++) {
							out.writeChar(fileManager.rootTable[i][j]);
						}

					}

					for (int i = 0; i < Parameter.NUM_OF_DATASECTOR; i++) {
						out.writeChar(fileManager.fatTable[i]);
					}

					for (int i = 0; i < Parameter.NUM_OF_DATASECTOR; i++) {
						for (int j = 0; j < Parameter.SIZE_OF_SECTOR; j++) {
							out.writeChar(fileManager.dataArea[i][j]);
						}
					}
					out.close();

				} catch (Exception e) {
					System.out.println(e);
				}
				System.out.println("windowClosing");
			}

			public void windowOpened(WindowEvent arg0) {
				try {
					DataInputStream in =
						new DataInputStream(
							new BufferedInputStream(
								new FileInputStream("deploy.ini")));

					for (int i = 0; i < Parameter.NUM_OF_ROOTFILE; i++) {
						for (int j = 0; j < Parameter.SIZE_OF_FILEINFO; j++) {
							fileManager.rootTable[i][j] = in.readChar();
						}

					}

					for (int i = 0; i < Parameter.NUM_OF_DATASECTOR; i++) {
						fileManager.fatTable[i] = in.readChar();
					}

					for (int j = 0; j < Parameter.NUM_OF_DATASECTOR; j++) {
						for (int i = 0; i < Parameter.SIZE_OF_SECTOR; i++) {
							fileManager.dataArea[j][i] = in.readChar();
						}
					}
					in.close();

				} catch (Exception e) {
					System.out.println(e);
				}
			}
		});
		
	}
}

class FilePanel extends JPanel {
	private JFrame frame;
	private FileEditor fileEditor;
	private JTextArea textOutput;
	private JTextField textInput;
	private String currentPath = "cmd:\\>";
	private FileManager fileManager;

	public FilePanel(FileManager fileManager) {
		this.fileManager = fileManager;

		setLayout(new BorderLayout());
		Border brd = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK);

		textInput = new JTextField();
		textInput.setBorder(brd);
		textInput.setBackground(Color.BLACK);
		textInput.setForeground(Color.WHITE);

		KeyHandler KeyListener = new KeyHandler();
		textInput.addKeyListener(KeyListener);
		textInput.setFont(new Font("Verdana", Font.BOLD, 18));
		textInput.setFocusable(true);

		JLabel label = new JLabel(" Type Command here: ");
		label.setFont(new Font("Times New Roman", Font.BOLD, 15));
		label.setBorder(brd);
		label.setForeground(Color.black);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.WEST);
		panel.add(textInput);

		this.add(panel, BorderLayout.SOUTH);

		textOutput = new JTextArea();
		textOutput.setBorder(brd);
		textOutput.setLineWrap(true);
		textOutput.setWrapStyleWord(true);
		textOutput.setFocusable(false);
		textOutput.setBackground(Color.BLACK);
		textOutput.setForeground(Color.WHITE);
		textOutput.setFont(new Font("Verdana", Font.BOLD, 15));
		textOutput.append(currentPath);

		JScrollPane spOutput =
			new JScrollPane(
				textOutput,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.add(spOutput);
	}

	private class KeyHandler implements KeyListener {
		public void keyPressed(KeyEvent event) {
			int keyCode = event.getKeyCode();
			if (keyCode == KeyEvent.VK_ENTER
				&& textInput.getText().compareTo("") != 0) {
				handerInput(textInput.getText());
				textInput.setText("");
				textOutput.append(currentPath);
			}
		}
		public void keyReleased(KeyEvent event) {
		}

		public void keyTyped(KeyEvent event) {
		}
	}

	private void handerInput(String cmd) {
		textOutput.append(cmd + "\n");
		String cmdHead, cmdBody;

		int pos = cmd.indexOf(" ");
		if (pos == -1) {
			cmdHead = cmd;
			cmdBody = "";
		} else {
			cmdHead = cmd.substring(0, pos);
			cmdBody = cmd.substring(pos).trim();
		}
		cmdHead = cmdHead.toLowerCase();
		cmdBody = cmdBody.toLowerCase();
		
		if (cmdHead.compareTo("chd") == 0) {
			handleChD(cmdBody);
		} else if (
			cmdHead.substring(0, 2).compareTo("cd") == 0
				&& cmdBody.compareTo("") == 0) {
			if (cmdHead.substring(2).trim().compareTo("\\") == 0
				|| cmdHead.substring(2).trim().compareTo("..") == 0
				|| cmdHead.substring(2).trim().compareTo(".") == 0) {
				String temp = cmdHead.substring(2).trim();
				System.out.println("here! " + temp);
				handleChD(temp);
			} else {
				textOutput.append(
					"'"
						+ cmd
						+ "' is not a executable command , input 'help' to get some tips  ");
			}
		} else if (cmdHead.compareTo("dir") == 0 && cmdBody.compareTo("") == 0) {
			handleDir();
		} else if (cmdHead.compareTo("help") == 0) {
			handleHelp();
		} else if (cmdHead.compareTo("nd") == 0) {
			handleNd(cmdBody);
		} else if (cmdHead.compareTo("dd") == 0) {
			handleDd(cmdBody);
		} else if (cmdHead.compareTo("nf") == 0) {
			handleNf(cmdBody);
		} else if (cmdHead.compareTo("df") == 0) {
			handleDf(cmdBody);
		} else if (cmdHead.compareTo("edit") == 0) {
			handleEdit(cmdBody);
		} else if (cmdHead.compareTo("format") == 0) {
			handleFormat();
		} else {
			textOutput.append(
				"'"
					+ cmd
					+ "' is not a executable command , input 'help' to get some tips  ");
		}
		textOutput.append("\n\n");
		textOutput.setCaretPosition(textOutput.getText().length());
	}

	void handleChD(String para) {

		if (fileManager.changeDirectory(para) == true) {
			this.currentPath = fileManager.getCurrentPath();
		} else {
			textOutput.append("The subDirectory doesn't exist!");
		}

	}
	void handleDir() {
		ArrayList fileList = fileManager.getCurrentDirInfo();
		String name;
		char type;
		textOutput.append("-------LIST OF FILES & DIRECTORIES-------\n");
		int i;
		for (i = 0; i < fileList.size(); i++) {
			if (i != 0)
				textOutput.append("\n");

			String fileInfo = (String) fileList.get(i);
			name =
				fileInfo
					.substring(
							Parameter.POS_NAME,
							Parameter.POS_NAME + Parameter.LEN_OF_NAME)
					.trim();
			type = fileInfo.charAt(Parameter.POS_STATE);
			
			if (type == Parameter.DIRECTORY) {
				textOutput.append("     <DIR>       ");
			} else {
				textOutput.append("                        ");
			}
			textOutput.append(name);
		}
	}
	void handleHelp() {
		textOutput.append(
			"FORMAT        Formats the disk for use.\n"
				+ "HELP             Provides Help information for my filemanager.\n"
				+ "DIR               Open the files and subdirectories in a directory.\n"
				+ "ChD             changes the current directory.\n"
				+ "                          ( '.' or '..' or '\\' or name of the directory)\n"
				+ "ND **           New a directory named **.\n"
				+ "DD **           Delete a directory named **.\n"
				+ "NF **           New a file named **.\n"
				+ "DF **           Delete a file named **.\n"
				+ "Edit               Edit a file.\n");
	}
	void handleNd(String para) {
		if (para.length() == 0) {
			textOutput.append("Please input the name of the directory!");
			return;
		}
		if (para.length() >= 12) {
			textOutput.append(
				"Create Fail:\nThe length of the name should between 1 and 12!");
			return;
		}
		if (fileManager.createInfo(Parameter.DIRECTORY, para) == false) {
			textOutput.append(
				"Create Fail:\nNames collide!Please input other name!");
			return;
		}
		textOutput.append(
			"Create the SubDirectory '" + para + "' successfully!");
	}
	void handleDd(String para) {
		if (para.length() == 0) {
			textOutput.append("Please input the name of the directory!");
			return;
		}
		if (fileManager.deleteInfo(Parameter.DIRECTORY, para) == true) {
			textOutput.append(
				"Delete the SubDirectory '" + para + "' successfully!");
		} else {
			textOutput.append("The SubDirectory '" + para + "' doesn't exist!");
		}
	}
	void handleNf(String para) {
		if (para.length() == 0) {
			textOutput.append("Please input the name of the file!");
			return;
		}
		if (para.length() >= 12) {
			textOutput.append(
				"Create Fail:\nThe length of the name should between 1 and 12!");
			return;
		}
		if (fileManager.createInfo(Parameter.FILE, para) == false) {
			textOutput.append(
				"Create Fail:\nNames collide!Please input other name!");
			return;
		}
		textOutput.append("Create the file '" + para + "' successfully!");
	}
	void handleDf(String para) {
		if (para.length() == 0) {
			textOutput.append("Please input the name of the file!");
			return;
		}
		if (fileManager.deleteInfo(Parameter.FILE, para) == true) {
			textOutput.append("Delete the file '" + para + "' successfully!");
		} else {
			textOutput.append("The file '" + para + "' doesn't exist!");
		}
	}

	void handleEdit(String para) {
		if (para.length() == 0) {
			textOutput.append("Please input the name of the file!");
			return;
		}
		StringBuffer content = new StringBuffer();
		if (fileManager.loadFile(para, content) == true) {
			fileEditor = new FileEditor(null, para);
			fileEditor.textArea.setText(content.toString());
			fileEditor.show();
		} else {
			textOutput.append("'" + para + "' doesn't exist!");
		}

	}

	void handleFormat() {
		fileManager.formatAll();
		textOutput.append("Format the File System sussfully!");
	}

	class FileEditor extends JDialog {
		JTextArea textArea = new JTextArea();
		JButton save = new JButton("Save");
		JButton cancel = new JButton("Canel");
		String filename;

		public FileEditor(JFrame frame, String name) {
			super(frame, name, true);
			setSize(430, 430);
			setLocation(400, 150);
			setResizable(false);
			this.filename = name;

			Border brd =
				BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK);
			textArea.setBorder(brd);
			textArea.setBackground(Color.WHITE);
			textArea.setFont(new Font("Arial", Font.TRUETYPE_FONT, 25));
			textArea.setLineWrap(true);
			ButtonListener listener = new ButtonListener();
			save.addActionListener(listener);
			cancel.addActionListener(listener);

			JScrollPane spEdit =
				new JScrollPane(
					textArea,
					JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

			JPanel btPanel = new JPanel();
			btPanel.add(save);
			btPanel.add(cancel);

			Container container = getContentPane();
			container.setLayout(new BorderLayout());
			container.add(btPanel, BorderLayout.SOUTH);
			container.add(spEdit);
		}

		class ButtonListener implements ActionListener {
			public void actionPerformed(ActionEvent e) {
				if ((JButton) e.getSource() == save) {
					String content = textArea.getText();
					if (fileManager.writeFile(filename, content) == true) {
						dispose();
						return;
					}
				} else {
					dispose();
					return;
				}
			}
		}
	}
}
