package com.inventrax.atc.util;

import android.util.Log;


import com.inventrax.atc.common.collections.Lists;
import com.inventrax.atc.common.exceptions.UnexpectedException;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for Dates and Calendars
 */
public abstract class DateUtils {

    /**
     * Seconds in a minute
     */
    public static final int SECONDS_PER_MINUTE = 60;

    /**
     * Seconds in an hour
     */
    public static final int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;

    /**
     * Seconds in a day
     */
    public static final int SECONDS_PER_DAY = SECONDS_PER_HOUR * 24;

    /**
     * Seconds in a week
     */
    public static final int WEEK = SECONDS_PER_DAY * 7;

    /**
     * Number of milliseconds in a standard second.
     */
    public static final long MILLIS_PER_SECOND = 1000;

    /**
     * Number of milliseconds in a standard minute.
     */
    public static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;

    /**
     * Number of milliseconds in a standard hour.
     */
    public static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

    /**
     * Number of milliseconds in a standard day.
     */
    public static final long MILLIS_PER_DAY = 24 * MILLIS_PER_HOUR;

    /**
     * Date format like yyyy-MM-ddTHH:mm:ss Z
     */
    public static final String YYYYMMDDTHHMMSSZ_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZZZZ";

    /**
     * Date format like yyyy-MM-dd HH:mm:ss Z
     */
    public static final String YYYYMMDDHHMMSSZ_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    /**
     * Date format like 2010-10-25 21:30:00
     */
    public static final String YYYYMMDDHHMMSS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Date format like 2010-10-25 21:30:00
     */
    public static final String DDMMMYYYYHHMMSS_DATE_FORMAT = "dd-MMM-yyyy hh:mm:ss aa";


    /**
     * Date format like 2010-10-25 21:30:00.123
     */
    public static final String YYYYMMDDHHMMSSSSS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * Date format like 10/25/2010 21:30
     */
    public static final String MMDDYYYYHHMM_DATE_FORMAT = "MM/dd/yyyy HH:mm";

    /**
     * Date format like 10/25/2010
     */
    public static final String MMDDYYYY_DATE_FORMAT = "MM/dd/yyyy";

    /**
     * Date format like 10-25-2010
     */
    public static final String MMDDYYYY_SLASH_DATE_FORMAT = "MM-dd-yyyy";

    /**
     * Date format like 2010-10-25
     */
    public static final String YYYYMMDD_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Date format like Fri 5:15 AM
     */
    public static final String EHHMMAA_DATE_FORMAT = "E hh:mm aa";

    /**
     * Date format like Fri
     */
    public static final String E_DATE_FORMAT = "E";

    /**
     * Date format like Friday
     */
    public static final String EEEE_DATE_FORMAT = "EEEE";

    /**
     * Date format like Nov 5 3:45 PM
     */
    public static final String MMMDHHMMAA_DATE_FORMAT = "MMM d hh:mm aa";

    /**
     * Date format like Nov 5 1985 3:45 PM
     */
    public static final String MMMDYYYYHHMMAA_DATE_FORMAT = "MMM d yyyy hh:mm aa";

    /**
     * Date format like Nov 5 1985
     */
    public static final String MMMDYYYY_DATE_FORMAT = "MMM d yyyy";

    /**
     * Date format like Monday Nov 5 1985
     */
    public static final String EEEE_MMMDYYYY_DATE_FORMAT = "EEEE MMM d yyyy";

    /**
     * Date format like Nov 5
     */
    public static final String MMMD_DATE_FORMAT = "MMM d";

    /**
     * Date format like Monday Nov 5
     */
    public static final String EEEE_MMMD_DATE_FORMAT = "EEEE MMM d";

    /**
     * Date format like November 5
     */
    public static final String MMMMD_DATE_FORMAT = "MMMM d";

    /**
     * Date format like 03:45 PM
     */
    public static final String HHMMAA_DATE_FORMAT = "hh:mm aa";

