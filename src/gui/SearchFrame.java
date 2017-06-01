package gui;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import util.Book;
import mysql.SQLConnection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by baislsl on 17-4-28.
 */
public class SearchFrame extends Composite {

    private SQLConnection connect;
    private Properties props;
    private Combo[] combos;
    private Text[] boxs;
    private Button andBtn, orBtn, stockBtn;
    private final static int count = 3;
    private ListPanel listPanel;

    SearchFrame(Composite parent, int var, SQLConnection connect,
                Properties props, HashMap<String, String> searchData) {
        super(parent, var);
        this.connect = connect;
        this.props = props;
        this.combos = new Combo[count];
        this.boxs = new Text[count];
        this.setLayout(new GridLayout(4, true));
        init(searchData);
    }

    private void init(HashMap<String, String> searchData) {
        String[] items = {
                props.getProperty("book.title"),
                props.getProperty("book.id"),
                props.getProperty("book.isbn10"),
                props.getProperty("book.isbn13"),
                props.getProperty("book.author"),
                props.getProperty("book.stock")
        };
        for (int i = 0; i < count; i++) {
            combos[i] = new Combo(this, SWT.READ_ONLY);
            combos[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
            combos[i].setItems(items);
            boxs[i] = new Text(this, SWT.MULTI | SWT.BORDER);
            boxs[i].setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
            combos[i].setText(items[i]);
        }
        if (searchData != null) {
            int index = 0;
            for (Map.Entry<String, String> entry : searchData.entrySet()) {
                combos[index].setText(entry.getKey());
                boxs[index].setText(entry.getValue());
                index++;
            }
        }
        Composite relation = new Composite(this, SWT.BORDER);
        relation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        relation.setLayout(new FillLayout());
        andBtn = new Button(relation, SWT.RADIO);
        andBtn.setText(props.getProperty("search.and"));
        andBtn.setSelection(true);
        orBtn = new Button(relation, SWT.RADIO);
        orBtn.setText(props.getProperty("search.or"));

        Composite stockPositive = new Composite(this, SWT.BORDER);
        stockPositive.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1,1));
        stockPositive.setLayout(new FillLayout());
        stockBtn = new Button(stockPositive, SWT.CHECK);
        stockBtn.setText(props.getProperty("search.stock.positive"));
        stockBtn.setSelection(false);

        Button searchBtn = new Button(this, SWT.PUSH);
        searchBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
        searchBtn.setText(props.getProperty("search.button"));
        searchBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);
                try {
                    onSearch();
                } catch (SQLException e) {
                    e.printStackTrace();
                }finally {
                    MessageBox dialog = new MessageBox(getShell(),
                            SWT.APPLICATION_MODAL | SWT.YES);
                    dialog.setMessage(props.getProperty("search.finish"));
                    dialog.open();
                }
            }
        });
    }

    public void onSearch() throws SQLException {
        Book[] books = getSearchResult();
        boolean stockPositive = stockBtn.getSelection();
        listPanel.clearBookLists();
        if (books == null || books.length == 0) return;
        for (Book book : books) {
            if(!stockPositive || book.stock > 0){
                System.out.println(book.title);
                listPanel.addBook(book);
            }
        }
        getParent().layout();
    }

    private Book[] getSearchResult() throws SQLException {
        HashMap<String, String> map = getSearchData();
        System.out.println("size = " + map.size());
        if (map.size() == 1) {
            return oneSelectSearch();
        } else if (map.size() != 0) {
            return connect.selectMultiBook(map, andBtn.getSelection());
        } else {
            return null;
        }
    }

    private String queryMap(String str) {
        if (str.equals(props.getProperty("book.title"))) {
            return "title";
        } else if (str.equals(props.getProperty("book.id"))) {
            return "book_id";
        } else if (str.equals(props.getProperty("book.isbn10"))) {
            return "isbn10";
        } else if (str.equals(props.getProperty("book.isbn13"))) {
            return "isbn13";
        } else if (str.equals(props.getProperty("book.author"))) {
            return "author";
        } else if (str.equals(props.getProperty("book.stock"))) {
            return "stock";
        } else {
            return null;
        }
    }

    private Book[] oneSelectSearch() throws SQLException {
        int i;
        String keyWord = null;
        for (i = 0; i < 3; i++) {
            keyWord = boxs[i].getText();
            if (keyWord != null && keyWord.length() != 0)
                break;
        }
        switch (combos[i].getSelectionIndex()) {
            case 0:     // title
                return connect.selectTitleBook(keyWord);
            case 1:     // book id
                Book[] books = new Book[1];
                books[0] = connect.getBook(keyWord);
                return books;
            case 2:     // isbn10
                return connect.selectIsbn10Book(keyWord);
            case 3:    // isbn13
                return connect.selectIsbn13Book(keyWord);
            case 4:     // author
                return connect.selectAuthorBook(keyWord);
            case 5:     // stock
                return connect.selectStockBook(Integer.parseInt(keyWord));
            default:
                throw new SQLException("no such search function");
        }
    }

    public void setListPanel(ListPanel listPanel) {
        this.listPanel = listPanel;
    }

    public HashMap<String, String> getSearchData() {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            String keyWord = boxs[i].getText();
            if (keyWord != null && keyWord.length() != 0) {
                String key = queryMap(combos[i].getText());
                map.put(key, keyWord);
            }
        }
        return map;
    }
}
