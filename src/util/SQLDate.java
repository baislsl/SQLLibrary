package util;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by baislsl on 17-5-26.
 */
public class SQLDate {

    /**
     * @brief convert informal date into formal string. This function
     * can solve informal form such as "yyyy-MM-dd", "yyyy-MM" and "yyyy",
     * if no day given, the default value of day is 1
     * if no month given, the default value of month is 1
     */
    public static String toDate(String date) throws SQLException {
        if (date == null || date.length() == 0)
            return null;
        String[] d = date.split("-");
        switch (d.length) {
            case 3:
                return d[0] + "-" + d[1] + "-" + d[2] ;
            case 2:
                return d[0] + "-" + d[1] + "-01";
            case 1:
                return d[0] + "-01-01";
            default:
                throw new SQLException("error date format of" + date);

        }
    }

    public static String getDate() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR), month = now.get(Calendar.MONTH) + 1, day = now.get(Calendar.DAY_OF_MONTH);
        return year + "-" + month + "-" + day;
    }

    /**
     * <p>ensure the date foormat is "yyyy-MM-dd"</p>
     * @return the day distance between the given the and the system date
     * */
    public static long getDifferDay(String date){
        SimpleDateFormat dateParse = new SimpleDateFormat("yyyy-MM-dd");
        try{
            Date now = new Date();
            Date objectDate = dateParse.parse(date);
            return (now.getTime() - objectDate.getTime())/(1000 * 60 * 60 * 24);
        }catch (ParseException e){
            e.printStackTrace();
            return -1;
        }
    }
}
