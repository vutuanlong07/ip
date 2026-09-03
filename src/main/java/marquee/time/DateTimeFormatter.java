package marquee.time;

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
import java.util.stream.Collectors;

public final class DateTimeFormatter {
    public static final List<String> DAYS_OF_WEEK = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    public static final List<String> MONTHS = List.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

    private static final List<String> DAYS_OF_WEEK_PREFIX = DAYS_OF_WEEK.stream().map(dow -> dow.substring(0, 3)).toList();
    private static final List<String> MONTHS_PREFIX = MONTHS.stream().map(month -> month.substring(0, 3)).toList();

    private static final List<Pattern> TIME_PATTERNS = List.of(
            Pattern.compile("^\\b(?<hour>\\d{2}):?(?<minute>\\d{2})\\b\\s*"),
            Pattern.compile("^\\b(?<hour>\\d{2}):(?<minute>\\d{2}):(?<second>\\d{2})\\b\\s*")
    );

    private static final Pattern TODAY_PATTERN = Pattern.compile(
            "^\\btoday\\b\\s*"
    );
    private static final Pattern YESTERDAY_PATTERN = Pattern.compile(
            "^\\byesterday(?<yesterday>(?: yesterday)*)\\b\\s*"
    );
    private static final Pattern TOMORROW_PATTERN = Pattern.compile(
            "^\\btomorrow(?<tomorrow>(?: tomorrow)*)\\b\\s*"
    );
    private static final Pattern DAYS_OF_WEEK_PATTERN = Pattern.compile(
            "^\\b(?<next>next )*(?<dayOfWeek>"
                    + DAYS_OF_WEEK_PREFIX.stream()
                    .map(dow -> dow.substring(0, 3) + "(?:" + dow.substring(3) + ")?")
                    .collect(Collectors.joining("|"))
                    + ")\\b\\s*"
    );
    private static final List<Pattern> DATE_TEXT_PATTERNS = List.of(
            Pattern.compile(
                    "^\\b(?<month>"
                            + MONTHS_PREFIX.stream()
                            .map(dow -> dow.substring(0, 3) + "(?:" + dow.substring(3) + ")?")
                            .collect(Collectors.joining("|"))
                            + ")\\s+(?<day>\\d{1,2}),\\s*(?<year>\\d{1,4})\\b\\s*"
            ),
            Pattern.compile(
                    "^\\b(?<day>\\d{1,2})\\s+(?<month>"
                            + MONTHS_PREFIX.stream()
                            .map(dow -> dow.substring(0, 3) + "(?:" + dow.substring(3) + ")?")
                            .collect(Collectors.joining("|"))
                            + ")[, ]\\s*(?<year>\\d{1,4})\\b\\s*"
            )
    );
    private static final List<Pattern> DATE_NUMBER_PATTERNS = List.of(
            Pattern.compile("^\\b(?<day>\\d{1,2})([/-])(?<month>\\d{1,2})\\2(?<year>\\d{1,4})\\b\\s*")
    );

    private static final Pattern SECONDS_PATTERN = Pattern.compile("^\\b(?<seconds>\\d+)\\s*(?: seconds|sec|s)\\b\\s*");
    private static final Pattern MINUTES_PATTERN = Pattern.compile("^\\b(?<minutes>\\d+)\\s*(?: minutes|min|m)\\b\\s*");
    private static final Pattern HOURS_PATTERN = Pattern.compile("^\\b(?<hours>\\d+)\\s*(?: hours|hrs|hr|h)\\b\\s*");
    private static final Pattern DAYS_PATTERN = Pattern.compile("^\\b(?<days>\\d+)\\s*(?: days|d)\\b\\s*");

