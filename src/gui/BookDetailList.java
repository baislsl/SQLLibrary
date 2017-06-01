package gui;

import util.Book;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.util.Properties;

/**
 * Created by baislsl on 17-5-7.
 */
public class BookDetailList {
    private Book book;
    private Shell shell;
    private Properties props;
    private String date;

    BookDetailList(Book book, String date, Properties props) {
        shell = new Shell();
        this.book = book;
        this.date = date;
        this.props = props;
        init();
    }

    private void init() {
        shell.setText(book.title);
        Table table = new Table(shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        new TableColumn(table, SWT.NONE);
        new TableColumn(table, SWT.NONE);

        String[][] attributes = {
                {props.getProperty("borrow.date"),date},
                {props.getProperty("book.id"), book.id},
                {props.getProperty("book.title"), book.title},
                {props.getProperty("book.author"), showStringArray(book.author)},
                {props.getProperty("book.translator"), showStringArray(book.translator)},
                {props.getProperty("book.publisher"), book.publisher},
                {props.getProperty("book.pubdate"), book.pubdate},
                {props.getProperty("book.pages"), Integer.toString(book.pages)},
                {props.getProperty("book.stock"), Integer.toString(book.stock)},
                {props.getProperty("book.price"), book.price},
                {props.getProperty("book.isbn10"), book.isbn10},
                {props.getProperty("book.isbn13"), book.isbn13},
                {props.getProperty("book.url"), book.url}
        };

        for (String[] attr : attributes) {
            System.out.println("0 : " + attr[0] + "\n1:" + attr[1]);
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(0, attr[0]);
            if (attr[1] == null)
                item.setText(1, "\\");
            else
                item.setText(1, attr[1]);
        }
        table.getColumn(0).pack();
        table.getColumn(1).pack();
        table.setSize(table.computeSize(SWT.DEFAULT, 600));
        shell.open();
    }

    private String showStringArray(String[] array) {
        StringBuilder ans = new StringBuilder();
        boolean isFirst = true;
        for (String str : array) {
            if (isFirst) {
                isFirst = false;
            } else {
                ans.append(',');
            }
            ans.append(str);
        }
        return ans.toString();
    }
}