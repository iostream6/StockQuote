/*
 * 2020.09.18  - Created
 */
package com.munoor.quote.service;

import com.munoor.quote.DateQuotes;
import com.munoor.quote.Quote;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

/**
 *
 * @author Ilamah, Osho
 */
public class YahooFinanceQuoteService extends AbstractQuoteService {

    private static final String SERVICE_NAME = "Yahoo! Finance Quote Service";

    public YahooFinanceQuoteService() {
        SYMBOL_MAP = new HashMap<>();
        // add mapping for instrument symbols which need special help to map to the Yahoo! Finance stock symbol
        SYMBOL_MAP.put("AIR", "AIR.PA");
        // todo .. add more client to service space symbol mappings as appropriate!
    }

    @Override
    public List<DateQuotes> getPriceQuotes(List<String> symbols, LocalDate startDate, LocalDate endDate, Quote.QuoteType type, final List<String> failed) {
        final List<String> mappedSymbols = mapSymbols(symbols);
        String[] mappedSymbolArray = new String[mappedSymbols.size()];
        mappedSymbols.toArray(mappedSymbolArray);
        
        final List<DateQuotes> dateQuotes = new ArrayList<>();

        final Calendar from = Calendar.getInstance();
        from.set(startDate.getYear(), startDate.getMonthValue() - 1, startDate.getDayOfMonth());
        final Calendar to = Calendar.getInstance();
        to.set(endDate.getYear(), endDate.getMonthValue() - 1, endDate.getDayOfMonth());

        Interval interval = type.equals(Quote.QuoteType.EOD) ? Interval.DAILY : Interval.MONTHLY;

        try {
            final Map<String, Stock> results = YahooFinance.get(mappedSymbolArray, from, to, interval);

            if (results.keySet().size() != symbols.size()) {
                System.out.println("****************  Could not retrieve data for certain symbols\n\n\n");
                symbols.stream().filter(s -> results.keySet().contains(s) == false).forEach(ss -> {
                    System.out.println(String.format("||| -> %s \n", ss));
                    failed.add(ss);
                });
            }
            if (debug) {
                results.keySet().stream().forEach(key -> {
                    System.out.println("\n\n===================================================");
                    Stock stock = results.get(key);
                    stock.print();
                });
            }
            final List<HistoricalQuote> hq = new ArrayList<>();

            results.forEach((key, value) -> {
                try {
                    hq.addAll(value.getHistory());
                } catch (IOException ex) {
                    Logger.getLogger(YahooFinanceQuoteService.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            hq.stream().collect(Collectors.groupingBy(HistoricalQuote::getDate)).forEach((k, v) -> {
                //k is a Calendar, v is a List of HistoricQuotes
                final DateQuotes dq = new DateQuotes();
                final List<Quote> oneDateQuotes = v.stream().map(h -> new Quote(h.getSymbol(), h.getAdjClose().doubleValue())).collect(Collectors.toList());
                final LocalDate date = YahooFinanceQuoteService.getDate(v.get(0).getDate());
                dq.setDate(date);
                dq.setQuotes(oneDateQuotes);
                dateQuotes.add(dq);
            });

            dateQuotes.sort(Comparator.comparing(DateQuotes::getDate));

        } catch (IOException ex) {
            Logger.getLogger(YahooFinanceQuoteService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return dateQuotes;
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }
}
