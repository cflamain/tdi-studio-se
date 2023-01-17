package com.talend.csv;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.talend.csv.CSVWriter.QuoteStatus;

class CSVWriterTest {

    @Test
    void writeCustom() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {

            writer.setSeparator(';').setEscapeChar('@').setQuoteChar('"').setLineEnd("\r\n").setQuoteStatus(QuoteStatus.AUTO);

            writer.writeAll(Arrays.asList(new String[] { "1", "Hello", "\"Mad\" World" },
                    new String[] { "2", "Good by", "world @ by" }));
        }
        Assertions.assertEquals("1;Hello;\"@\"Mad@\" World\"\r\n2;Good by;world @@ by\r\n",
                result.toString());

    }

    @Test
    void writeDefault() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {
            writer.setLineEnd("\n");
            writer.writeAll(Arrays.asList(new String[] { "1", "Hello", "\"Mad\" World" },
                    new String[] { "2", "Good by", "world \\ by" }));
        }
        Assertions.assertEquals("1,Hello,\"\"\"Mad\"\" World\"\n2,Good by,world \\ by\n",
                result.toString());

    }

    @Test
    void writeForceQuote() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {
            writer.setLineEnd("\n");
            writer.setQuoteStatus(QuoteStatus.FORCE);
            writer.writeAll(Arrays.asList(new String[] { "1", "Hello", "\"Mad\" World" },
                    new String[] { "2", "Good by", "world \\ by" }));
        }
        Assertions.assertEquals("\"1\",\"Hello\",\"\"\"Mad\"\" World\"\n\"2\",\"Good by\",\"world \\ by\"\n",
                result.toString());

    }

    @Test
    void writeAutoQuote() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {
            writer.setLineEnd("\n");
            writer.setQuoteStatus(QuoteStatus.AUTO);
            writer.writeAll(Arrays.asList(new String[] { "1", "Hello", "\"Mad\" World" },
                    new String[] { "2", "Good by", "world \\ by" }));
        }
        Assertions.assertEquals("1,Hello,\"\"\"Mad\"\" World\"\n2,Good by,world \\ by\n",
                result.toString());
    }

    @Test
    void writeNoQuote() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {
            writer.setLineEnd("\n");
            writer.setQuoteStatus(QuoteStatus.NO);
            writer.writeAll(Arrays.asList(new String[] { "1", "Hello", "\"Mad\" World" },
                    new String[] { "2", "Good by", "world \\ by" }));
        }
        Assertions.assertEquals("1,Hello,\"Mad\" World\n2,Good by,world \\ by\n",
                result.toString());
    }

    @Test
    void writeEscapeByBackSlash() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {
            writer.setLineEnd("\n");
            writer.setEscapeChar('\\');
            writer.writeAll(Arrays.asList(new String[] { "1", "Hello", "\"Mad\" World" },
                    new String[] { "2", "Good by", "world \\ by" }));
        }
        Assertions.assertEquals("1,Hello,\"\\\"Mad\\\" World\"\n2,Good by,world \\\\ by\n",
                result.toString());
    }

    @Test
    void writeFirstColumnEmptyStringCase() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {
            writer.setLineEnd("\n");
            writer.writeAll(Arrays.asList(new String[] { "", "Hello", "" },
                    new String[] { "", "Good by", "world \\ by" }));
        }
        Assertions.assertEquals("\"\",Hello,\n\"\",Good by,world \\ by\n",
                result.toString());
    }

    @Test
    void writeWhiteSpaceAndTabProtection() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {
            writer.setLineEnd("\n");
            writer.writeAll(Arrays.asList(new String[] { "1", "Hello ", "" },
                    new String[] { "2", " Good by", "world \\ by" }));
        }
        Assertions.assertEquals("1,\"Hello \",\n2,\" Good by\",world \\ by\n",
                result.toString());
    }

    @Test
    void writeNextEnhance() throws IOException {
        final StringWriter result = new StringWriter();
        try (final CSVWriter writer = new CSVWriter(result)) {
            writer.setLineEnd("\n");
            writer.writeNextEnhance((new String[] { "1", null, "World" }), "NULL");
            writer.writeNextEnhance((new String[] { "", null, "World" }), "NULL");
            writer.setQuoteStatus(QuoteStatus.FORCE);
            writer.writeNextEnhance((new String[] { "3", null, "World" }), "NULL");
        }
        Assertions.assertEquals("1,NULL,World\n\"\",NULL,World\n\"3\",NULL,\"World\"\n",
                result.toString());
    }
}