    /**
     * Date format like 21:45
     */
    public static final String HHMM_DATE_FORMAT = "HH:mm";

    /**
     * Date format like 21:45:34
     */
    public static final String HHMMSS_DATE_FORMAT = "HH:mm:ss";

    /**
     * Date format like Friday 5 November
     */
    public static final String EEEEDMMMM_DATE_FORMAT = "EEEE d MMMM";

    /**
     * Date format like Friday November
     */
    public static final String EEEEMMMM_DATE_FORMAT = "EEEE MMMM";

    /**
     * Date format like Sat, Dec 15, 2012
     */
    public static final String EEMMMDYYYY_DATE_FORMAT = "EE, MMM d, yyyy";

    public static void init() {
        // nothing...
    }

    /**
     * @param dateFormatted The formatted string to parse
     * @param dateFormat
     * @return A date that represents the formatted string
     */
    public static Date parse(String dateFormatted, String dateFormat) {
        return DateUtils.parse(dateFormatted, new SimpleDateFormat(dateFormat));
    }

    /**
     * @param dateFormatted The formatted string to parse
     * @param dateFormat
     * @return A date that represents the formatted string
     */
    public static Date parse(String dateFormatted, SimpleDateFormat dateFormat) {
        Date date = null;
        if (StringUtils.isNotEmpty(dateFormatted)) {
            try {
                date = dateFormat.parse(dateFormatted);
            } catch (ParseException e) {
                throw new UnexpectedException("Error parsing the date: '" + dateFormatted + "' with pattern: "
                        + dateFormat.toPattern(), e);
            }
        }
        return date;
    }

    /**
     * Transform the {@link Date} to a {@link String} with a format like Friday November 5th
     *
     * @param date The {@link Date} to be formatted
     * @return A String that represent the date with the pattern
     */
    public static String formatToCardinal(Date date) {
        int day = DateUtils.getDayOfMonth(date);
        String ordinalSuffix = NumberUtils.getOrdinalSuffix(day);
        StringBuilder builder = new StringBuilder();
        builder.append(format(date, EEEEMMMM_DATE_FORMAT));
        builder.append(" ");
        builder.append(day);
        builder.append(ordinalSuffix);
        return builder.toString();
    }

    /**
     * @param date       The {@link Date} to be formatted
     * @param dateFormat The {@link DateFormat} used to format the {@link Date}
     * @return A String that represent the date with the pattern
     */
    public static String format(Date date, String dateFormat) {
        return DateUtils.format(date, new SimpleDateFormat(dateFormat));
    }

    /**
     * Transform the {@link Date} to a {@link String} using the received {@link SimpleDateFormat}
     *
     * @param date       The {@link Date} to be formatted
     * @param dateFormat The {@link DateFormat} used to format the {@link Date}
     * @return A String that represent the date with the pattern
     */
    public static String format(Date date, DateFormat dateFormat) {
        return date != null ? dateFormat.format(date) : null;
    }

    public static String formatDateTime(Date date) {
        return format(date, DateFormat.getDateTimeInstance());
    }

    public static String formatDate(Date date) {
        return format(date, DateFormat.getDateInstance());
    }

    public static String formatTime(Date date) {
        return format(date, DateFormat.getTimeInstance(DateFormat.SHORT));
    }

