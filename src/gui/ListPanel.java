package gui;

import mysql.SQLConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import util.Book;
import util.History;
import util.SQLDate;
import util.ViewFrame;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by baislsl on 17-5-11.
 */
public class ListPanel extends ScrolledComposite {

    private ArrayList<Composite> lists = new ArrayList<>();
    private Composite listFrame;
    private SQLConnection connect;
    private Properties props;
    private ViewFrame view = ViewFrame.BOOKLIST;
    private HashMap<String, String> searchData;
    private SearchFrame searchFrame;
    private Book[] bookList;

    public ListPanel(Composite parent, Properties props, SQLConnection connection) {
        super(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.FILL);
        listFrame = new Composite(this, SWT.NONE);
        this.connect = connection;
        this.props = props;
        this.setExpandHorizontal(true);
        this.setExpandVertical(true);
        this.setContent(listFrame);
        // this.setMinSize(600,500);
        this.setMinSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
        init();
    }

    private void init() {
        GridLayout layout = new GridLayout(1, false);
        listFrame.setLayout(layout);
        // listFrame.setSize(listFrame.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    public void turnSearchFrame() {
        clear();
        view = ViewFrame.SEARCH;
        searchFrame = new SearchFrame(listFrame, SWT.NONE, connect, props, searchData);
        searchFrame.setListPanel(this);
        addItem(searchFrame);
    }

    public void turnBookFrame() {
        clear();
        view = ViewFrame.BOOKLIST;
        updateBookInfo();
    }

    public void turnHistory() {
        clear();
        view = ViewFrame.HISTORY;
        try {
            History[] historyList = connect.selectHistory();
            for (History hist : historyList) {
                this.addHistory(hist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateInfo() throws SQLException {
        switch (view) {
            case BOOKLIST:
                turnBookFrame();
                break;
            case HISTORY:
                turnHistory();
                break;
            case SEARCH:
                turnSearchFrame();
                searchFrame.onSearch();
        }
    }

    private void updateBookInfo() {
        try {
            HashMap<Book, String> bookMap = connect.selectBook();
            bookList = new Book[bookMap.size()];
            int index = 0;
            for(Map.Entry<Book, String> item : bookMap.entrySet()){
                Book book = item.getKey();
                bookList[index++] = book;
                System.out.println(book.title);
                this.addBook(book, item.getValue());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        this.layout();

    }

    public void clearBookLists() {
        int size = lists.size();
        for (int i = 1; i < size; i++) {
            lists.get(i).dispose();
        }
        while (lists.size() != 1) lists.remove(1);
    }

    private void clear() {
        if (view == ViewFrame.SEARCH)
            searchData = searchFrame.getSearchData();
        for (Composite composite : lists) {
            composite.dispose();
        }
        lists.clear();
        this.layout();
    }

    private void addItem(Composite item) {
        lists.add(item);
        item.setParent(listFrame);
        //item.setSize(item.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        item.layout();
        this.setMinSize(computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private void addBook(Book book, String date){
        int differDay = (int)SQLDate.getDifferDay(date);
        boolean timeOut = false;
        if(differDay > Integer.parseInt(props.getProperty("borrow.max.day"))){
            timeOut = true;
        }
        BookComposite bookCom = new BookComposite(this, book, connect, props,
                true, false, !timeOut);
        addItem(bookCom);
        this.layout();
    }

    public void addBook(Book book) {
        boolean returnAble = false, borrowAble = true;
        for(Book item : bookList){
            if(item.id.equals(book.id)){
                returnAble = true;
                borrowAble = false;
                break;
            }
        }
        BookComposite bookCom = new BookComposite(this, book, connect, props,
                returnAble, borrowAble, false);
        addItem(bookCom);
        this.layout();
    }

    private void addHistory(History history) {
        HistoryComposite com = new HistoryComposite(listFrame, history, props);
        addItem(com);
        this.layout();
    }
}