    private static LocalDateTime parseTimestamp(String input) throws DateTimeParseException {
        List<Matcher> timeMatchers = TIME_PATTERNS.stream().map(pattern -> pattern.matcher(input)).toList();
        Matcher todayMatcher = TODAY_PATTERN.matcher(input);
        Matcher yesterdayMatcher = YESTERDAY_PATTERN.matcher(input);
        Matcher tomorrowMatcher = TOMORROW_PATTERN.matcher(input);
        Matcher dayOfWeekMatcher = DAYS_OF_WEEK_PATTERN.matcher(input);
        List<Matcher> dateTextMatchers = DATE_TEXT_PATTERNS.stream().map(pattern -> pattern.matcher(input)).toList();
        List<Matcher> dateNumberMatchers = DATE_NUMBER_PATTERNS.stream().map(pattern -> pattern.matcher(input)).toList();

        LocalDateTime now = LocalDateTime.now();
        int index = 0;
        int second = 0, minute = 0, hour = 0, day = now.getDayOfMonth(), month = now.getMonthValue(), year = now.getYear();
        boolean setTime = false, setDate = false;
        while (index < input.length()) {
            if (!setTime) {
                boolean hasMatchedTime = false;
                for (Matcher timeMatcher : timeMatchers) {
                    if (timeMatcher.find()) {
                        second = timeMatcher.namedGroups().containsKey("seconds")
                                ? Integer.parseInt(timeMatcher.group("seconds"))
                                : 0;
                        minute = Integer.parseInt(timeMatcher.group("minute"));
                        hour = Integer.parseInt(timeMatcher.group("hour"));
                        index = timeMatcher.end();
                        setTime = hasMatchedTime = true;
                        break;
                    }
                }
                if (hasMatchedTime) continue;
            }
            if (!setDate) {
                if (todayMatcher.find(index)) {
                    LocalDate targetDate = LocalDate.now();
                    day = targetDate.getDayOfMonth();
                    month = targetDate.getMonthValue();
                    year = targetDate.getYear();
                    index = todayMatcher.end();
                    setDate = true;
                    continue;
                }
                if (yesterdayMatcher.find(index)) {
                    int count = dayOfWeekMatcher.group("yesterday").length() / 10 + 1;
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
                if (dayOfWeekMatcher.find(index)) {
                    int dayOfWeek = DAYS_OF_WEEK_PREFIX.indexOf(dayOfWeekMatcher.group("dayOfWeek").substring(0, 3)) + 1;
                    LocalDate targetDate = LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.of(dayOfWeek)))
                            .plusWeeks(dayOfWeekMatcher.group("next").length() / 5);
                    day = targetDate.getDayOfMonth();
                    month = targetDate.getMonthValue();
                    year = targetDate.getYear();
                    index = dayOfWeekMatcher.end();
                    setDate = true;
                    continue;
                }
                boolean hasMatchedDate = false;
                for (Matcher dateTextMatcher : dateTextMatchers) {
                    if (dateTextMatcher.find(index)) {
                        day = Integer.parseInt(dateTextMatcher.group("day"));
                        month = MONTHS_PREFIX.indexOf(dateTextMatcher.group("month")) + 1;
                        year = Integer.parseInt(dateTextMatcher.group("year"));
                        index = dateTextMatcher.end();
                        setDate = hasMatchedDate = true;
                        break;
                    }
                }
                if (hasMatchedDate) continue;
                for (Matcher dateNumberMatcher : dateNumberMatchers) {
                    if (dateNumberMatcher.find(index)) {
                        day = Integer.parseInt(dateNumberMatcher.group("day"));
                        month = Integer.parseInt(dateNumberMatcher.group("month"));
                        year = Integer.parseInt(dateNumberMatcher.group("year"));
                        index = dateNumberMatcher.end();
                        setDate = hasMatchedDate = true;
                        break;
                    }
                }
                if (hasMatchedDate) continue;
            }
            throw new DateTimeParseException("Invalid date-time", input, index);
        }
        return LocalDateTime.of(year, month, day, hour, second, minute);
    }

    private static LocalDateTime parseRelativeTime(String input) throws DateTimeParseException {
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

    public static LocalDateTime parseDateTime(String input) throws DateTimeParseException {
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
                    res.append(DAYS_OF_WEEK.get(input.getDayOfWeek().getValue() - 1)).append(" this week");
                }
            } else if (inputWeek == currentWeek - 1) {
                res.append(DAYS_OF_WEEK.get(input.getDayOfWeek().getValue() - 1)).append(" last week");
            } else if (inputWeek == currentWeek + 1) {
                res.append(DAYS_OF_WEEK.get(input.getDayOfWeek().getValue() - 1)).append(" next week");
            } else {
                res.append(input.getDayOfMonth()).append(' ').append(MONTHS.get(input.getMonthValue() - 1));
            }
        } else {
            res.append(input.getDayOfMonth()).append(' ').append(MONTHS.get(input.getMonthValue() - 1)).append(' ').append(inputYear);
        }
        return res.toString();
    }
}
