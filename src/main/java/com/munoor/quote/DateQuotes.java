/*
 * 2020.09.18  - Created
 */
package com.munoor.quote;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author Ilamah, Osho
 */
public class DateQuotes {
    private List<Quote> quotes;
    private LocalDate date;

    public List<Quote> getQuotes() {
        return quotes;
    }

    public void setQuotes(List<Quote> quotes) {
        this.quotes = quotes;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    
}
