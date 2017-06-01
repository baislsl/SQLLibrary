package gui;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import util.User;
import mysql.SQLConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by baislsl on 17-4-29.
 *
 * <p>This class generate a frame with a list of user information</p>
 */
public class ShowUserList {
    private Shell shell;
    private Display display;
    private SQLConnection connect;
    private Table table;
    private Properties props;
    private Button removeUserBtn;

    ShowUserList(Display display, SQLConnection connect, Properties props) {
        this.shell = new Shell(display);
        this.display = display;
        this.connect = connect;
        this.props = props;

        init();
    }

    private void init() {
        this.table = new Table(shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        String[] titles = {props.getProperty("user.id"), props.getProperty("user.password"),
                props.getProperty("user.mail"), props.getProperty("user.date")};
        for (int i = 0; i < titles.length; i++) {
            TableColumn column = new TableColumn(table, SWT.NONE);
            column.setText(titles[i]);
        }
        User[] users = null;
        try {
            users = connect.getUserList();
            for (User user : users) {
                TableItem item = new TableItem(table, SWT.PUSH);
                item.setText(0, user.id);
                item.setText(1, user.password);
                if (user.mailAddress != null)
                    item.setText(2, user.mailAddress);
                if (user.registerTime != null)
                    item.setText(3, user.registerTime);
            }
        } catch (SQLException e) {
            System.out.println("Error when connect to sql");
            e.printStackTrace();
        }
        for (int i = 0; i < titles.length; i++) {
            table.getColumn(i).pack();
        }
        // table.setSize(table.computeSize(SWT.DEFAULT, 200));
        Point point = table.computeSize(SWT.DEFAULT, 200);

        table.setBounds(0, 0, point.x, point.y);
        System.out.println("x=" + point.x + "\ny=" + point.y);

        removeUserBtn = new Button(shell, SWT.PUSH);
        removeUserBtn.setText(props.getProperty("user.drop"));
        removeUserBtn.setBounds(point.x + 10, 10, 100, 50);
        removeUserBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onDropUser();
            }
        });
    }

    private void onDropUser() {
        TableItem[] select = table.getSelection();
        if (select == null || select.length == 0) return;
        MessageBox dialog = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
        dialog.setText(props.getProperty("user.drop"));
        dialog.setMessage(props.getProperty("user.drop.tips"));
        if (dialog.open() == SWT.YES) {
            for (TableItem item : select) {
                String userId = item.getText(0);
                try {
                    connect.dropUser(userId);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            MessageBox result = new MessageBox(shell, SWT.APPLICATION_MODAL | SWT.YES);
            result.setMessage(props.getProperty("user.drop.success"));
            result.open();
            table.dispose();
            removeUserBtn.dispose();
            init();
        }
    }

    void start() {
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
