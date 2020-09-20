/*
 * 2020.09.18  - Created
 */
package com.munoor.quote.service;

import com.munoor.quote.DateQuotes;
import com.munoor.quote.Quote;
import com.munoor.quote.Quote.DateQuote;
import com.munoor.quote.Quote.QuoteType;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author Ilamah, Osho
 */
public class AlphaVantageQuoteService extends AbstractQuoteService {

    private static final String TIME_SERIES_MONTHLY_ADJUSTED_FXN = "TIME_SERIES_MONTHLY_ADJUSTED";
    private static final String TIME_SERIES_DAILY_ADJUSTED_FXN = "TIME_SERIES_DAILY_ADJUSTED";
    private static final String PRICE_CSV_URL_TEMPLATE = "https://www.alphavantage.co/query?function=%s&symbol=%s&apikey=%s&datatype=csv";
    private static final String UA = "User-Agent", ACCEPT_CHARSET = "Accept-Charset", UA_VALUE = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36", METHOD = "GET";
    private static final String ACCEPT_CHARSET_VALUE = java.nio.charset.StandardCharsets.UTF_8.name();
    private static final String API_ERROR_PREFIX = "{", SERVICE_NAME = "AlphaVantage Quote Service";

    public AlphaVantageQuoteService() {
        SYMBOL_MAP = new HashMap<>();
        // add mapping for instrument symbols which need special help to map to the AlphaVantage stock symbol
        SYMBOL_MAP.put("AIR", "AIR.PA");         //
        //
        // LSE shares:: https://github.com/prediqtiv/alpha-vantage-cookbook/blob/master/symbol-lists.md#united-kindom
        SYMBOL_MAP.put("BATS", "BATS.L");
        SYMBOL_MAP.put("BRSC", "BRSC.L");
        SYMBOL_MAP.put("DGE", "DGE.L");
        SYMBOL_MAP.put("FGT", "FGT.L");
        SYMBOL_MAP.put("GGRP", "GGRP.L");
        SYMBOL_MAP.put("HSBA", "HSBA.L");
        SYMBOL_MAP.put("IMB", "IMB.L");
        SYMBOL_MAP.put("LGEN", "LGEN.L");
        SYMBOL_MAP.put("NG.", "NG.L");
        SYMBOL_MAP.put("RDSB", "RDSB.L");
        SYMBOL_MAP.put("SGE", "SGE.L");
        SYMBOL_MAP.put("ULVR", "ULVR.L");
        SYMBOL_MAP.put("WLDS", "WLDS.L");

        SYMBOL_MAP.put("", ".L");

        // todo .. add more client to service space symbol mappings as appropriate!
    }

    @Override
    public List<DateQuotes> getPriceQuotes(List<String> symbols, LocalDate startDate, LocalDate endDate, QuoteType type, final List<String> failed) {
        final List<String> mappedSymbols = mapSymbols(symbols);
        final List<DateQuotes> dateQuotes = new ArrayList<>();
        switch (type) {
            case EOD:
                break;
            case EOM:
                break;
        }

        final String function = type.equals(QuoteType.EOD) ? TIME_SERIES_DAILY_ADJUSTED_FXN : TIME_SERIES_MONTHLY_ADJUSTED_FXN;

        HttpURLConnection con = null;
        //
        boolean hasError = false;
        final Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})|(\\d+\\.*\\d+)"); //tested at https://regex-testdrive.com/en/dotest with 2020-09-17,202.8500,205.5800,202.0000,205.2700,205.2700,7420687,0.0000,1.0000
        final Matcher matcher = pattern.matcher("");

        final List<DateQuote> records = new ArrayList<>();

        for (String symbol : mappedSymbols) {
            try {
                //API is limited to 5 reqs per minute, in theory. So 12secs between requests is required. We use a random range between 15 to 30 secs
                final long delay = (long) ((Math.random() * 15000) + 15000);
                System.out.println(String.format("Processing  ::: '%s'", symbol));
                Thread.sleep(delay);

                final String requestURL = String.format(PRICE_CSV_URL_TEMPLATE, function, symbol, config.getProperty("alphavantage.apikey")); // Another option is to use RestTemplates from Spring   
                URL myurl = new URL(requestURL);
                con = (HttpURLConnection) myurl.openConnection();
                con.setRequestProperty(UA, UA_VALUE); // Do as if you're using Chrome 41 on Windows 7.
                con.setRequestMethod(METHOD);
                con.setRequestProperty(ACCEPT_CHARSET, ACCEPT_CHARSET_VALUE);

                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //sc = new Scanner(con.getInputStream());
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                        final String firstLine = reader.readLine().trim();
                        if (firstLine.equals(API_ERROR_PREFIX)) {
                            //hasError = true;
                            //break;
                            System.out.println(String.format("Error downloading data for symbol::: '%s'", symbol));
                            failed.add(symbol);
                        } else {
                            String line;
                            //Scanner sc = null;
                            LocalDate lineDate = null;
                            while ((line = reader.readLine()) != null) {
                                int index = 0;
                                matcher.reset(line);
                                while (matcher.find()) {
                                    if (index == 0) {
                                        //read date
                                        lineDate = LocalDate.parse(matcher.group());
                                        // see if this date is relevant
                                        if (lineDate.isBefore(startDate) || lineDate.isAfter(endDate)) {
                                            break;
                                        }
                                    } else if (index == 5) {
                                        //adjusted close
                                        final DateQuote q = new DateQuote(lineDate, symbol, Double.parseDouble(matcher.group()));
                                        records.add(q);
                                        //System.out.println(String.format("%s :: %s :: %s", lineDate, symbol, q.getValue()));
                                        break;
                                    }
                                    index++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        hasError = true;
                        break;
                    }
                }
            } catch (Exception e) {
                hasError = true;
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }
        }
        if (hasError == false) {
            records.stream().collect(Collectors.groupingBy(DateQuote::getDate)).forEach((k, v) -> {
                final DateQuotes dq = new DateQuotes();
                final List<Quote> oneDateQuotes = v.stream().map(h -> new Quote(h.getSymbol(), h.getValue())).collect(Collectors.toList());
                final LocalDate date = v.get(0).getDate();
                dq.setDate(date);
                dq.setQuotes(oneDateQuotes);
                dateQuotes.add(dq);
            });
            dateQuotes.sort(Comparator.comparing(DateQuotes::getDate));

        }
        return dateQuotes;
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

}
