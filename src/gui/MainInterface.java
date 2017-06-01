package gui;

import load.FileLoader;
import mysql.SQLConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.omg.CORBA.COMM_FAILURE;
import util.Book;
import util.History;
import util.Language;

import javax.crypto.spec.DESedeKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by baislsl on 17-5-11.
 */
public class MainInterface extends Composite {
    private SQLConnection connect;
    private final String userName, password;
    private Properties props;
    private Language language;
    private ListPanel listPanel;

    public MainInterface(String userName, String password, SQLConnection connect,
                         Composite parent, Properties props, Language language) {
        super(parent, SWT.FILL | SWT.BORDER);
        this.userName = userName;
        this.password = password;
        this.connect = connect;
        this.props = props;
        this.language = language;
        init();
    }

    private void init() {
        this.setLayout(new GridLayout(5, true));
        ControlPanel controlPanel = new ControlPanel(this, props);
        controlPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        Composite listPanrlTmp = new Composite(this, SWT.BORDER);
        listPanrlTmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
        listPanrlTmp.setLayout(new FillLayout());
        listPanel = new ListPanel(listPanrlTmp, props, connect);
        listPanrlTmp.setSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
        initMenu();
        controlPanel.setListPanel(listPanel);
        listPanel.turnBookFrame();
    }

    private void initMenu() {
        Menu menu = new Menu(getShell(), SWT.BAR);
        getShell().setMenuBar(menu);

        MenuItem file = new MenuItem(menu, SWT.CASCADE);
        file.setText(props.getProperty("menu.file"));
        Menu fileList = new Menu(getShell(), SWT.DROP_DOWN);
        file.setMenu(fileList);
        MenuItem loadBook = new MenuItem(fileList, SWT.PUSH);
        loadBook.setText(props.getProperty("menu.file.load_book"));
        loadBook.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onLoadBooks();
            }
        });
        MenuItem loadUser = new MenuItem(fileList, SWT.PUSH);
        loadUser.setText(props.getProperty("menu.file.load_user"));
        loadUser.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onLoadUsers();
            }
        });
        MenuItem exit = new MenuItem(fileList, SWT.PUSH);
        exit.setText(props.getProperty("menu.file.exit"));//System.exit(0)
        exit.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onExit();
            }
        });

        MenuItem stimulator = new MenuItem(menu, SWT.CASCADE);
        stimulator.setText(props.getProperty("menu.option"));
        Menu stimulatorList = new Menu(getShell(), SWT.DROP_DOWN);
        stimulator.setMenu(stimulatorList);
        MenuItem createUser = new MenuItem(stimulatorList, SWT.PUSH);
        createUser.setText(props.getProperty("menu.option.create_user"));
        createUser.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onCreateUser();
            }
        });
        MenuItem showUser = new MenuItem(stimulatorList, SWT.PUSH);
        showUser.setText(props.getProperty("menu.option.list"));
        showUser.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onShowUser();
            }
        });
        MenuItem language = new MenuItem(stimulatorList, SWT.CASCADE);
        language.setText(props.getProperty("menu.language"));
        Menu langMenu = new Menu(getShell(), SWT.DROP_DOWN);
        language.setMenu(langMenu);

        Language[] languageSet = Language.values();
        for (Language lang : languageSet) {
            MenuItem langItem = new MenuItem(langMenu, SWT.PUSH);
            langItem.setText(props.getProperty("menu.language." + lang.name().toLowerCase()));
            langItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent selectionEvent) {
                    super.widgetSelected(selectionEvent);
                    toLanguage(lang);
                }
            });
        }

        MenuItem others = new MenuItem(menu, SWT.CASCADE);
        others.setText(props.getProperty("menu.other"));
        Menu otherMenu = new Menu(getShell(), SWT.DROP_DOWN);
        others.setMenu(otherMenu);
        MenuItem update = new MenuItem(otherMenu, SWT.PUSH);
        update.setText(props.getProperty("menu.other.update"));
        update.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onUpdate();
            }
        });
        MenuItem logout = new MenuItem(otherMenu, SWT.PUSH);
        logout.setText(props.getProperty("menu.other.logout"));
        logout.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onLogout();
            }
        });

        if (!this.userName.equals(props.getProperty("root.name"))) {
            createUser.setEnabled(false);
            showUser.setEnabled(false);
            loadBook.setEnabled(false);
            loadUser.setEnabled(false);
        }
    }

    private void onUpdate() {
        try {
            listPanel.updateInfo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void toLanguage(Language language) {
        if (this.language != language) {
            MessageBox dialog = new MessageBox(getShell(),
                    SWT.APPLICATION_MODAL | SWT.YES | SWT.NO | SWT.CANCEL);
            dialog.setText(props.getProperty("language.change"));
            dialog.setMessage(props.getProperty("language.tip"));
            switch (dialog.open()) {
                case SWT.CANCEL:
                    return;
                case SWT.NO:
                    setLanguage(language);
                    break;
                case SWT.YES:
                    setLanguage(language);
                    getDisplay().dispose();
                    this.dispose();
                    new Login().start(userName, password);
            }
        }
    }

    private boolean setLanguage(Language language) {
        Properties langProps = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(Login.getLanguagePropsPath()))) {
            langProps.load(in);
            langProps.setProperty("library.language", language.name().toLowerCase());
            in.close();
            FileOutputStream fos = new FileOutputStream(new File(Login.getLanguagePropsPath()));
            langProps.store(fos, "The Java program generate");
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void onLoadBooks() {
        String path = onOpen(props.getProperty("load.book"));
        try {
            if (path == null || path.length() == 0) return;
            FileLoader.BookLoader(path, connect, false);
            MessageBox result = new MessageBox(getShell(), SWT.YES);
            result.setMessage(props.getProperty("load.success"));
            result.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onLoadUsers() {
        String path = onOpen(props.getProperty("load.user"));
        try {
            if (path == null || path.length() == 0) return;
            FileLoader.UserLoader(path, connect);
            MessageBox result = new MessageBox(getShell(), SWT.YES);
            result.setMessage(props.getProperty("load.success"));
            result.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onCreateUser() {
        CreateNewUserShell uShell = new CreateNewUserShell(getDisplay(), connect, props);
        uShell.start();
    }

    private void onShowUser() {
        ShowUserList uShell = new ShowUserList(getDisplay(), connect, props);
        uShell.start();
    }

    private String onOpen(String title) {
        FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
        fileDialog.setText(title);
        fileDialog.setFilterExtensions(new String[]{
                "*.json",
                "*.*"
        });
        String filePath = fileDialog.open();
        System.out.println("file path is " + filePath);
        return filePath;
    }

    private void onExit() {
        System.exit(0);
    }

    private void onLogout() {
        getDisplay().dispose();
        this.dispose();
        new Login().start();
    }
}
