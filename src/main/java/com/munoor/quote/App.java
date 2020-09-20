/*
 * 2020.09.18  - Created
 */
package com.munoor.quote;

import com.munoor.quote.service.AbstractQuoteService;
import com.munoor.quote.service.AlphaVantageQuoteService;
import com.munoor.quote.service.YahooFinanceQuoteService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Ilamah, Osho
 */
public class App {

    public static void main(String[] args) {

        final LocalDate startDate = LocalDate.parse("2020-04-01");
        final LocalDate endDate = LocalDate.parse("2020-10-01");
        List<String> symbols = Arrays.asList("ABBV", "AIR", "AMD", "BA", "BAM", "BATS", "BLK", "BRSC", "CSCO", "DGE", "FB", "FGT", "FUQUIT", "GGRP", "GOOG", "HSBA", "IMB", "JNJ", "LGEN",
                "MA", "MSFT", "NG.", "RDSB", "SGE", "TSM", "ULVR", "V", "WLDS");

        final AbstractQuoteService[] qservice = {new AlphaVantageQuoteService(), new YahooFinanceQuoteService()};

        for (final AbstractQuoteService aqs : qservice) {

            List<String> failed = new ArrayList<>();

            System.out.println("\n\n\n\n===========================================================================");
            final List<DateQuotes> rs = aqs.getPriceQuotes(symbols, startDate, endDate, Quote.QuoteType.EOM, failed);
            System.out.println(String.format("\n\n --- Historic quotes from %s ---", aqs.getName()));
            rs.stream().forEach(dqs -> {
                System.out.println(String.format("%s --------------------------", dqs.getDate()));
                dqs.getQuotes().stream().forEach(q -> System.out.println(String.format("%s :: %.2f", q.getSymbol(), q.getValue())));
                System.out.println("*************************************");
            });

            if (failed.isEmpty() == false) {
                System.out.println("\n^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
                failed.stream().forEach(f -> {
                    System.out.println(String.format(" --- %s historic quotes not found by %s", f, aqs.getName()));
                });

            }
        }

    }
}
