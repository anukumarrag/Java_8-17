package com.training.java817.module1.datetime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Module 1 – Date & Time API")
class DateTimeApiExamplesTest {

    private DateTimeApiExamples ex;

    @BeforeEach
    void setUp() { ex = new DateTimeApiExamples(); }

    // ---- LocalDate ----------------------------------------------------------

    @Test
    @DisplayName("formatDate_After: formats date with correct pattern")
    void formatDate_producesCorrectString() {
        assertEquals("15-Mar-2024", ex.formatDate_After(2024, 3, 15));
    }

    @Test
    @DisplayName("standardSettlementDate: returns T+2")
    void standardSettlementDate_returnsTPlus2() {
        LocalDate trade      = LocalDate.of(2024, 3, 15);
        LocalDate settlement = ex.standardSettlementDate(trade);
        assertEquals(trade.plusDays(2), settlement);
    }

    @Test
    @DisplayName("daysUntilSettlement: computes correct day count")
    void daysUntilSettlement_computesCorrectly() {
        LocalDate tradeDate      = LocalDate.of(2024, 3, 15);
        LocalDate settlementDate = LocalDate.of(2024, 3, 17);
        assertEquals(2L, ex.daysUntilSettlement(tradeDate, settlementDate));
    }

    @Test
    @DisplayName("isWeekday: Monday–Friday returns true; Saturday–Sunday returns false")
    void isWeekday_correctlyClassifiesDays() {
        assertTrue(ex.isWeekday(LocalDate.of(2024, 3, 18)));  // Monday
        assertTrue(ex.isWeekday(LocalDate.of(2024, 3, 22)));  // Friday
        assertFalse(ex.isWeekday(LocalDate.of(2024, 3, 23))); // Saturday
        assertFalse(ex.isWeekday(LocalDate.of(2024, 3, 24))); // Sunday
    }

    // ---- LocalDateTime ------------------------------------------------------

    @Test
    @DisplayName("isWithinBusinessHours: 12:00 is within, 07:00 and 17:00 are not")
    void isWithinBusinessHours_correctBoundary() {
        assertTrue(ex.isWithinBusinessHours(
                ex.tradeCreatedAt(2024, 3, 15, 12, 0)));
        assertFalse(ex.isWithinBusinessHours(
                ex.tradeCreatedAt(2024, 3, 15, 7, 59)));
        assertFalse(ex.isWithinBusinessHours(
                ex.tradeCreatedAt(2024, 3, 15, 17, 0)));
    }

    // ---- ZonedDateTime ------------------------------------------------------

    @Test
    @DisplayName("londonToNewYork: converts timezone correctly (NY is typically UTC-5)")
    void londonToNewYork_convertsTimezone() {
        LocalDateTime london = LocalDateTime.of(2024, 3, 15, 15, 0);   // 3 PM London
        ZonedDateTime ny     = ex.londonToNewYork(london);
        assertEquals(ZoneId.of("America/New_York"), ny.getZone());
        // NY offset depends on DST; just verify it is earlier than London time
        assertTrue(ny.getHour() < 15 || ny.toInstant().equals(
                london.atZone(ZoneId.of("Europe/London")).toInstant()),
                "NY time should differ from London time");
    }

    @Test
    @DisplayName("tradeTimestampUtc: creates UTC ZonedDateTime correctly")
    void tradeTimestampUtc_setsCorrectZone() {
        ZonedDateTime ts = ex.tradeTimestampUtc(2024, 3, 15, 9, 30, 0);
        assertEquals(ZoneId.of("UTC"), ts.getZone());
        assertEquals(2024, ts.getYear());
        assertEquals(9,    ts.getHour());
    }

    // ---- Duration -----------------------------------------------------------

    @Test
    @DisplayName("elapsedMillis: computes duration between two Instants")
    void elapsedMillis_computesCorrectly() {
        Instant t1 = Instant.ofEpochMilli(1000L);
        Instant t2 = Instant.ofEpochMilli(1500L);
        assertEquals(500L, ex.elapsedMillis(t1, t2));
    }

    @Test
    @DisplayName("isSlaBreach: 5 hours pending breaches 4-hour SLA")
    void isSlaBreach_5HoursBreachesSla() {
        LocalDateTime start = LocalDateTime.of(2024, 3, 15, 9, 0);
        LocalDateTime now   = LocalDateTime.of(2024, 3, 15, 14, 1);
        assertTrue(ex.isSlaBreach(start, now));
    }

    @Test
    @DisplayName("isSlaBreach: 3 hours pending does not breach SLA")
    void isSlaBreach_3HoursDoesNotBreach() {
        LocalDateTime start = LocalDateTime.of(2024, 3, 15, 9, 0);
        LocalDateTime now   = LocalDateTime.of(2024, 3, 15, 12, 0);
        assertFalse(ex.isSlaBreach(start, now));
    }

    // ---- Period -------------------------------------------------------------

    @Test
    @DisplayName("relationshipAge: computes years, months, days correctly")
    void relationshipAge_computesCorrectly() {
        LocalDate onboarded = LocalDate.of(2020, 1, 15);
        LocalDate today     = LocalDate.of(2024, 3, 15);
        Period p = ex.relationshipAge(onboarded, today);
        assertEquals(4, p.getYears());
        assertEquals(2, p.getMonths());
        assertEquals(0, p.getDays());
    }

    @Test
    @DisplayName("formatPeriod: formats period as 'Xy Xm Xd'")
    void formatPeriod_producesCorrectString() {
        Period p = Period.of(4, 2, 0);
        assertEquals("4y 2m 0d", ex.formatPeriod(p));
    }

    // ---- Immutability -------------------------------------------------------

    @Test
    @DisplayName("generateSettlementDates: original start date is unchanged")
    void generateSettlementDates_doesNotMutateStart() {
        LocalDate start = LocalDate.of(2024, 3, 15);
        ex.generateSettlementDates(start, 5);
        assertEquals(LocalDate.of(2024, 3, 15), start,
                "Original date must not be mutated – LocalDate is immutable");
    }

    @Test
    @DisplayName("generateSettlementDates: produces correct sequence")
    void generateSettlementDates_producesCorrectSequence() {
        LocalDate start = LocalDate.of(2024, 3, 15);
        var dates = ex.generateSettlementDates(start, 3);
        assertEquals(3, dates.size());
        assertEquals(LocalDate.of(2024, 3, 15), dates.get(0));
        assertEquals(LocalDate.of(2024, 3, 16), dates.get(1));
        assertEquals(LocalDate.of(2024, 3, 17), dates.get(2));
    }
}