    /**
     * Creates a {@link Date} for the specified day
     *
     * @param year        The year
     * @param monthOfYear The month number (starting on 0)
     * @param dayOfMonth  The day of the month
     * @return The {@link Date}
     */
    public static Date getDate(int year, int monthOfYear, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, monthOfYear, dayOfMonth);
        truncateTime(calendar);
        return calendar.getTime();
    }

    public static Date getDate(Date date, Date time, Boolean is24Hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (time != null) {
            calendar.set(Calendar.HOUR_OF_DAY, DateUtils.getHour(time, is24Hour));
            calendar.set(Calendar.MINUTE, DateUtils.getMinute(time));
            calendar.set(Calendar.SECOND, 0);
        } else {
            truncateTime(calendar);
        }
        return calendar.getTime();
    }

    public static Date getDate(Date date, Date time) {
        return getDate(date, time, true);
    }

    public static Date getDate(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return calendar.getTime();
    }

    public static Date getTime(int hour, int minutes) {
        return getTime(hour, minutes, true);
    }

    public static Date getTime(int hour, int minutes, Boolean is24Hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(is24Hour ? Calendar.HOUR_OF_DAY : Calendar.HOUR, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        truncateDate(calendar);
        return calendar.getTime();
    }

    public static int getYear() {
        return getYear(now());
    }

    public static int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public static int getMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH);
    }

    public static int getDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static int getDayOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getHour(Date date, Boolean is24Hour) {
        return DateUtils.getHour(date, TimeZone.getDefault(), is24Hour);
    }

    public static int getHour(Date date, TimeZone timeZone, Boolean is24Hour) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(timeZone);
        return calendar.get(is24Hour ? Calendar.HOUR_OF_DAY : Calendar.HOUR);
    }

    public static int getMinute(Date date) {
        return DateUtils.getMinute(date, TimeZone.getDefault());
    }

    public static int getMinute(Date date, TimeZone timeZone) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.setTimeZone(timeZone);
        return calendar.get(Calendar.MINUTE);
    }

    public static DayOfWeek getWeekDay() {
        return getWeekDay(DateUtils.now());
    }

    public static DayOfWeek getWeekDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        return DayOfWeek.findByNumber(dayOfWeek);
    }

    public static Date addSeconds(Date date, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    public static Date addMonths(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, days);
        return calendar.getTime();
    }

    public static Date addYears(Date date, int years) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    /**
     * Truncate the date removing hours, minutes, seconds and milliseconds
     *
     * @param date The {@link Date} to truncate
     * @return The truncated {@link Date}
     */
    public static Date truncateTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        truncateTime(calendar);
        return calendar.getTime();
    }

    /**
     * Truncate the date asigning it to 1st of January of 1980
     *
     * @param date The {@link Date} to truncate
     * @return The truncated {@link Date}
     */
    public static Date truncateDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        truncateDate(calendar);
        return calendar.getTime();
    }

    /**
     * Truncate the {@link Calendar} date asigning it to 1st of January of 1980
     *
     * @param calendar The {@link Calendar} to truncate
     */
    public static void truncateDate(Calendar calendar) {
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.YEAR, 1980);
    }

    /**
     * Truncate the {@link Calendar} removing hours, minutes, seconds and milliseconds
     *
     * @param calendar The {@link Calendar} to truncate
     */
    public static void truncateTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * @return the current moment
     */
    public static Date now() {
        return new Date();
    }

    /**
     * @param date      The date to compare
     * @param startDate The left between' side
     * @param endDate   The right between's side
     * @return <code>true</code> if the date is in the middle of startDate and endDate
     */
    public static boolean isBetween(Date date, Date startDate, Date endDate) {
        return DateUtils.isBeforeEquals(startDate, date) && DateUtils.isAfterEquals(endDate, date);
    }

    /**
     * Tests if the date is before than the specified dateToCompare.
     *
     * @param date          the date to compare with the dateToCompare.
     * @param dateToCompare the date to compare with the date.
     * @return <code>true</code> if the instant of time represented by <code>date</code> object is earlier than the
     * instant represented by <tt>dateToCompare</tt>; <code>false</code> otherwise.
     */
    public static boolean isBefore(Date date, Date dateToCompare) {
        return date.compareTo(dateToCompare) < 0;
    }

    /**
     * Tests if the date is before or equals than the specified dateToCompare.
     *
     * @param date          the date to compare with the dateToCompare.
     * @param dateToCompare the date to compare with the date.
     * @return <code>true</code> if the instant of time represented by <code>date</code> object is earlier or equal than
     * the instant represented by <tt>dateToCompare</tt>; <code>false</code> otherwise.
     */
    public static boolean isBeforeEquals(Date date, Date dateToCompare) {
        return date.compareTo(dateToCompare) <= 0;
    }

    /**
     * Tests if the date is after or equals than the specified dateToCompare.
     *
     * @param date          the date to compare with the dateToCompare.
     * @param dateToCompare the date to compare with the date.
     * @return <code>true</code> if the instant of time represented by <code>date</code> object is later or equal than
     * the instant represented by <tt>dateToCompare</tt>; <code>false</code> otherwise.
     */
    public static boolean isAfterEquals(Date date, Date dateToCompare) {
        return date.compareTo(dateToCompare) >= 0;
    }

    /**
     * Tests if the date is after than the specified dateToCompare.
     *
     * @param date          the date to compare with the dateToCompare.
     * @param dateToCompare the date to compare with the date.
     * @return <code>true</code> if the instant of time represented by <code>date</code> object is later than the
     * instant represented by <tt>dateToCompare</tt>; <code>false</code> otherwise.
     */
    public static boolean isAfter(Date date, Date dateToCompare) {
        return date.compareTo(dateToCompare) > 0;
    }

    /**
     * Returns true if two periods overlap
     *
     * @param startDate1 the period one start date
     * @param endDate1   the period one end date
     * @param startDate2 the period two start date
     * @param endDate2   the period two end date
     * @return true if overlap
     */
    public static boolean periodsOverlap(Date startDate1, Date endDate1, Date startDate2, Date endDate2) {
        return (startDate1.before(endDate2) || startDate1.equals(endDate2))
                && (endDate1.after(startDate2) || endDate1.equals(startDate2));
    }

    /**
     * Returns true if the first period contains the second periods
     *
     * @param startDate1 the period one start date
     * @param endDate1   the period one end date
     * @param startDate2 the period two start date
     * @param endDate2   the period two end date
     * @return true if the first period contains the second period
     */
    public static boolean containsPeriod(Date startDate1, Date endDate1, Date startDate2, Date endDate2) {
        return DateUtils.isBeforeEquals(startDate1, startDate2) && DateUtils.isAfterEquals(endDate1, endDate2);
    }

    private static Calendar todayCalendar() {
        Calendar calendar = Calendar.getInstance();
        DateUtils.truncateTime(calendar);
        return calendar;
    }

    /**
     * @return a day after today
     */
    public static Date tomorrow() {
        Calendar calendar = DateUtils.todayCalendar();
        return DateUtils.addDays(calendar.getTime(), 1);
    }

    public static Date today() {
        Calendar calendar = DateUtils.todayCalendar();
        return calendar.getTime();
    }

    /**
     * @return a day before today
     */
    public static Date yesterday() {
        Calendar calendar = DateUtils.todayCalendar();
        return DateUtils.addDays(calendar.getTime(), -1);
    }

    /**
     * @param months amount of months to move the calendar
     * @return a date that is <code>months</code> in the future/past. Use negative values for past dates.
     */
    public static Date monthsAway(int months) {
        return DateUtils.addMonths(DateUtils.todayCalendar().getTime(), months);
    }

    /**
     * @return a date that is one month in the future
     */
    public static Date oneMonthInFuture() {
        return DateUtils.monthsAway(1);
    }

    /**
     * @return a date that is one month in the past
     */
    public static Date oneMonthInPast() {
        return DateUtils.monthsAway(-1);
    }

    /**
     * @param date Date that includes the desired month in order to calculate the last day of that month
     * @return the date of the last day of the month
     */
    public static Date getLastDayOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        DateUtils.truncateTime(calendar);
        return calendar.getTime();
    }

    public static String formatDuration(long duration) {

        long hours = TimeUnit.MILLISECONDS.toHours(duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration) - (hours * 60);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) - (hours * 60 * 60) - (minutes * 60);
        long milliseconds = TimeUnit.MILLISECONDS.toMillis(duration) - (hours * 60 * 60 * 1000) - (minutes * 60 * 1000)
                - (seconds * 1000);

        StringBuilder builder = new StringBuilder();
        if (hours > 0) {
            builder.append(hours);
            builder.append("h, ");
        }
        if ((minutes > 0) || (builder.length() > 0)) {
            builder.append(minutes);
            builder.append("m, ");
        }
        if ((seconds > 0) || (builder.length() > 0)) {
            builder.append(seconds);
            builder.append("s, ");
        }
        if (milliseconds >= 0) {
            builder.append(milliseconds);
            builder.append("ms");
        }

        return builder.toString();
    }

    public enum DayOfWeek {

        SUNDAY("Sunday", 1, true),
        MONDAY("Monday", 2, false),
        TUESDAY("Tuesday", 3, false),
        WEDNESDAY("Wednesday", 4, false),
        THURSDAY("Thursday", 5, false),
        FRIDAY("Friday", 6, false),
        SATURDAY("Saturday", 7, true);

        private String name;
        private int number;
        private Boolean weekend;

        private DayOfWeek(String name, int number, Boolean weekend) {
            this.name = name;
            this.number = number;
            this.weekend = weekend;
        }

        public static DayOfWeek findByNumber(int number) {
            for (DayOfWeek each : values()) {
                if (each.getNumber() == number) {
                    return each;
                }
            }
            return null;
        }

        public static List<DayOfWeek> getWeekDays() {
            List<DayOfWeek> weekDays = Lists.newArrayList();
            for (DayOfWeek each : DayOfWeek.values()) {
                if (!each.isWeekend()) {
                    weekDays.add(each);
                }
            }
            return weekDays;
        }

        public Boolean isWeekend() {
            return weekend;
        }

        public DayOfWeek getNextDay() {
            return findByNumber((number % 7) + 1);
        }

        @Override
        public String toString() {
            return name;
        }

        public int getNumber() {
            return number;
        }


    }


    public static String getTimeStamp() {
        return new Timestamp(new Date().getTime()).toString();
    }

    public static String getDate() {
        return new SimpleDateFormat("dd-MM-yyyy").format(new Date());
    }

    public static String getDate(String format) {
        return new SimpleDateFormat(format).format(new Date());
    }

    public static String getHourMinuteSeconds() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);

        return hours + "" + minutes + "" + seconds;
    }


    public static String formatDate(String date) {
        SimpleDateFormat fromUser = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MMM-yyyy");

        try {

            return myFormat.format(fromUser.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();

        }

        return "";
    }

    public static String formatdatetime(String date) {
        SimpleDateFormat User = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
        SimpleDateFormat Format = new SimpleDateFormat("dd-MM-yyyy hh:mm aa");

        try {

            return Format.format(User.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();

        }
        return "";
    }

    public static String changeDateFormat(String currentFormat, String requiredFormat, String dateString){
        String result="";

        try {
            String input = dateString;
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
            Log.d("Date>>>>>>>>>   ",outputFormat.format(inputFormat.parse(input)));
            result=outputFormat.format(inputFormat.parse(input));
        } catch (Exception e)
        {
            e.printStackTrace();
        }





      /* SimpleDateFormat formatterOld = new SimpleDateFormat(currentFormat, Locale.getDefault());
        SimpleDateFormat formatterNew = new SimpleDateFormat(requiredFormat,Locale.getDefault());
        Date date=null;
        try {
            date = formatterOld.parse(dateString);
            formatterOld.setTimeZone(TimeZone.getDefault());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (date != null) {
            result = formatterNew.format(date);
            formatterNew.setTimeZone(TimeZone.getDefault());
            Log.d("prasanndate",""+result);

        }*/
        return result;
    }


    }


