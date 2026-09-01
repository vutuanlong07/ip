package time;

import java.security.InvalidParameterException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateTimeFormatter {
    public static final List<String> DAYS_OF_WEEK_SHORT = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
    public static final List<String> DAYS_OF_WEEK_LONG = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    public static final List<String> MONTHS_SHORT = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    public static final List<String> MONTH_LONG = List.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

    private static final List<Pattern> TIME_SHORT_PATTERNS = List.of(
            Pattern.compile("^\\b(?<hour>\\d{2}):?(?<minute>\\d{2})\\b\\s*")
    );
    private static final List<Pattern> TIME_LONG_PATTERNS = List.of(
            Pattern.compile("^\\b(?<hour>\\d{2}):(?<minute>\\d{2}):(?<second>\\d{2})\\b\\s*")
    );

    private static final Pattern YESTERDAY_PATTERN = Pattern.compile(
            "^\\byesterday(?<yesterday>(?: yesterday)*)\\b\\s*"
    );
    private static final Pattern TOMORROW_PATTERN = Pattern.compile(
            "^\\btomorrow(?<tomorrow>(?: tomorrow)*)\\b\\s*"
    );
    private static final Pattern DAYS_OF_WEEK_SHORT_PATTERN = Pattern.compile(
            "^\\b(?<next>next )*(?<dayOfWeekShort>"
                    + String.join("|", DAYS_OF_WEEK_SHORT)
                    + ")\\b\\s*"
    );
    private static final Pattern DAYS_OF_WEEK_LONG_PATTERN = Pattern.compile(
            "^\\b(?<next>next )*(?<dayOfWeekLong>"
                    + String.join("|", DAYS_OF_WEEK_LONG)
                    + ")\\b\\s*"
    );
    private static final List<Pattern> DATE_SHORT_PATTERNS = List.of(
            Pattern.compile(
                    "^\\b(?<monthShort>"
                            + String.join("|", MONTHS_SHORT)
                            + ")\\s+(?<day>\\d{1,2}),\\s*(?<year>\\d{1,4})\\b\\s*"
            ),
            Pattern.compile(
                    "^\\b(?<day>\\d{1,2})\\s+(?<monthShort>"
                            + String.join("|", MONTHS_SHORT)
                            + ")[, ]\\s*(?<year>\\d{1,4})\\b\\s*"
            )
    );
    private static final List<Pattern> DATE_LONG_PATTERNS = List.of(
            Pattern.compile(
                    "^\\b(?<day>\\d{1,2})\\s+(?<monthLong>\\d"
                            + String.join("|", MONTH_LONG)
                            + ")[, ]\\s*(?<year>\\d{1,4})\\b\\s*"
            ),
            Pattern.compile(
                    "^\\b(?<monthLong>"
                            + String.join("|", MONTH_LONG)
                            + ")\\s+(?<day>\\d{1,2}),\\s*(?<year>\\d{1,4})\\b\\s*"
            )
    );
    private static final List<Pattern> DATE_NUMBER_PATTERNS = List.of(
            Pattern.compile("^\\b(?<day>\\d{1,2})([/-])(?<month>\\d{1,2})\\2(?<year>\\d{1,4})\\b\\s*")
    );

    private static final Pattern SECONDS_PATTERN = Pattern.compile("^\\b(?<seconds>\\d+)\\s*(?:s|sec| seconds)\\b\\s*");
    private static final Pattern MINUTES_PATTERN = Pattern.compile("^\\b(?<minutes>\\d+)\\s*(?:m|min| minutes)\\b\\s*");
    private static final Pattern HOURS_PATTERN = Pattern.compile("^\\b(?<hours>\\d+)\\s*(?:h|hr|hrs| hours)\\b\\s*");
    private static final Pattern DAYS_PATTERN = Pattern.compile("^\\b(?<days>\\d+)\\s*(?:d| days)\\b\\s*");

    private static LocalDateTime parseTimestamp(String input) throws DateTimeParseException {
        List<Matcher> timeShortMatchers = TIME_SHORT_PATTERNS.stream().map(pattern -> pattern.matcher(input)).toList();
        List<Matcher> timeLongMatchers = TIME_LONG_PATTERNS.stream().map(pattern -> pattern.matcher(input)).toList();

        Matcher yesterdayMatcher = YESTERDAY_PATTERN.matcher(input);
        Matcher tomorrowMatcher = TOMORROW_PATTERN.matcher(input);
        Matcher dayOfWeekShortMatcher = DAYS_OF_WEEK_SHORT_PATTERN.matcher(input);
        Matcher dayOfWeekLongMatcher = DAYS_OF_WEEK_LONG_PATTERN.matcher(input);
        List<Matcher> dateShortMatchers = DATE_SHORT_PATTERNS.stream().map(pattern -> pattern.matcher(input)).toList();
        List<Matcher> dateLongMatchers = DATE_LONG_PATTERNS.stream().map(pattern -> pattern.matcher(input)).toList();
        List<Matcher> dateNumberMatchers = DATE_NUMBER_PATTERNS.stream().map(pattern -> pattern.matcher(input)).toList();

        LocalDateTime now = LocalDateTime.now();
        int index = 0;
        int second = 0, minute = 0, hour = 0, day = now.getDayOfMonth(), month = now.getMonthValue(), year = now.getYear();
        boolean setTime = false, setDate = false;
        while (index < input.length()) {
            if (!setTime) {
                boolean hasMatched = false;
                for (Matcher timeShortMatcher : timeShortMatchers) {
                    if (timeShortMatcher.find()) {
                        minute = Integer.parseInt(timeShortMatcher.group("minute"));
                        hour = Integer.parseInt(timeShortMatcher.group("hour"));
                        index = timeShortMatcher.end();
                        setTime = hasMatched = true;
                        break;
                    }
                }
                if (hasMatched) continue;
                for (Matcher timeLongMatcher : timeLongMatchers) {
                    if (timeLongMatcher.find()) {
                        second = Integer.parseInt(timeLongMatcher.group("second"));
                        minute = Integer.parseInt(timeLongMatcher.group("minute"));
                        hour = Integer.parseInt(timeLongMatcher.group("hour"));
                        index = timeLongMatcher.end();
                        setTime = hasMatched = true;
                        break;
                    }
                }
                if (hasMatched) continue;
            }
            if (!setDate) {
                if (yesterdayMatcher.find(index)) {
                    int count = dayOfWeekShortMatcher.group("yesterday").length() / 10 + 1;
                    LocalDate targetDate = LocalDate.now().minusDays(count);
                    day = targetDate.getDayOfMonth();
                    month = targetDate.getMonthValue();
                    year = targetDate.getYear();
                    index = yesterdayMatcher.end();
                    setDate = true;
                    continue;
                }
                if (tomorrowMatcher.find(index)) {
                    int count = tomorrowMatcher.group("tomorrow").length() / 9 + 1;
                    LocalDate targetDate = LocalDate.now().plusDays(count);
                    day = targetDate.getDayOfMonth();
                    month = targetDate.getMonthValue();
                    year = targetDate.getYear();
                    index = tomorrowMatcher.end();
                    setDate = true;
                    continue;
                }
                if (dayOfWeekShortMatcher.find(index)) {
                    int dayOfWeekShort = DAYS_OF_WEEK_SHORT.indexOf(dayOfWeekShortMatcher.group("dayOfWeekShort")) + 1;
                    LocalDate targetDate = LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.of(dayOfWeekShort)))
                            .plusWeeks(dayOfWeekShortMatcher.group("next").length() / 5);
                    day = targetDate.getDayOfMonth();
                    month = targetDate.getMonthValue();
                    year = targetDate.getYear();
                    index = dayOfWeekShortMatcher.end();
                    setDate = true;
                    continue;
                }
                if (dayOfWeekLongMatcher.find(index)) {
                    int dayOfWeekLong = DAYS_OF_WEEK_LONG.indexOf(dayOfWeekLongMatcher.group("dayOfWeekLong")) + 1;
                    LocalDate targetDate = LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.of(dayOfWeekLong)))
                            .plusWeeks(dayOfWeekShortMatcher.group("next").length() / 5);
                    day = targetDate.getDayOfMonth();
                    month = targetDate.getMonthValue();
                    year = targetDate.getYear();
                    index = dayOfWeekLongMatcher.end();
                    setDate = true;
                    continue;
                }
                boolean hasMatched = false;
                for (Matcher dateNumberMatcher : dateNumberMatchers) {
                    if (dateNumberMatcher.find(index)) {
                        day = Integer.parseInt(dateNumberMatcher.group("day"));
                        month = Integer.parseInt(dateNumberMatcher.group("month"));
                        year = Integer.parseInt(dateNumberMatcher.group("year"));
                        index = dateNumberMatcher.end();
                        setDate = hasMatched = true;
                        break;
                    }
                }
                if (hasMatched) continue;
                for (Matcher dateShortMatcher : dateShortMatchers) {
                    if (dateShortMatcher.find(index)) {
                        day = Integer.parseInt(dateShortMatcher.group("day"));
                        month = MONTHS_SHORT.indexOf(dateShortMatcher.group("monthShort")) + 1;
                        year = Integer.parseInt(dateShortMatcher.group("year"));
                        index = dateShortMatcher.end();
                        setDate = hasMatched = true;
                        break;
                    }
                }
                if (hasMatched) continue;
                for (Matcher dateLongMatcher : dateLongMatchers) {
                    if (dateLongMatcher.find(index)) {
                        day = Integer.parseInt(dateLongMatcher.group("day"));
                        month = MONTH_LONG.indexOf(dateLongMatcher.group("monthLong")) + 1;
                        year = Integer.parseInt(dateLongMatcher.group("year"));
                        index = dateLongMatcher.end();
                        setDate = hasMatched = true;
                        break;
                    }
                }
                if (hasMatched) continue;
            }
            throw new DateTimeParseException("Invalid date-time", input, index);
        }
        return LocalDateTime.of(year, month, day, hour, second, minute);
    }

    private static LocalDateTime parseRelativeTime(String input) throws InvalidParameterException, DateTimeParseException {
        if (!input.endsWith("later")) {
            throw new DateTimeParseException("No 'later' indicator", input, input.length() < 5 ? 0 : input.length() - 5);
        }
        input =  input.substring(0, input.length() - 5);

        Matcher secondsMatcher = SECONDS_PATTERN.matcher(input);
        Matcher minutesMatcher = MINUTES_PATTERN.matcher(input);
        Matcher hoursMatcher = HOURS_PATTERN.matcher(input);
        Matcher daysMatcher = DAYS_PATTERN.matcher(input);

        int index = 0;
        int seconds = 0, minutes = 0, hours = 0, days = 0;
        boolean setSeconds = false, setMinutes = false, setHours = false, setDays = false;
        while (index < input.length()) {
            if (secondsMatcher.find(index)) {
                seconds += Integer.parseInt(secondsMatcher.group("seconds"));
                setSeconds = true;
                index = secondsMatcher.end();
            } else if (minutesMatcher.find(index)) {
                minutes += Integer.parseInt(minutesMatcher.group("minutes"));
                setMinutes = true;
                index = minutesMatcher.end();
            } else if (hoursMatcher.find(index)) {
                hours += Integer.parseInt(hoursMatcher.group("hours"));
                setHours = true;
                index = hoursMatcher.end();
            } else if (daysMatcher.find(index)) {
                days += Integer.parseInt(daysMatcher.group("days"));
                setDays = true;
                index = daysMatcher.end();
            } else {
                throw new DateTimeParseException("Invalid duration", input, index);
            }
        }

        return (setSeconds
                ? LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                : setMinutes
                  ? LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
                  : setHours
                    ? LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
                    : LocalDateTime.now().truncatedTo(ChronoUnit.DAYS))
                .plusSeconds(seconds)
                .plusMinutes(minutes)
                .plusHours(hours)
                .plusDays(days);
    }

    public static LocalDateTime parseDateTime(String input) throws InvalidParameterException, DateTimeParseException {
        if (input.equals("now")) {
            return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        } else if (input.endsWith("later")) {
            return parseRelativeTime(input);
        } else {
            return parseTimestamp(input);
        }
    }

    public static String formatDateTime(LocalDateTime input) {
        StringBuilder res =  new StringBuilder();
        res.append(String.format("%02d:%02d:%02d ", input.getHour(), input.getMinute(), input.getSecond()));

        LocalDate today = LocalDate.now();
        int currentYear = today.get(WeekFields.ISO.weekBasedYear());
        int currentWeek = today.get(WeekFields.ISO.weekOfWeekBasedYear());
        int currentDay = today.getDayOfWeek().getValue();
        int inputYear = input.get(WeekFields.ISO.weekBasedYear());
        int inputWeek = input.get(WeekFields.ISO.weekOfWeekBasedYear());
        int inputDay = input.getDayOfWeek().getValue();

        if (inputYear == currentYear) {
            if (inputWeek == currentWeek) {
                if (inputDay == currentDay) {
                    res.append("today");
                } else if (inputDay == currentDay - 1) {
                    res.append("yesterday");
                } else if (inputDay == currentDay + 1) {
                    res.append("tomorrow");
                } else {
                    res.append(DAYS_OF_WEEK_LONG.get(input.getDayOfWeek().getValue() - 1)).append(" this week");
                }
            } else if (inputWeek == currentWeek - 1) {
                res.append(DAYS_OF_WEEK_LONG.get(input.getDayOfWeek().getValue() - 1)).append(" last week");
            } else if (inputWeek == currentWeek + 1) {
                res.append(DAYS_OF_WEEK_LONG.get(input.getDayOfWeek().getValue() - 1)).append(" next week");
            } else {
                res.append(input.getDayOfMonth()).append(' ').append(MONTH_LONG.get(input.getMonthValue() - 1));
            }
        } else {
            res.append(input.getDayOfMonth()).append(' ').append(MONTH_LONG.get(input.getMonthValue() - 1)).append(' ').append(inputYear);
        }
        return res.toString();
    }
}
