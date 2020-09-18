/*
 * 2020.09.18  - Created
 */
package com.munoor.quote.service;

import com.munoor.quote.DateQuotes;
import com.munoor.quote.Quote.QuoteType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Ilamah, Osho
 */
public abstract class AbstractQuoteService {

    protected static Map<String, String> SYMBOL_MAP;
    protected static boolean debug;

    /**
     * Maps user-space provided quote symbols to quote service-space symbols
     *
     * @param symbols a list of user-space quote symbols
     * @return an array of quote service-space symbols
     */
    protected static String[] mapSymbols(List<String> symbols) {
        List<String> mappedSymbols = new ArrayList<>();
        symbols.stream().forEach(s -> {
            final String symbol = SYMBOL_MAP.get(s);
            mappedSymbols.add(symbol == null ? s : symbol);
        });
        String[] mappedSymbolArray = new String[mappedSymbols.size()];
        mappedSymbols.toArray(mappedSymbolArray);
        return mappedSymbolArray;
    }

    /**
     * Creates a LocalDate which contains the same day/month/year information as the input Calendar
     *
     * @param c the input Calendar object
     * @return a LocalDate which contains the same day/month/year information as the input Calendar
     */
    protected static LocalDate getDate(final Calendar c) {
        return LocalDate.of(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Retrieves price quotes for the specified symbols between the stipulated dates.
     *
     * @param symbols the list of symbols for which the price quote is required
     * @param startDate the start date
     * @param endDate the end date
     * @param type the quote type
     * @return null if an error occurs or else a list of DateQuotes
     */
    public abstract List<DateQuotes> getPriceQuotes(List<String> symbols, LocalDate startDate, LocalDate endDate, QuoteType type);
}
