package gui;

import load.ImageLoader;
import mysql.SQLConnection;
import load.ImageCatchException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import util.Book;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by baislsl on 17-4-26.
 */
public class BookComposite extends Composite {
    private Book book;
    private SQLConnection connect;
    private Properties props;
    private ListPanel listParent;
    private final static int maxTextLength = 40;

    BookComposite(ListPanel parent, Book book, SQLConnection connect, Properties props,
                  boolean returnAble, boolean borrowAble, boolean renewAble) {
        super(parent, SWT.BORDER | SWT.FILL);
        this.listParent = parent;
        this.connect = connect;
        this.props = props;
        this.book = book;
        this.setLayout(new GridLayout(8, false));
        init(returnAble, borrowAble, renewAble);
    }

    private void init(boolean returnAble, boolean borrowAble, boolean renewAble) {
        this.setBackground(new Color(getParent().getDisplay(), 255, 255, 255));

        Composite composite = new Composite(this, SWT.NONE);
        composite.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.FILL));
        GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 4);
        gd_composite.widthHint = 80;
        composite.setLayoutData(gd_composite);

        Group group = new Group(composite, SWT.NONE);
        group.setText(props.getProperty("book.cover"));
        group.setLayout(new FillLayout(SWT.HORIZONTAL));

        Canvas canvas = new Canvas(group, SWT.NONE);
        if (book.img_url != null) {
            try {
                final Image image = ImageLoader.downLoad(book.img_url, getDisplay());
                canvas.addPaintListener(new PaintListener() {
                    public void paintControl(PaintEvent arg0) {
                        if (image != null) {
                            arg0.gc.drawImage(image, 0, 0);
                        }
                    }
                });
            } catch (ImageCatchException e) {
                e.printStackTrace();
            }
        }

        addLabel(props.getProperty("book.id"), book.id);
        addLabel(props.getProperty("book.title"), book.title);
        String authorName;
        if (book.author.length != 0) {
            StringBuilder author = new StringBuilder();
            for (String str : book.author)
                author.append("[" + str + "] ");
            authorName = author.toString();
        } else {
            authorName = null;
        }
        addLabel(props.getProperty("book.author"), authorName);

        Button borrowBtn = new Button(this, SWT.PUSH);
        borrowBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        borrowBtn.setText(props.getProperty("action.borrow"));
        borrowBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onBorrow();
            }
        });
        if (book.stock == 0) {
            borrowBtn.setEnabled(false);
        }else{
            borrowBtn.setEnabled(borrowAble);
        }

        Button renewBtn = new Button(this, SWT.PUSH);
        renewBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        renewBtn.setText(props.getProperty("action.renew"));
        renewBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onRenew();
            }
        });
        renewBtn.setEnabled(renewAble);
        if(!renewAble && returnAble)
            this.setBackground(getDisplay().getSystemColor(SWT.COLOR_RED));

        Button returnBtn = new Button(this, SWT.PUSH);
        returnBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        returnBtn.setText(props.getProperty("action.return"));
        returnBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onReturn();
            }
        });
        returnBtn.setEnabled(returnAble);

        Button detailBtn = new Button(this, SWT.PUSH);
        detailBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        detailBtn.setText(props.getProperty("action.detail"));
        detailBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onShowDetail(book);
            }
        });

        Button deleteBtn = new Button(this, SWT.PUSH);
        deleteBtn.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1,1));
        deleteBtn.setText(props.getProperty("action.delete"));
        deleteBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                onDeleteBook();
            }
        });
        deleteBtn.setEnabled(connect.isRoot());

    }


    private void onDeleteBook(){
        MessageBox dialog = new MessageBox(getShell(), SWT.APPLICATION_MODAL | SWT.YES | SWT.NO);
        dialog.setText(props.getProperty("book.drop"));
        dialog.setMessage(props.getProperty("book.drop.tips"));
        if(dialog.open() != SWT.YES) return;
        try{
            connect.dropBook(book.id);
            MessageBox dialog2 = new MessageBox(getShell(), SWT.APPLICATION_MODAL | SWT.YES);
            dialog2.setText(props.getProperty("book.drop"));
            dialog2.setMessage(props.getProperty("book.drop.success"));
            dialog2.open();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    private void addLabel(String name, String text) {
        if (text == null) text = "--";
        if (text.length() > maxTextLength)
            text = text.substring(0, maxTextLength) + "...";
        Label nameLabel = new Label(this, SWT.NONE);
        nameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        nameLabel.setText(name);
        Label textLabel = new Label(this, SWT.NONE);
        textLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 6, 1));
        textLabel.setText(text);
    }

    private void onShowDetail(Book book) {
        String date;
        try{
            date = connect.getBorrowDate(book.id);
        }catch (SQLException e){
            e.printStackTrace();
            date = "";
        }
        new BookDetailList(book, date, props);
    }

    private void onReturn() {
        try {
            connect.returnBook(book);
            MessageBox dialog = new MessageBox(getShell(),
                    SWT.APPLICATION_MODAL | SWT.YES);
            dialog.setText(props.getProperty("return.success"));
            dialog.setMessage(props.getProperty("return.success"));
            dialog.open();
            removeThis();
            listParent.updateInfo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void onBorrow() {
        if (book.stock > 0)
            try {
                connect.borrowBook(book);
                MessageBox dialog = new MessageBox(getShell(),
                        SWT.APPLICATION_MODAL | SWT.YES);
                dialog.setText(props.getProperty("borrow.success"));
                dialog.setMessage(props.getProperty("borrow.success"));
                dialog.open();
                listParent.updateInfo();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    private void onRenew(){
        try {
            connect.renewBook(book.id);
            MessageBox dialog = new MessageBox(getShell(),
                    SWT.APPLICATION_MODAL | SWT.YES);
            dialog.setText(props.getProperty("renew.success"));
            dialog.setMessage(props.getProperty("renew.success"));
            dialog.open();
            listParent.updateInfo();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeThis() {
        this.dispose();
    }

}
