package org.cs2103t.marquee.core.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.List;

import org.junit.jupiter.api.Test;

class CsvTableTest {
    private static final Path readTestFile = Path.of("src/test/resources/test_read.csv");
    private static final Path writeTestFile = Path.of("src/test/resources/test_write.csv");
    @Test
    void readFile() throws IOException {
        try {
            CsvTable testTable = CsvTable.readFile(readTestFile);
            assertEquals(List.of("column1", "column \"2\"", ""), testTable.getColumns());
            assertEquals(List.of(
                    testTable.createRecord("1", "2", "3"),
                    testTable.createRecord("\"A\"", "B,B", "C\r\nC")
            ), testTable.getValues());
        } catch (ParseException e) {
            throw new AssertionError(e);
        }
    }

    @Test
    void writeFile() throws IOException {
        Files.write(writeTestFile, List.of(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        CsvTable testTable = new CsvTable("column#1", "Column,2", "\r\"\r\n\"\n");
        testTable.add(
                testTable.createRecord("field#1", "field\"2\"", ""),
                testTable.createRecord("", "", ""),
                testTable.createRecord("\r\n", "", "")
        );
        CsvTable.writeFile(writeTestFile, testTable);
        assertTrue(
                Files.readString(writeTestFile).startsWith(
                        "column#1,\"Column,2\",\"\r\"\"\r\n\"\"\n\"\r\n"
                        + "field#1,\"field\"\"2\"\"\",\r\n"
                        + ",,\r\n"
                        + "\"\r\n\",,"
                )
        );
    }
}