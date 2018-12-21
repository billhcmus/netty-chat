import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 * User: Thuyen Phan
 * Date: 09/09/2018
 * Time: 16:12
 */
public class Login {
    private JPanel panelLogin;
    private JLabel lblUsername;
    private JLabel lblPassword;
    private JButton btnLogin;
    private JButton btnSignUp;
    private JPasswordField pwPassword;
    private JFormattedTextField txtUsername;


    public static JFrame Loginframe = new JFrame("Login");

    Login() {
        Loginframe.setContentPane(panelLogin);
        Loginframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Loginframe.pack();
        Loginframe.setVisible(true);
        Loginframe.setLocationRelativeTo(null);
        handleEventLoginClick();
        handleEventSignUpClick();
        try {
            new NettyClient().Run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handleEventLoginClick() {
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String username = txtUsername.getText();
                try {
                    ClientHandler.processLogin(username, String.valueOf(pwPassword.getPassword()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void handleEventSignUpClick() {
        btnSignUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                String username = txtUsername.getText();
                String password = String.valueOf(pwPassword.getPassword());
                try {
                    ClientHandler.processSignUp(username, password);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        System.err.close();
        System.setErr(System.out);
        new Login();
    }
}
