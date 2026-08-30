package time;

import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateTimeFormatter {
    public static final List<String> daysOfWeekShort = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
    public static final List<String> daysOfWeekLong = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    public static final List<String> monthsShort = List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    public static final List<String> monthsLong = List.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

    private static final List<Pattern> timeShortPatterns = List.of(
            Pattern.compile("^\\b(?<hour>\\d{2}):?(?<minute>\\d{2})\\b\\s*")
    );
    private static final List<Pattern> timeLongPatterns = List.of(
            Pattern.compile("^\\b(?<hour>\\d{2}):(?<minute>\\d{2}):(?<second>\\d{2})\\b\\s*")
    );
    private static final List<Pattern> dateShortPatterns = List.of(
            Pattern.compile(
                    "^\\b(?<monthShort>"
                            + String.join("|", monthsShort)
                            + ")\\s+(?<day>\\d{1,2}),\\s*(?<year>\\d{1,4})\\b\\s*"
            ),
            Pattern.compile(
                    "^\\b(?<day>\\d{1,2})\\s+(?<monthShort>"
                            + String.join("|", monthsShort)
                            + ")[, ]\\s*(?<year>\\d{1,4})\\b\\s*"
            )
    );
    private static final List<Pattern> dateLongPatterns = List.of(
            Pattern.compile(
                    "^\\b(?<day>\\d{1,2})\\s+(?<monthLong>\\d"
                            + String.join("|", monthsLong)
                            + ")[, ]\\s*(?<year>\\d{1,4})\\b\\s*"
            ),
            Pattern.compile(
                    "^\\b(?<monthLong>"
                            + String.join("|", monthsLong)
                            + ")\\s+(?<day>\\d{1,2}),\\s*(?<year>\\d{1,4})\\b\\s*"
            )
    );
    private static final List<Pattern> dateNumberPatterns = List.of(
            Pattern.compile("^\\b(?<day>\\d{1,2})([/-])(?<month>\\d{1,2})\\2(?<year>\\d{1,4})\\b\\s*")
    );

    private static final Pattern secondsPattern = Pattern.compile("^\\b(?<seconds>\\d+)\\s*(?:s|sec| seconds)\\b\\s*");
    private static final Pattern minutesPattern = Pattern.compile("^\\b(?<minutes>\\d+)\\s*(?:m|min| minutes)\\b\\s*");
    private static final Pattern hoursPattern = Pattern.compile("^\\b(?<hours>\\d+)\\s*(?:h|hr|hrs| hours)\\b\\s*");
    private static final Pattern daysPattern = Pattern.compile("^\\b(?<days>\\d+)\\s*(?:d| days)\\b\\s*");

    private static LocalDateTime parseTimestamp(String input) throws DateTimeParseException {
        List<Matcher> timeShortMatchers = timeShortPatterns.stream().map(pattern -> pattern.matcher(input)).toList();
        List<Matcher> timeLongMatchers = timeLongPatterns.stream().map(pattern -> pattern.matcher(input)).toList();
        List<Matcher> dateShortMatchers = dateShortPatterns.stream().map(pattern -> pattern.matcher(input)).toList();
        List<Matcher> dateLongMatchers = dateLongPatterns.stream().map(pattern -> pattern.matcher(input)).toList();
        List<Matcher> dateNumberMatchers = dateNumberPatterns.stream().map(pattern -> pattern.matcher(input)).toList();

        LocalDateTime now = LocalDateTime.now();
        int index = 0;
        int second = 0, minute = 0, hour = 0, day = now.getDayOfMonth(), month = now.getMonthValue(), year = now.getYear();
        boolean setTime = false, setDate = false;
        while (index < input.length()) {
            if (!setTime) {
                boolean matched = false;
                for (Matcher timeShortMatcher : timeShortMatchers) {
                    if (timeShortMatcher.find()) {
                        minute = Integer.parseInt(timeShortMatcher.group("minute"));
                        hour = Integer.parseInt(timeShortMatcher.group("hour"));
                        index = timeShortMatcher.end();
                        setTime = matched = true;
                        break;
                    }
                }
                if (matched) continue;
                for (Matcher timeLongMatcher : timeLongMatchers) {
                    if (timeLongMatcher.find()) {
                        second = Integer.parseInt(timeLongMatcher.group("second"));
                        minute = Integer.parseInt(timeLongMatcher.group("minute"));
                        hour = Integer.parseInt(timeLongMatcher.group("hour"));
                        index = timeLongMatcher.end();
                        setTime = matched = true;
                        break;
                    }
                }
                if (matched) continue;
            }
            if (!setDate) {
                boolean matched = false;
                for (Matcher dateNumberMatcher : dateNumberMatchers) {
                    if (dateNumberMatcher.find(index)) {
                        day = Integer.parseInt(dateNumberMatcher.group("day"));
                        month = Integer.parseInt(dateNumberMatcher.group("month"));
                        year = Integer.parseInt(dateNumberMatcher.group("year"));
                        index = dateNumberMatcher.end();
                        setDate = matched = true;
                        break;
                    }
                }
                if (matched) continue;
                for (Matcher dateShortMatcher : dateShortMatchers) {
                    if (dateShortMatcher.find(index)) {
                        day = Integer.parseInt(dateShortMatcher.group("day"));
                        month = monthsShort.indexOf(dateShortMatcher.group("monthShort")) + 1;
                        year = Integer.parseInt(dateShortMatcher.group("year"));
                        index = dateShortMatcher.end();
                        setDate = matched = true;
                        break;
                    }
                }
                if (matched) continue;
                for (Matcher dateLongMatcher : dateLongMatchers) {
                    if (dateLongMatcher.find(index)) {
                        day = Integer.parseInt(dateLongMatcher.group("day"));
                        month = monthsLong.indexOf(dateLongMatcher.group("monthLong")) + 1;
                        year = Integer.parseInt(dateLongMatcher.group("year"));
                        index = dateLongMatcher.end();
                        setDate = matched = true;
                        break;
                    }
                }
                if (matched) continue;
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

        Matcher secondsMatcher = secondsPattern.matcher(input);
        Matcher minutesMatcher = minutesPattern.matcher(input);
        Matcher hoursMatcher = hoursPattern.matcher(input);
        Matcher daysMatcher = daysPattern.matcher(input);

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
                    res.append(daysOfWeekLong.get(input.getDayOfWeek().getValue() - 1)).append(" this week");
                }
            } else if (inputWeek == currentWeek - 1) {
                res.append(daysOfWeekLong.get(input.getDayOfWeek().getValue() - 1)).append(" last week");
            } else if (inputWeek == currentWeek + 1) {
                res.append(daysOfWeekLong.get(input.getDayOfWeek().getValue() - 1)).append(" next week");
            } else {
                res.append(input.getDayOfMonth()).append(' ').append(monthsLong.get(input.getMonthValue() - 1));
            }
        } else {
            res.append(input.getDayOfMonth()).append(' ').append(monthsLong.get(input.getMonthValue() - 1)).append(' ').append(inputYear);
        }
        return res.toString();
    }
}
