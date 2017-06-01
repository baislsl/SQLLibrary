package gui;

import util.SQLDate;
import util.User;
import mysql.SQLConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by baislsl on 17-4-29.
 */
public class CreateNewUserShell {
    private Text userNameText, passwordText, comfiredPasswordText, mailText;
    private Shell shell;
    private Display display;
    private Label console;
    private SQLConnection connect;
    private Properties props;

    public CreateNewUserShell(Display display, SQLConnection connect, Properties props) {
        this.shell = new Shell(display);
        this.display = display;
        this.connect = connect;
        this.props = props;
        init();
    }

    private void init() {
        shell.setSize(600, 400);
        shell.setText(props.getProperty("menu.option.create_user"));
        Label nameLabel = new Label(shell, SWT.NONE);
        nameLabel.setText(props.getProperty("user.id"));
        nameLabel.setBounds(20, 50, 100, 20);
        userNameText = new Text(shell, SWT.MULTI);
        userNameText.setBounds(150, 50, 300, 20);

        Label passwordLabel = new Label(shell, SWT.NONE);
        passwordLabel.setText(props.getProperty("user.password"));
        passwordLabel.setBounds(20, 100, 100, 20);
        passwordText = new Text(shell, SWT.PASSWORD);
        passwordText.setBounds(150, 100, 300, 20);

        Label comfirmLabel = new Label(shell, SWT.NONE);
        comfirmLabel.setText(props.getProperty("password.confirm"));
        comfirmLabel.setBounds(20, 150, 100, 20);
        comfiredPasswordText = new Text(shell, SWT.PASSWORD);
        comfiredPasswordText.setBounds(150, 150, 300, 20);

        Label mailLabel = new Label(shell, SWT.NONE);
        mailLabel.setText(props.getProperty("user.mail"));
        mailLabel.setBounds(20, 200, 100, 20);
        mailText = new Text(shell, SWT.NONE);
        mailText.setBounds(150, 200, 300, 20);

        Button createBtn = new Button(shell, SWT.PUSH);
        createBtn.setText(props.getProperty("menu.option.create_user"));
        createBtn.setBounds(130, 240, 200, 30);
        createBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onCreate();
            }
        });
        console = new Label(shell, SWT.BORDER);
        console.setBounds(50, 290, 400, 60);
    }

    private void onCreate() {
        User user = new User();
        user.password = passwordText.getText();
        user.id = userNameText.getText();
        user.mailAddress = mailText.getText();
        user.registerTime = SQLDate.getDate();
        if (user.password.length() == 0 || user.id.length() == 0) {
            console.setText("user name and password can not be null");
            return;
        } else if (!user.password.equals(comfiredPasswordText.getText())) {
            console.setText("confirmed you password please");
            return;
        }
        boolean flag;
        try {
            flag = connect.createUser(user);
        } catch (SQLException e) {
            console.setText("Error code : " + e.getErrorCode());
            e.printStackTrace();
            return;
        }
        if (flag) {
            console.setText("create user : " + user.id + " successfully !");
            shell.close();
        } else {
            console.setText("Something error when create new user.");
        }

    }

    public void start() {
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
