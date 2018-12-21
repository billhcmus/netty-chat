import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 09/09/2018
 * Time: 09:29
 */
public class ChatGUI extends JFrame {
    private JPanel panelChat;
    private JFormattedTextField txtMessage;
    private JButton btnSend;
    private JTextArea msgArea;
    private JButton btnCreateChannel;
    private JButton btnListChannel;
    private JList listChannels;
    private JList listUsers;
    private JButton btnGetListUsers;
    private JButton btnJoinChannel;
    private JButton btnLogout;
    private JScrollPane scrChannel;

    public static JFrame chatFrame = new JFrame();
    private String username;
    ChatGUI() {
        chatFrame.setContentPane(panelChat);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.pack();
        handleEventSendClick();
        handleEventCreateChannelClick();
    }

    public ChatGUI(String username) {
        this.username = username;
        chatFrame.setTitle(username);
        chatFrame.setContentPane(panelChat);
        chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        chatFrame.pack();
        chatFrame.setSize(600, 400);
        chatFrame.setLocationRelativeTo(null);

        handleEventSendClick();
        handleEventCreateChannelClick();
        handleEventGetListChannelClick();
        handleEventGetListUsersClick();
        handleEventSelectionListChannel();
        handleEventSelectionListUsers();
        handleEventJoinChannel();
        handleEventLogoutClick();
    }

    private void handleEventLogoutClick() {
        btnLogout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ClientHandler.LogOut();
            }
        });
    }

    private void handleEventJoinChannel() {
        btnJoinChannel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JoinChannel dialog = new JoinChannel();
                dialog.pack();
                dialog.setVisible(true);

                String channelName = dialog.ChannelJoinName;
                if (channelName != "") {
                    ClientHandler.joinChannel(channelName);
                }
            }
        });
    }

    private void handleEventSelectionListUsers() {
        listUsers.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {

                if (!listSelectionEvent.getValueIsAdjusting()) {
                    listChannels.clearSelection();
                    try {
                        if (listUsers.getSelectedValue() != null) {
                            ClientHandler.getUserMessage(listUsers.getSelectedValue().toString());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void handleEventSelectionListChannel() {
        listChannels.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                listUsers.clearSelection();
                if (!listSelectionEvent.getValueIsAdjusting()) {
                    try {
                        if (listChannels.getSelectedValue() != null) {
                            ClientHandler.getChannelMessage(listChannels.getSelectedValue().toString());
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void handleEventGetListUsersClick() {
        btnGetListUsers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ClientHandler.getListUsers();
            }
        });
    }

    private void handleEventGetListChannelClick() {
        btnListChannel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ClientHandler.getListChannels();
            }
        });
    }

    private void handleEventCreateChannelClick() {
        btnCreateChannel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                CreateChannel dialog = new CreateChannel();
                dialog.pack();
                dialog.setVisible(true);
                String channelName = dialog.channelName;
                if (channelName != "") {
                    ClientHandler.createChannel(channelName);
                }
            }
        });
    }

    public void addChannelsList(List<String> channelsList) {
        DefaultListModel model = new DefaultListModel();
        for (String channel : channelsList) {
            model.addElement(channel);
        }
        if (model.size() != 0) {
            listChannels.setModel(model);
            listChannels.setSelectedIndex(0);
        }
    }

    public void addUsersList(List<String> users) {
        DefaultListModel model = new DefaultListModel();
        for (String user: users) {
            model.addElement(user);
        }
        if (model.size() != 0) {
            listUsers.setModel(model);
            listChannels.setSelectedIndex(0);
        }
    }

    public void addMessage(String message, String from, String channelName) {
        if (channelName != "") {
            listChannels.setSelectedValue(channelName, true);
        } else {
            listUsers.setSelectedValue(from, true);
            try {
                ClientHandler.getUserMessage(from);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        this.msgArea.append(from + ": ");
        this.msgArea.append(message + "\n");
    }

    public void clearMessageArea() {
        this.msgArea.setText("");
    }

    public void addAllMessageToArea(String conversation) {
        this.msgArea.append(conversation);
    }

    public void handleEventSendClick() {
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String receiver = null;
                if (!listChannels.isSelectionEmpty()) {
                    receiver = listChannels.getSelectedValue().toString();
                }
                if (!listUsers.isSelectionEmpty()) {
                    receiver = listUsers.getSelectedValue().toString();
                }

                if (receiver != null && txtMessage.getText() != "") {
                    ClientHandler.sendMessage(txtMessage.getText(), receiver);
                    msgArea.append(username + ": " + txtMessage.getText() + "\n");
                    txtMessage.setValue("");
                }
            }
        });
    }

    public static void main(String[] args) {

    }
}
