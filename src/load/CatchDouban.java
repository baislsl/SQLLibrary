package load;

import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

import util.Book;
import mysql.SQLConnection;

/**
 * Created by baislsl on 17-4-26.
 * <p>
 * <p>This class handler batch import of books from DouBan, the open API of DouBan can be
 * seen at {@link}[https://developers.douban.com/wiki/?title=guide] </p>
 */
public class CatchDouban {
    private final static int count = 10, start = 30;
    private final static String keyWord = "computer";
    private final static String[] attr = {
            "id", "isbn10", "isbn13", "title", "url", "author", "publisher", "pubdate",
            "translator", "summary", "images", "pages", "price"
    };
    private final static String base = "https://api.douban.com/v2/book/search?q=";

    public static void main(String[] args) {             //load from douban
        Book[] books = CatchDouban.load(count, start, keyWord);
        try {
            SQLConnection connect = RootUser.getRootConnection();
            for (Book book : books) {
                try {
                    connect.insertBook(book);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Book[] load(int count, int start, String keyword) {
        String str = base + keyword;
        ArrayList<Book> books = new ArrayList<>();
        while (count > 0) {
            try {
                URL url = new URL(buildUrl(str + "&start=" + Integer.toString(start) +
                        "&count=" + Integer.toString(count < 10 ? count : 10)));
                System.out.println(url.toString());
                InputStream in = url.openStream();
                Book[] bookList = FileLoader.JSONBookReader(in, true);
                for (Book book : bookList)
                    books.add(book);
            } catch (Exception e) {
                e.printStackTrace();
            }
            count -= 10;
            start += 10;
        }
        return books.toArray(new Book[0]);
    }

    /**
     * <p>This function generates a query website base</p>
     */
    private static String buildUrl(String base) {
        StringBuilder url = new StringBuilder(base);
        if (attr.length != 0)
            url.append("&fields=");
        boolean first = false;
        for (String str : attr) {
            if (!first) {
                first = true;
            } else {
                url.append(',');
            }
            url.append(str);
        }
        System.out.println(url);
        return url.toString();
    }
}
