/*
 * 2020.09.18  - Created
 */
package com.munoor.quote;

import java.time.LocalDate;

/**
 *
 * @author Ilamah, Osho
 */
public class Quote {

    private String symbol;
    private double value;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Quote() {
    }

    public Quote(String symbol, double value) {
        this.symbol = symbol;
        this.value = value;
    }

    public static class DateQuote extends Quote {

        private LocalDate date;

        public DateQuote(LocalDate date, String symbol, double value) {
            super(symbol, value);
            this.date = date;
        }

        public DateQuote() {
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    public static enum QuoteType {
        EOD, EOM
    }
}
