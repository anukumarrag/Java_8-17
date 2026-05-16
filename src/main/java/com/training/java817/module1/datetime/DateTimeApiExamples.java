package com.training.java817.module1.datetime;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * =============================================================================
 * MODULE 1 – DATE & TIME API (java.time, Java 8 – JEP 150)
 * =============================================================================
 *
 * THEORY
 * ------
 * The legacy java.util.Date and java.util.Calendar API suffered from:
 *   1. Mutability: Date objects are mutable – silently modified after creation.
 *   2. Poor API: months are 0-indexed (Jan=0), years offset by 1900.
 *   3. No timezone support: Date represents a point in time, not a local date.
 *   4. Thread-safety: SimpleDateFormat is NOT thread-safe.
 *
 * Java 8 (JSR-310, inspired by Joda-Time) introduced java.time:
 *   • Immutable & thread-safe by default.
 *   • Clear separation of concepts: date-only, time-only, date-time, with/without TZ.
 *   • Fluent, readable API.
 *   • ISO-8601 aligned out-of-the-box.
 *
 * KEY CLASSES
 * -----------
 *   LocalDate       – date without time or timezone (2024-03-15)
 *   LocalTime       – time without date or timezone (14:30:00)
 *   LocalDateTime   – date + time without timezone (2024-03-15T14:30:00)
 *   ZonedDateTime   – date + time + timezone (2024-03-15T14:30:00+01:00[Europe/Paris])
 *   Instant         – machine time (nanoseconds from Unix epoch) – ideal for timestamps
 *   Duration        – time-based amount (hours, minutes, seconds)
 *   Period          – date-based amount (years, months, days)
 *   DateTimeFormatter – formatting and parsing (thread-safe)
 */
public class DateTimeApiExamples {

    // =========================================================================
    // BEFORE – Legacy java.util.Date (Java 7 style)
    // =========================================================================

    /** Parse and format a date – messy, non-thread-safe, error-prone. */
    @SuppressWarnings("deprecation")
    public String formatDate_Before(int year, int month, int day) {
        // month is 0-indexed in legacy API: January = 0
        java.util.Date date = new java.util.Date(year - 1900, month - 1, day);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MMM-yyyy");
        return sdf.format(date);
    }

    // =========================================================================
    // AFTER – java.time (Java 8+)
    // =========================================================================

    // ---- LocalDate: date only -----------------------------------------------

    /** Create and format a LocalDate. */
    public String formatDate_After(int year, int month, int day) {
        LocalDate date = LocalDate.of(year, month, day);   // month IS 1-indexed
        return date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
    }

    /** Calculate the number of days until review. */
    public long daysUntilReview(LocalDate hireDate, LocalDate reviewDate) {
        return ChronoUnit.DAYS.between(hireDate, reviewDate);
    }

    /** Business rule: review date is T+90 (ninety days after hire date). */
    public LocalDate standardReviewDate(LocalDate hireDate) {
        return hireDate.plusDays(90);
    }

    /** Is the trade date a weekday? */
    public boolean isWeekday(LocalDate date) {
        return switch (date.getDayOfWeek()) {
            case SATURDAY, SUNDAY -> false;
            default -> true;
        };
    }

    // ---- LocalDateTime: date + time, no timezone ----------------------------

    /** Record the exact time an employee was hired. */
    public LocalDateTime employeeHiredAt(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute);
    }

    /** Check if a trade was created within business hours (08:00 – 17:00). */
    public boolean isWithinBusinessHours(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        return hour >= 8 && hour < 17;
    }

    // ---- ZonedDateTime: date + time + timezone ------------------------------

    /** Convert a London trade time to New York time. */
    public ZonedDateTime londonToNewYork(LocalDateTime londonTime) {
        ZonedDateTime london = londonTime.atZone(ZoneId.of("Europe/London"));
        return london.withZoneSameInstant(ZoneId.of("America/New_York"));
    }

    /** Build a ZonedDateTime for the event timestamp in UTC. */
    public ZonedDateTime eventTimestampUtc(int year, int month, int day,
                                           int hour, int minute, int second) {
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0,
                ZoneId.of("UTC"));
    }

    // ---- Instant: machine timestamps ----------------------------------------

    /** Capture when a trade was submitted (for audit logs). */
    public Instant captureTimestamp() {
        return Instant.now();
    }

    /** Measure how long a trade processing step took (in milliseconds). */
    public long elapsedMillis(Instant start, Instant end) {
        return Duration.between(start, end).toMillis();
    }

    // ---- Duration: time-based amounts ----------------------------------------

    /** How long has the employee been in ONBOARDING status? */
    public Duration onboardingDuration(LocalDateTime onboardingSince, LocalDateTime now) {
        return Duration.between(onboardingSince, now);
    }

    /** Overdue: onboarding must complete within 30 days. */
    public boolean isOnboardingOverdue(LocalDateTime onboardingSince, LocalDateTime now) {
        Duration elapsed = Duration.between(onboardingSince, now);
        Duration sla     = Duration.ofDays(30);
        return elapsed.compareTo(sla) > 0;
    }

    // ---- Period: date-based amounts ------------------------------------------

    /** How many years, months, days since the employee was hired? */
    public Period employeeTenure(LocalDate hireDate, LocalDate today) {
        return Period.between(hireDate, today);
    }

    /** Format the period nicely for display. */
    public String formatPeriod(Period period) {
        return period.getYears() + "y " + period.getMonths() + "m " + period.getDays() + "d";
    }

    // ---- DateTimeFormatter: custom patterns ----------------------------------

    /** Parse an ISO-8601 string to a LocalDateTime. */
    public LocalDateTime parseIso(String isoString) {
        return LocalDateTime.parse(isoString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /** Format a ZonedDateTime as a compact event timestamp string. */
    public String formatEventTimestamp(ZonedDateTime zdt) {
        return zdt.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX"));
    }

    // ---- Immutability in action ----------------------------------------------

    /**
     * LocalDate is IMMUTABLE. plus/minus operations return NEW instances.
     * The original date is NEVER modified.
     */
    public List<LocalDate> generateReviewDates(LocalDate start, int count) {
        LocalDate[] dates = new LocalDate[count];
        LocalDate   current = start;
        for (int i = 0; i < count; i++) {
            dates[i] = current;
            current = current.plusDays(1);   // returns a new instance
        }
        return List.of(dates);
    }

    // demo main
    public static void main(String[] args) {
        DateTimeApiExamples ex = new DateTimeApiExamples();

        LocalDate today = LocalDate.of(2024, 3, 15);
        System.out.println("Formatted      : " + ex.formatDate_After(2024, 3, 15));
        System.out.println("Review T+90    : " + ex.standardReviewDate(today));
        System.out.println("Is weekday     : " + ex.isWeekday(today));

        LocalDateTime noon = LocalDateTime.of(2024, 3, 15, 12, 0);
        System.out.println("In biz hours   : " + ex.isWithinBusinessHours(noon));
        System.out.println("London->NY     : " + ex.londonToNewYork(noon));

        Instant t1 = Instant.now();
        Instant t2 = t1.plusMillis(250);
        System.out.println("Elapsed ms     : " + ex.elapsedMillis(t1, t2));

        Period p = ex.employeeTenure(LocalDate.of(2020, 1, 15), today);
        System.out.println("Employee tenure: " + ex.formatPeriod(p));
    }
}
