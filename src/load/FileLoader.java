package load;

import util.Book;
import util.User;
import mysql.SQLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by baislsl on 17-4-29.
 */

/**
 * <p>Handle batch import of users or books, read file of json format</p>
 *
 * book json format, *.json, a example can see {@link}[./src/test/book_lists.json]
 * {"books" : [
 *      {
 *          "id"        : "1234",
 *          "title"     : "Excited",
 *          "images"    : image_url,
 *          "author"    : ["baislsl", "zjuers"],
 *          ...
 *      },
 *
 *      {
 *          "id"        : "12345",
 *          "title"     : "Excited2",
 *          "images"    : image_url,
 *          ...
 *      },
 *      ...
 * ]}
 *
 * user json format, *.json, a example can see {@link}[./src/test/user_lists.json]
 * { "users" :[
 *      {
 *          "id"        : "@baislsl",
 *          "password"  : "123",
 *          "date"      : "2000-01-01",
 *          "mail"      : "baislsl@163.com"
 *      },
 *
 *      {
 *          "id"        : "user2",
 *          "password"  : "45678",
 *          "date"      : "1000-01-01",
 *          "mail"      : "gg@gmail.com"
 *      },
 *      ...
 * ]}
 *
 *
 * */
public class FileLoader {
    private final static String[] imageSize = {"small", "medium", "large"};
    private final static int defaultImageIndex = 0; // default size as small when catch from DouBan

    public static void BookLoader(String path, SQLConnection connect, boolean isCatchDouban)
            throws JSONException, IOException {
        Book[] books = JSONBookReader(new FileInputStream(new File(path)), isCatchDouban);
        for (Book book : books) {
            try {
                connect.insertBook(book);
            } catch (SQLException e) {
                System.out.println("Failed when inserting book : " + book.toString());
                e.printStackTrace();
            }
        }
    }

    public static void UserLoader(String path, SQLConnection connect)
            throws JSONException, IOException {
        User[] users = JSONUserReader(new FileInputStream(new File(path)));
        for (User user : users) {
            try {
                connect.createUser(user);
            } catch (SQLException e) {
                System.out.println("Failed when creating user : " + user.toString());
                e.printStackTrace();
            }
        }
    }

    public static Book[] JSONBookReader(InputStream input, boolean isCatchDouban)
            throws JSONException, IOException {
        BufferedReader read = new BufferedReader(new InputStreamReader(input, "utf-8"));
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = read.read()) != -1) {
            sb.append((char) cp);
        }
        Random random = new Random();   // when load books from Douban, the stock is undefined,
                                        // here I generate random image stock
        JSONObject json = new JSONObject(sb.toString());
        JSONArray bookLists = json.getJSONArray("books");
        Book[] res = new Book[bookLists.length()];
        for (int i = 0; i < bookLists.length(); i++) {
            Book book = new Book();
            JSONObject jbook = bookLists.getJSONObject(i);
            book.id = jbook.getString("id");
            book.isbn10 = jbook.getString("isbn10");
            book.isbn13 = jbook.getString("isbn13");
            book.title = jbook.getString("title");
            book.url = jbook.getString("url");
            book.pubdate = jbook.getString("pubdate");
            book.publisher = jbook.getString("publisher");
            book.price = jbook.getString("price");

            String pages = jbook.getString("pages");
            if (pages.length() == 0) {
                book.pages = -1;
            } else {
                System.out.print(pages);
                book.pages = Integer.valueOf(pages);
            }

            if (isCatchDouban) {
                JSONObject jimage = jbook.getJSONObject("images");
                book.img_url = (String) jimage.get(imageSize[defaultImageIndex]); // small size, default
                book.stock = random.nextInt(20);                   //random integer in [0,20)
            } else {
                book.img_url = jbook.getString("images");
                book.stock = jbook.getInt("stock");
            }

            ArrayList<String> author = new ArrayList<>();
            JSONArray jauthor = jbook.getJSONArray("author");
            for (int j = 0; j < jauthor.length(); j++) {
                author.add(jauthor.getString(j));
            }
            book.author = author.toArray(new String[0]);

            ArrayList<String> translater = new ArrayList<>();
            JSONArray jtranslater = jbook.getJSONArray("translator");
            for (int j = 0; j < jtranslater.length(); j++) {
                translater.add(jtranslater.getString(j));
            }
            book.translator = translater.toArray(new String[0]);

            res[i] = book;
        }
        return res;
    }

    private static User[] JSONUserReader(InputStream input) throws JSONException, IOException {
        BufferedReader read = new BufferedReader(new InputStreamReader(input, "utf-8"));
        StringBuilder stringBuilder = new StringBuilder();
        int cc;
        while ((cc = read.read()) != -1) {
            stringBuilder.append((char) cc);
        }
        System.out.println(stringBuilder);
        JSONObject json = new JSONObject(stringBuilder.toString());
        JSONArray userList = json.getJSONArray("users");
        User[] res = new User[userList.length()];
        for (int i = 0; i < userList.length(); i++) {
            User user = new User();
            JSONObject juser = userList.getJSONObject(i);
            user.id = juser.getString("id");
            user.password = juser.getString("password");
            user.registerTime = juser.getString("date");
            user.mailAddress = juser.getString("mail");
            res[i] = user;
        }
        return res;
    }
}

