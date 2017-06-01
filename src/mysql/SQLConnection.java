package mysql;

import util.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 * Created by baislsl on 17-4-28.
 *
 * <p>The center class to perform the connection between local MySQ and the program </p>
 */
public class SQLConnection {
    private final Connection connect;
    private final String userName, password;
    private final Statement stat;
    private final static String dataBasePropsPath = "res/property/database.properties";
    private boolean isRoot;

    public SQLConnection(String userName, String password) throws SQLException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get(dataBasePropsPath))) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null)
            System.setProperty("jdbc.drivers", drivers);
        String url = props.getProperty("jdbc.url");
        this.userName = userName;
        this.password = password;
        isRoot = userName.equals(props.getProperty("jdbc.username"));
        connect = DriverManager.getConnection(url, userName, password);
        stat = connect.createStatement();
        init();
    }

    private void init() throws SQLException {
        userBookStat        = connect.prepareStatement(userBookQuery);
        bookWirteStat       = connect.prepareStatement(bookWriteQuery);
        bookTranslate       = connect.prepareStatement(bookTranslateQuery);
        bookStat            = connect.prepareStatement(bookIdQuery);
        historyStat         = connect.prepareStatement(historyQuery);
        insertHistoryStat   = connect.prepareStatement(insertHistoryAction);
        bookTitleStat       = connect.prepareStatement(bookTitleQuery);
        bookIsbn10Stat      = connect.prepareStatement(bookIsbn10Query);
        bookIsbn13Stat      = connect.prepareStatement(bookIsbn13Query);
        bookAuthorStat      = connect.prepareStatement(bookAuthorQuery);
        bookStockStat       = connect.prepareStatement(bookStockQuery);
        createUserStat      = connect.prepareStatement(createUserAction);
        insertUserStat      = connect.prepareStatement(insertUserAction);
        delUserStat         = connect.prepareStatement(delUserAction);
        dropUserStat        = connect.prepareStatement(dropUserAction);
        insertBookStat      = connect.prepareStatement(insertBook);
        insertWorkStat      = connect.prepareStatement(insertWork);
        insertTranslateStat = connect.prepareStatement(insertTranslate);
        deleteBookStat      = connect.prepareStatement(deleteBook);
        selectBorrowDateStat= connect.prepareStatement(selectBorrowDate);
        renewBookStat       = connect.prepareStatement(renewBook);
        // no grantStat, which contain more than one sql statements
    }

    public void close() throws  SQLException{
        connect.close();
    }


    // Run the sql command and show the result if has. This function is mainly for debug used
    private boolean runSQLCommand(String cmd) throws SQLException {
        // System.out.println(cmd);
        boolean result = stat.execute(cmd);
        if (result) {
            ResultSet res = stat.getResultSet();
            showResultSet(res);
        }
        return result;
    }

    public static void showResultSet(ResultSet res) throws SQLException {
        ResultSetMetaData metaRes = res.getMetaData();
        int length = metaRes.getColumnCount();
        for (int i = 1; i <= length; i++) {
            System.out.print(metaRes.getColumnLabel(i) + "  ");
        }
        System.out.println();
        while (res.next()) {
            for (int i = 1; i <= length; i++) {
                System.out.print(res.getString(i) + " ");
            }
            System.out.println();
        }
        res.beforeFirst();
    }


    // user_id id was create by the system, this function return the user_id id of the new user_id
    // only available when you have login in as the root user_id of has such kind of privilege
    public boolean createUser(User user) throws SQLException {
        String userName = user.id,
                password = user.password,
                mail_address = user.mailAddress,
                date = user.registerTime;
        // inset into the user list
        insertUserStat.setString(1, userName);
        insertUserStat.setString(2, password);
        insertUserStat.setString(3, date);
        if (mail_address.length() == 0)
            insertUserStat.setNull(4, Types.VARCHAR);
        else
            insertUserStat.setString(4, mail_address);
        insertUserStat.execute();

        //create user
        createUserStat.setString(1, userName);
        createUserStat.setString(2, password);
        System.out.println(userName + " -- " + password);
        try {
            createUserStat.execute();
        } catch (SQLException e) {  //roll back
            delUserStat.setString(1, userName);
            delUserStat.execute();
            throw e;
        }

        //grant privilege for the new user
        try {
            for (String str : grantAction) {
                grantStat = connect.prepareStatement(str);
                grantStat.setString(1, userName);
                grantStat.execute();
            }
        } catch (SQLException e) {  // roll back
            delUserStat.setString(1, userName);
            delUserStat.execute();
            dropUserStat.setString(1, userName);
            dropUserStat.execute();
            throw e;
        }
        return true;
    }


    public boolean isRoot(){
        return this.isRoot;
    }

    private static String judgeNull(String str) {
        if (str == null || str.length() == 0)
            return null;
        else return str;
    }

    public User[] getUserList() throws SQLException {
        ResultSet resultSet = stat.executeQuery(selectUserAction);
        ArrayList<User> userLists = new ArrayList<>();
        while (resultSet.next()) {
            User user = new User();
            user.id = resultSet.getString(1);
            user.password = resultSet.getString(2);
            user.registerTime = resultSet.getString(3);
            user.mailAddress = resultSet.getString(4);
            userLists.add(user);
        }
        return userLists.toArray(new User[0]);
    }

    public Book[] selectIsbn13Book(String isbn13) throws SQLException {
        bookIsbn13Stat.setString(1, isbn13);
        ResultSet resultSet = bookIsbn13Stat.executeQuery();
        ArrayList<Book> bookList = new ArrayList<>();
        while (resultSet.next()) {
            bookList.add(getBook(resultSet.getString(1)));
            resultSet.next();
        }
        return bookList.toArray(new Book[0]);
    }

    public Book[] selectIsbn10Book(String isbn10) throws SQLException {
        bookIsbn10Stat.setString(1, isbn10);
        ResultSet resultSet = bookIsbn10Stat.executeQuery();
        ArrayList<Book> bookList = new ArrayList<>();
        while (resultSet.next()) {
            bookList.add(getBook(resultSet.getString(1)));
            System.out.print(resultSet.getString(1));
            resultSet.next();
        }
        return bookList.toArray(new Book[0]);
    }

    public Book[] selectTitleBook(String title) throws SQLException {
        bookTitleStat.setString(1, ".*" + title.trim() + ".*");
        System.out.println(bookTitleStat.toString());
        ResultSet resultSet = bookTitleStat.executeQuery();
        ArrayList<Book> bookList = new ArrayList<>();
        while (resultSet.next()) {
            bookList.add(getBook(resultSet.getString(1)));
            resultSet.next();
        }
        return bookList.toArray(new Book[0]);
    }

    public Book[] selectAuthorBook(String author) throws SQLException{
        bookAuthorStat.setString(1, ".*" + author.trim() + ".*");
        ResultSet resultSet = bookAuthorStat.executeQuery();
        ArrayList<Book> bookList = new ArrayList<>();
        while(resultSet.next()){
            bookList.add(getBook(resultSet.getString(1)));
            resultSet.next();
        }
        return bookList.toArray(new Book[0]);
    }

    public Book[] selectStockBook(int minStock) throws SQLException{
        bookStockStat.setInt(1, minStock);
        ResultSet resultSet = bookStockStat.executeQuery();
        ArrayList<Book> bookList = new ArrayList<>();
        while(resultSet.next()){
            bookList.add(getBook(resultSet.getString(1)));
            resultSet.next();
        }
        return bookList.toArray(new Book[0]);
    }

    public void insertBook(Book book) throws SQLException {
        int index = 1;
        insertBookStat.setString(index++, judgeNull(book.id));
        insertBookStat.setString(index++, judgeNull(book.isbn10));
        insertBookStat.setString(index++, judgeNull(book.isbn13));
        insertBookStat.setString(index++, judgeNull(book.title));
        insertBookStat.setString(index++, judgeNull(book.url));
        insertBookStat.setString(index++, judgeNull(book.img_url));
        insertBookStat.setString(index++, judgeNull(book.price));
        insertBookStat.setString(index++, judgeNull(book.publisher));
        insertBookStat.setInt(index++, book.pages);
        insertBookStat.setInt(index++, book.stock);
        insertBookStat.setString(index++, SQLDate.toDate(book.pubdate));
        insertBookStat.execute();

        for (String author : book.author) {
            insertWorkStat.setString(1, author);
            insertWorkStat.setString(2, book.id);
            System.out.println(insertWorkStat);
            insertWorkStat.execute();
        }
        for (String trans : book.translator) {
            insertTranslateStat.setString(1, trans);
            insertTranslateStat.setString(2, book.id);
            insertTranslateStat.execute();
        }
    }

    public History[] selectHistory() throws SQLException {
        return selectHistory(this.userName);
    }

    public History[] selectHistory(String user_id) throws SQLException {
        historyStat.setString(1, user_id);
        System.out.println(historyStat);
        ResultSet resultSet = historyStat.executeQuery();

        ArrayList<History> historyList = new ArrayList<>();
        while (resultSet.next()) {
            String book_id = resultSet.getString(1);
            String action = resultSet.getString(2);

            History history = new History();
            history.user_id = user_id;
            history.action = Enum.valueOf(Action.class, action.toUpperCase());
            history.date = resultSet.getString(3);
            history.book = getBook(book_id);
            historyList.add(history);
        }
        return historyList.toArray(new History[0]);
    }

    public Book getBook(String book_id) throws SQLException {
        bookWirteStat.setString(1, book_id);
        bookTranslate.setString(1, book_id);
        bookStat.setString(1, book_id);
        ResultSet publisher = bookWirteStat.executeQuery();
        ResultSet translators = bookTranslate.executeQuery();
        ResultSet book = bookStat.executeQuery();

        if(!book.next())
            throw new SQLException("No such book with id : " + book_id);
        Book ans = new Book();
        int index = 1;
        ans.id = book.getString(index++);
        ans.isbn10 = book.getString(index++);
        ans.isbn13 = book.getString(index++);
        ans.title = book.getString(index++);
        ans.url = book.getString(index++);
        ans.img_url = book.getString(index++);
        ans.price = book.getString(index++);
        ans.publisher = book.getString(index++);
        ans.pages = book.getInt(index++);
        ans.stock = book.getInt(index++);
        ans.pubdate = book.getString(index++);

        ArrayList<String> authorList = new ArrayList<>();
        while (publisher.next()) {
            authorList.add(publisher.getString(1));
        }
        ans.author = authorList.toArray(new String[0]);

        ArrayList<String> tranList = new ArrayList<>();
        while (translators.next()) {
            tranList.add(translators.getString(1));
        }
        ans.translator = tranList.toArray(new String[0]);

        return ans;
    }

    // select the book which user has borrowed and the borrow date
    public HashMap<Book, String> selectBook() throws SQLException {
        return selectBook(this.userName);
    }

    public HashMap<Book, String> selectBook(String user_id) throws SQLException {
        userBookStat.setString(1, user_id);
        ResultSet resultSet = userBookStat.executeQuery();

        HashMap<Book, String> bookList = new HashMap<>();
        while (resultSet.next()) {
            Book book = getBook(resultSet.getString(1));
            String date = resultSet.getString(2);
            bookList.put(book, date);
        }
        return bookList;
    }

    public void borrowBook(Book book) throws SQLException {
        insertHistoryStat.setString(1, userName);
        insertHistoryStat.setString(2, book.id);
        insertHistoryStat.setString(3, "borrow");
        insertHistoryStat.execute();
    }

    public void returnBook(Book book) throws SQLException {
        insertHistoryStat.setString(1, userName);
        insertHistoryStat.setString(2, book.id);
        insertHistoryStat.setString(3, "return");
        insertHistoryStat.execute();
    }

    public Book[] selectMultiBook(HashMap<String, String> querySet, boolean isAnd) throws SQLException{
        StringBuilder query = new StringBuilder(bookMultiQuery);
        boolean isFirst = true;
        String logic = isAnd ? " AND " : " OR ";
        for(Map.Entry<String, String> entry : querySet.entrySet()){
            if(isFirst){
                isFirst = false;
            }else{
                query.append(logic);
            }
            if(entry.getKey().equals("title") || entry.getKey().equals("author")){
                query.append(entry.getKey());
                query.append(" REGEXP ");
                query.append("\".*" + entry.getValue() + ".*\"");
            }else{
                query.append(entry.getKey());
                if(entry.getKey().equals("stock"))
                    query.append(">=");
                else
                    query.append("=");
                query.append("\"" + entry.getValue() + "\"");
            }
        }
        System.out.println(query);
        ResultSet resultSet = stat.executeQuery(query.toString());
        ArrayList<Book> bookList = new ArrayList<>();
        while (resultSet.next()) {
            Book book = getBook(resultSet.getString(1));
            bookList.add(book);
        }
        return bookList.toArray(new Book[0]);
    }

    public void dropUser(String userId) throws SQLException{
        delUserStat.setString(1, userId);
        delUserStat.execute();  // delete user from the library.user table
        dropUserStat.setString(1, userId);
        dropUserStat.execute(); // drop user from the database system
    }

    public void dropBook(String bookId) throws SQLException{
        deleteBookStat.setString(1, bookId);
        deleteBookStat.execute();
    }

    public String getBorrowDate(String bookId) throws SQLException{
        selectBorrowDateStat.setString(1, userName);
        selectBorrowDateStat.setString(2, bookId);
        ResultSet resultSet = selectBorrowDateStat.executeQuery();
        String date = "";
        if(resultSet.next()){
            date = resultSet.getString(1);
            System.out.print("date = " + date);
        }
        return date;
    }

    public void renewBook(String bookId) throws SQLException{
        renewBookStat.setString(1, bookId);
        renewBookStat.setString(2, userName);
        System.out.println(renewBookStat);
        renewBookStat.execute();
    }


    private final static String renewBook =
            "UPDATE borrow " +
            "SET date = CURRENT_DATE " +
            "WHERE book_id = ? AND user_id = ?";
    private final static String selectBorrowDate =
            "SELECT date " +
            "FROM borrow " +
            "WHERE user_id = ? AND book_id = ?";
    private final static String deleteBook =
            "DELETE FROM book " +
            "WHERE book_id = ?";
    private final static String insertTranslate =
            "INSERT INTO translate " +
            "VALUE(?, ?)";
    private final static String insertWork =
            "INSERT INTO work " +
            "VALUE(?, ?)";
    private final static String insertBook =
            "INSERT INTO book " +
            "VALUE(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private final static String bookMultiQuery =
            "SELECT DISTINCT book_id " +
            "FROM book NATURAL JOIN work " +
            "WHERE ";
    private final static String selectUserAction =
            "SELECT * " +
            "FROM user " +
            "ORDER BY user_id";
    private final static String[] grantAction = {
            "GRANT SELECT, INSERT, DELETE ON library.borrow TO ?",
            "GRANT SELECT, INSERT ON library.history TO ?",
            "GRANT SELECT ON library.book TO ?",
            "GRANT SELECT ON library.translate TO ?",
            "GRANT SELECT ON library.work TO ?"
    };
    private final static String dropUserAction =
            "DROP user ?";
    private final static String delUserAction =
            "DELETE FROM user WHERE user_id = ?";
    private final static String createUserAction =
            "CREATE user ? identified BY ?";
    private final static String insertUserAction =
            "INSERT INTO user " +
            "VALUE (?, ?, ? ,?)";
    private final static String bookIsbn13Query =
            "SELECT book_id " +
            "FROM book " +
            "WHERE isbn13 = ?";
    private final static String bookIsbn10Query =
            "SELECT book_id " +
            "FROM book " +
            "WHERE isbn10 = ?";
    private final static String bookTitleQuery =
            "SELECT book_id " +
            "FROM book " +
            "WHERE title REGEXP ? ";
    private final static String bookStockQuery =
            "SELECT book_id " +
            "FROM book " +
            "WHERE stock >= ?";
    private final static String bookAuthorQuery =
            "SELECT book_id " +
            "FROM work " +
            "WHERE author REGEXP ?";
    private final static String insertHistoryAction =
            "INSERT INTO history " +
            "VALUE (?, ?, ?, CURRENT_DATE )";
    private final static String historyQuery =
            "SELECT book_id, action, date " +
            "FROM history " +
            "WHERE user_id = ? " +
            "ORDER BY date DESC";
    private final static String userBookQuery =
            "SELECT book_id, date " +
            "FROM borrow " +
            "WHERE user_id = ?";
    private final static String bookWriteQuery =
            "SELECT author " +
            "FROM work " +
            "WHERE book_id = ?";
    private final static String bookTranslateQuery =
            "SELECT translator " +
            "FROM translate " +
            "WHERE book_id = ?";
    private final static String bookIdQuery =
            "SELECT * " +
            "FROM book " +
            "WHERE book_id = ?";
    private PreparedStatement insertTranslateStat;
    private PreparedStatement insertWorkStat;
    private PreparedStatement insertHistoryStat;
    private PreparedStatement historyStat;
    private PreparedStatement userBookStat;
    private PreparedStatement bookWirteStat;
    private PreparedStatement bookTranslate;
    private PreparedStatement bookStat;
    private PreparedStatement bookTitleStat;
    private PreparedStatement bookIsbn10Stat;
    private PreparedStatement bookIsbn13Stat;
    private PreparedStatement bookAuthorStat;
    private PreparedStatement bookStockStat;
    private PreparedStatement createUserStat;
    private PreparedStatement insertUserStat;
    private PreparedStatement grantStat;
    private PreparedStatement delUserStat;
    private PreparedStatement dropUserStat;
    private PreparedStatement insertBookStat;
    private PreparedStatement deleteBookStat;
    private PreparedStatement selectBorrowDateStat;
    private PreparedStatement renewBookStat;

}
