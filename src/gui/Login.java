package gui;

/**
 * Created by baislsl on 17-4-26.
 */

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import util.Language;
import mysql.SQLConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

public class Login {
    private final static int WIDTH = 300;
    private final static int HEIGHT = 400;
    private final static int TextHeight = 20;
    private final static int TextLength = 110;
    private final static String languagePropsPath = "res/property/database.properties";
    private final static String IconPath = "res/pictures/Icon/icon.png";
    private final Image icon;
    private Language language;
    private String propsPath;
    private Display display;
    private Shell shell;
    private Text nameText, passwordText, infoText;
    private String userName, password;
    private SQLConnection connection;
    private Properties props;
    private Listener enterListener;

    public Login() {
        this.display = new Display();
        this.props = new Properties();
        this.shell = new Shell(display);
        this.enterListener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (e.keyCode == SWT.Selection)
                    onLogin();
            }
        };
        icon = new Image(display, IconPath);
        nameText = new Text(shell, SWT.NONE);
        passwordText = new Text(shell, SWT.PASSWORD);
        infoText = new Text(shell, SWT.NONE);
        init();
    }

    private void init() {
        try (InputStream in = Files.newInputStream(Paths.get(languagePropsPath))) {
            Properties langProps = new Properties();
            langProps.load(in);
            String name = langProps.getProperty("library.language");
            try {
                language = Enum.valueOf(Language.class, name.toUpperCase());
                propsPath = language.path;
            } catch (EnumConstantNotPresentException e) {
                throw new IOException("Some thing wrong with you property file.");

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (propsPath == null) {  //if load fail, set the default language as English
                language = Language.ENGLISH;
                propsPath = language.path;
            }
        }

        try (InputStream in = Files.newInputStream(Paths.get(propsPath))) {
            props.load(new InputStreamReader(in, "utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        shell.setBounds(100, 100, WIDTH, HEIGHT);
        shell.setText(props.getProperty("login"));
        shell.setImage(icon);
        Button loginButton = new Button(shell, SWT.NONE);
        loginButton.setText(props.getProperty("login"));
        loginButton.setBounds(115, 235, 70, 25);

        nameText.setBounds(120, 100, TextLength, TextHeight);
        passwordText.setBounds(120, 160, TextLength, TextHeight);
        infoText.setBounds(65, 270, TextLength + 60, TextHeight);

        Label nameLabel = new Label(shell, SWT.NONE);
        nameLabel.setText(props.getProperty("user.id") + "：");
        nameLabel.setBounds(20, 100, 85, TextHeight);

        Label passLabel = new Label(shell, SWT.NONE);
        passLabel.setText(props.getProperty("user.password") + "：");
        passLabel.setBounds(20, 165, 85, TextHeight);

        loginButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onLogin();
            }
        });
        display.addFilter(SWT.KeyDown, enterListener);
    }

    private void onLogin() {
        try {
            infoText.setText("Connect to database...");
            connection = new SQLConnection(nameText.getText(), passwordText.getText());
            infoText.setText("Login successfully");
            userName = nameText.getText();
            password = passwordText.getText();
            display.removeFilter(SWT.KeyDown, enterListener);
            shell.close();
        } catch (SQLException sqlException) {
            infoText.setText("Login fail");
            sqlException.printStackTrace();
        }
    }

    public void start() {
        start("", "");
    }

    public void start(String userName, String password) {
        nameText.setText(userName);
        passwordText.setText(password);
        shell.open();
        if (userName.length() != 0) {
            onLogin();
        }
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        System.out.println("Login frame close!");
        generateMainFrame();
    }

    private void generateMainFrame() {
        shell = new Shell(display);
        shell.setLayout(new FillLayout());
        new MainInterface(userName, password, connection, shell, props, language);
        shell.setText(props.getProperty("title") + userName);
        shell.setImage(icon);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }


    public static String getLanguagePropsPath() {
        return languagePropsPath;
    }

    public SQLConnection getConnection() {
        return connection;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public Properties getProps() {
        return this.props;
    }
}


