package marquee.time;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Custom {@link LocalDateTime} formatter and parser that supports many date-time formats used in daily life.
 * <p>
 *     At least 1 of date or time component below must exist to be considered a valid date-time string.
 * </p>
 * <h3>Available date component formats</h3>
 * <p>
 *     <ul>
 *         <li>{@code today}</li>
 *         <li>{@code yesterday} and {@code tomorrow}, can be repeated for further future and past days</li>
 *         <li>{@code last} day-of-week for last or current day-of-week</li>
 *         <li>{@code next} day-of-week, {@code next} can be repeated for future days-of-week</li>
 *         <li>Day, month, year, separated by forward-slash {@code /} or hyphen {@code -}</li>
 *         <li>Day, written month, year, separated by 1 or more spaces <code>&nbsp;</code>, comma {@code ,} with optional trailing spaces, or hyphen {@code -}</li>
 *         <li>Written month, day, year, same separator as above except for spaces</li>
 *     </ul>
 * </p>
 * <p>
 *     Separator choices must be consistent throughout the date component
 * </p>
 * <p>
 *     Day-of-week and written month can be full or abbreviated.
 * </p>
 * <p>
 *     Year component can be omitted, then it is assumed to be the current year
 *     from {@link LocalDateTime#now()}{@link LocalDateTime#getYear() .getYear()}.
 * </p>
 * <h3>Available time component formats</h3>
 * <p>
 *     <ul>
 *         <li>Hour, minute, second, separated by semicolon {@code :}</li>
 *         <li>Hour, minute, separator optional</li>
 *     </ul>
 * </p>
 * <p>
 *     Hour, minute and seconds must be 2 digits.
 * </p>
 * <p>
 *     Second component can be omitted, then it is assumed to be 00.
 * </p>
 * <p>
 *     If date component exist but time component is missing,
 *     it is assumed to be midnight at the start of that day (00:00:00).
 * </p>
 * <p>
 *     If time component exist but date component is missing,
 *     it is assumed to be on the current day (date component taken from {@link LocalDate#now()}).
 * </p>
 */
public final class DateTimeFormatter {
    public static final List<String> DAYS_OF_WEEK = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    public static final List<String> MONTHS = List.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

    private static final String RELATIVE_TIME_TAG = "later";

    private static final List<String> DAYS_OF_WEEK_PREFIX = DAYS_OF_WEEK.stream().map(dow -> dow.substring(0, 3).toLowerCase()).toList();
    private static final List<String> MONTHS_PREFIX = MONTHS.stream().map(month -> month.substring(0, 3).toLowerCase()).toList();

    private static final Pattern TIME_FULL_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<hour>\\d{2}):(?<minute>\\d{2})(?::(?<second>\\d{2}))?\\b\\s*"
    );
    private static final Pattern TIME_ABBREV_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<hour>\\d{2})(?<minute>\\d{2})\\b\\s*"
    );
    private static final Pattern TODAY_PATTERN = Pattern.compile(
            "\\G\\s*\\btoday\\b\\s*"
    );
    private static final Pattern YESTERDAY_PATTERN = Pattern.compile(
            "\\G\\s*\\byesterday(?<yesterday>(?: yesterday)*)\\b\\s*"
    );
    private static final Pattern TOMORROW_PATTERN = Pattern.compile(
            "\\G\\s*\\btomorrow(?<tomorrow>(?: tomorrow)*)\\b\\s*"
    );
    private static final Pattern DAYS_OF_WEEK_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<nextOrLast>last|(?:next )+)(?<dayOfWeek>"
                    + DAYS_OF_WEEK.stream()
                    .map(dow -> dow.substring(0, 3) + "(?:" + dow.substring(3) + ")?")
                    .collect(Collectors.joining("|"))
                    + ")\\b\\s*"
    );
    private static final Pattern DATE_TEXT_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<month>"
                    + MONTHS.stream()
                    .map(dow ->
                            "[" + dow.charAt(0) + Character.toLowerCase(dow.charAt(0)) + "]"
                                    + dow.substring(1, 3)
                                    + "(?:" + dow.substring(3) + ")?"
                    )
                    .collect(Collectors.joining("|"))
                    + ")"
                    + "(,\\s*|-|\\s+)(?<day>\\d{1,2})"
                    + "(?:\\2(?<year>\\d{1,4}))?\\b\\s*"
    );
    private static final Pattern DATE_TEXT_REVERSED_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<day>\\d{1,2})"
                    + "(,\\s*|-)(?<month>"
                    + MONTHS.stream()
                    .map(dow ->
                            "[" + dow.charAt(0) + Character.toLowerCase(dow.charAt(0)) + "]"
                                    + dow.substring(1, 3)
                                    + "(?:" + dow.substring(3) + ")?"
                    )
                    .collect(Collectors.joining("|"))
                    + ")"
                    + "(?:\\2\\s*(?<year>\\d{1,4}))?\\b\\s*"
    );
    private static final Pattern DATE_NUMBER_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<day>\\d{1,2})([/-])(?<month>\\d{1,2})\\2(?<year>\\d{1,4})\\b\\s*"
    );
    private static final Pattern SECONDS_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<seconds>\\d+)\\s*(?: seconds|sec|s)\\b\\s*"
    );
    private static final Pattern MINUTES_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<minutes>\\d+)\\s*(?: minutes|min|m)\\b\\s*"
    );
    private static final Pattern HOURS_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<hours>\\d+)\\s*(?: hours|hrs|hr|h)\\b\\s*"
    );
    private static final Pattern DAYS_PATTERN = Pattern.compile(
            "\\G\\s*\\b(?<days>\\d+)\\s*(?: days|d)\\b\\s*"
    );

    private static LocalDateTime parseTimestamp(String input) throws DateTimeParseException {
        Matcher timeFullMatcher = TIME_FULL_PATTERN.matcher(input);
        Matcher timeAbbrevMatcher = TIME_ABBREV_PATTERN.matcher(input);
        Matcher todayMatcher = TODAY_PATTERN.matcher(input);
        Matcher yesterdayMatcher = YESTERDAY_PATTERN.matcher(input);
        Matcher tomorrowMatcher = TOMORROW_PATTERN.matcher(input);
        Matcher dayOfWeekMatcher = DAYS_OF_WEEK_PATTERN.matcher(input);
        Matcher dateTextMatcher = DATE_TEXT_PATTERN.matcher(input);
        Matcher dateTextReversedMatcher = DATE_TEXT_REVERSED_PATTERN.matcher(input);
        Matcher dateNumberMatcher = DATE_NUMBER_PATTERN.matcher(input);

        LocalDateTime now = LocalDateTime.now();
        int index = 0;
        int second = 0, minute = 0, hour = 0, day = now.getDayOfMonth(), month = now.getMonthValue(), year = now.getYear();
        boolean setTime = false, setDate = false;
        while (index < input.length()) {
            if (!setTime) {
                if (timeFullMatcher.find(index)) {
                    second = timeFullMatcher.group("second") != null
                            ? Integer.parseInt(timeFullMatcher.group("second"))
                            : second;
                    minute = Integer.parseInt(timeFullMatcher.group("minute"));
                    hour = Integer.parseInt(timeFullMatcher.group("hour"));
                    index = timeFullMatcher.end();
                    setTime = true;
                    continue;
                }
                if (timeAbbrevMatcher.find(index)) {
                    minute = Integer.parseInt(timeAbbrevMatcher.group("minute"));
                    hour = Integer.parseInt(timeAbbrevMatcher.group("hour"));
                    index = timeAbbrevMatcher.end();
                    setTime = true;
                    continue;
                }
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
                    int count = yesterdayMatcher.group("yesterday").length() / 10 + 1;
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
                    int dayOfWeek = DAYS_OF_WEEK_PREFIX.indexOf(dayOfWeekMatcher.group("dayOfWeek").substring(0, 3).toLowerCase()) + 1;
                    LocalDate targetDate = LocalDate.now()
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.of(dayOfWeek)))
                            .plusWeeks(dayOfWeekMatcher.group("nextOrLast").equals("last")
                                    ? 0
                                    : dayOfWeekMatcher.group("nextOrLast").length() / 5
                            );
                    day = targetDate.getDayOfMonth();
                    month = targetDate.getMonthValue();
                    year = targetDate.getYear();
                    index = dayOfWeekMatcher.end();
                    setDate = true;
                    continue;
                }
                if (dateTextMatcher.find(index)) {
                    day = Integer.parseInt(dateTextMatcher.group("day"));
                    month = MONTHS_PREFIX.indexOf(dateTextMatcher.group("month")) + 1;
                    year = dateTextMatcher.group("year") != null
                            ? Integer.parseInt(dateTextMatcher.group("year"))
                            : year;
                    index = dateTextMatcher.end();
                    setDate = true;
                    continue;
                }
                if (dateTextReversedMatcher.find(index)) {
                    day = Integer.parseInt(dateTextReversedMatcher.group("day"));
                    month = MONTHS_PREFIX.indexOf(dateTextReversedMatcher.group("month")) + 1;
                    year = dateTextReversedMatcher.group("year") != null
                            ? Integer.parseInt(dateTextReversedMatcher.group("year"))
                            : year;
                    index = dateTextReversedMatcher.end();
                    setDate = true;
                    continue;
                }
                if (dateNumberMatcher.find(index)) {
                    day = Integer.parseInt(dateNumberMatcher.group("day"));
                    month = Integer.parseInt(dateNumberMatcher.group("month"));
                    year = dateNumberMatcher.group("year") != null
                            ? Integer.parseInt(dateNumberMatcher.group("year"))
                            : year;
                    index = dateNumberMatcher.end();
                    setDate = true;
                    continue;
                }
            }
            throw new DateTimeParseException("Invalid date-time", input, index);
        }
        return LocalDateTime.of(year, month, day, hour, second, minute);
    }

    private static LocalDateTime parseRelativeTime(String input) throws DateTimeParseException {
        if (!input.endsWith(RELATIVE_TIME_TAG)) {
            throw new DateTimeParseException(
                    "No '" + RELATIVE_TIME_TAG + "' indicator",
                    input,
                    input.length() < RELATIVE_TIME_TAG.length() ? 0 : input.length() - RELATIVE_TIME_TAG.length());
        }
        input =  input.substring(0, input.length() - RELATIVE_TIME_TAG.length());

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

    /**
     * Parse the given string as a {@link LocalDateTime}, according to the class-defined format.
     *
     * @param input the string to be parsed
     * @return the parsed {@link LocalDateTime}
     * @throws DateTimeParseException if the string doesn't follow the class-defined format
     */
    public static LocalDateTime parseDateTime(String input) throws DateTimeParseException {
        if (input.equals("now")) {
            return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        } else if (input.endsWith("later")) {
            return parseRelativeTime(input);
        } else {
            return parseTimestamp(input);
        }
    }

    /**
     * Returns a string representation of the given {@link LocalDateTime}, according to the class-defined format.
     *
     * @param dateTime the date-time to format
     * @return the formatted date-time string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        StringBuilder res =  new StringBuilder();
        res.append(String.format("%02d:%02d:%02d ", dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond()));

        LocalDate today = LocalDate.now();
        LocalDate target = dateTime.toLocalDate();

        long daysDifference = today.until(dateTime, ChronoUnit.DAYS);
        if (daysDifference == 0) {
            res.append("today");
        } else if (daysDifference == -1) {
            res.append("yesterday");
        } else if (daysDifference == 1) {
            res.append("tomorrow");
        } else if (daysDifference > -7 && daysDifference <= 7) {
            res.append(daysDifference > 0 ? "next " : "last ");
            res.append(DAYS_OF_WEEK.get(target.getDayOfWeek().getValue() - 1));
        } else if (today.getYear() == target.getYear()) {
            res.append(dateTime.getDayOfMonth()).append(' ').append(MONTHS.get(dateTime.getMonthValue() - 1));
        } else {
            res.append(dateTime.getDayOfMonth()).append(' ').append(MONTHS.get(dateTime.getMonthValue() - 1)).append(' ').append(target.getYear());
        }
        return res.toString();
    }
}
