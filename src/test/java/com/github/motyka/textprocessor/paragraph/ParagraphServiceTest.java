package com.github.motyka.textprocessor.paragraph;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ParagraphServiceTest {

	@InjectMocks
	private ParagraphService service;

	@Test
	@DisplayName("single paragraph")
	public void singleParagraph() {
		List<Paragraph> paragraphs = service.splitAndSearch("wholeText", "search", 1200, 800);

		assertEquals(1, paragraphs.size());
		assertEquals(new Paragraph("wholeText", 0, 9, false), paragraphs.get(0));
	}

	@Test
	@DisplayName("single paragraph with whitespaces")
	public void paragraphWhitespaces() {
		List<Paragraph> paragraphs = service.splitAndSearch(" \t\n wholeText \t\n ", "search", 1200, 800);

		assertEquals(1, paragraphs.size());
		assertEquals(new Paragraph("wholeText", 4, 9, false), paragraphs.get(0));
	}

	@Test
	@DisplayName("single paragraph with whitespaces and trailing punctuation")
	public void paragraphWhitespacesPunctuation() {
		List<Paragraph> paragraphs = service.splitAndSearch(" \t\n ,!paragraph?, \t\n ", "search", 1200, 800);

		assertEquals(1, paragraphs.size());
		assertEquals(new Paragraph(",!paragraph?,", 4, 13, false), paragraphs.get(0));
	}

	@Test
	@DisplayName("paragraphs separated by new lines")
	public void newLines() {
		List<Paragraph> paragraphs = service.splitAndSearch("paragraph1\n\nparagraph2", "search", 1200, 800);

		assertEquals(2, paragraphs.size());
		assertEquals(new Paragraph("paragraph1", 0, 10, false), paragraphs.get(0));
		assertEquals(new Paragraph("paragraph2", 12, 10, false), paragraphs.get(1));
	}

	@Test
	@DisplayName("paragraphs separated by new lines separated by whitespaces")
	public void newLinesWhitespaces() {
		List<Paragraph> paragraphs = service.splitAndSearch(" paragraph1 \n \t \n \t \n paragraph2 ", "search", 1200, 800);

		assertEquals(2, paragraphs.size());
		assertEquals(new Paragraph("paragraph1", 1, 10, false), paragraphs.get(0));
		assertEquals(new Paragraph("paragraph2", 22, 10, false), paragraphs.get(1));
	}

	@Test
	@DisplayName("paragraphs separated by multiple new lines separated by whitespaces and punctuations")
	public void newLinesWhitespacesPunctuation() {
		List<Paragraph> paragraphs = service.splitAndSearch(" paragraph1 \n!\t,\n,\t?\n paragraph2 ", "search", 1200, 800);

		assertEquals(2, paragraphs.size());
		assertEquals(new Paragraph("paragraph1", 1, 10, false), paragraphs.get(0));
		assertEquals(new Paragraph("paragraph2", 22, 10, false), paragraphs.get(1));
	}

	@Test
	@DisplayName("paragraphs separated by multiple new lines with trailing punctuations")
	public void newLinesTrailingPunctuation() {
		List<Paragraph> paragraphs = service.splitAndSearch(" !paragraph1. \n\n ,.?paragraph2-, ", "search", 1200, 800);

		assertEquals(2, paragraphs.size());
		assertEquals(new Paragraph("!paragraph1.", 1, 12, false), paragraphs.get(0));
		assertEquals(new Paragraph(",.?paragraph2-,", 17, 15, false), paragraphs.get(1));
	}

	@Test
	@DisplayName("high limits, no separation needed")
	public void noSeparation() {
		List<Paragraph> paragraphs = service.splitAndSearch(" !paragraph1. \n. ,.?paragraph2-, ", "search", 1200, 800);

		assertEquals(1, paragraphs.size());
		assertEquals(new Paragraph("!paragraph1. \n. ,.?paragraph2-,", 1, 31, false), paragraphs.get(0));
	}

	@Test
	@DisplayName("paragraphs separated by single new lines")
	public void singleNewLine() {
		List<Paragraph> paragraphs = service.splitAndSearch("123?567890\n2345\n78,01.3456\n890", "search", 12, 8);

		assertEquals(4, paragraphs.size());
		assertEquals(new Paragraph("123?567890", 0, 10, false), paragraphs.get(0));
		assertEquals(new Paragraph("2345", 11, 4, false), paragraphs.get(1));
		assertEquals(new Paragraph("78,01.3456", 16, 10, false), paragraphs.get(2));
		assertEquals(new Paragraph("890", 27, 3, false), paragraphs.get(3));
	}

	@Test
	@DisplayName("paragraphs separated by dots")
	public void dots() {
		List<Paragraph> paragraphs = service.splitAndSearch("123.567890.2345.7890123456.890", "search", 12, 8);

		assertEquals(4, paragraphs.size());
		assertEquals(new Paragraph("123.567890.", 0, 11, false), paragraphs.get(0));
		assertEquals(new Paragraph("2345.", 11, 5, false), paragraphs.get(1));
		assertEquals(new Paragraph("7890123456.", 16, 11, false), paragraphs.get(2));
		assertEquals(new Paragraph("890", 27, 3, false), paragraphs.get(3));
	}

	@Test
	@DisplayName("paragraphs separated by dots with trailing punctuations")
	public void dotsPunctuations() {
		List<Paragraph> paragraphs = service.splitAndSearch(" word,,.,, \t ,.?word2,., ,, ,.?word3,.", "search", 12, 8);

		assertEquals(3, paragraphs.size());
		assertEquals(new Paragraph("word,,.", 1, 7, false), paragraphs.get(0));
		assertEquals(new Paragraph(",.?word2,.", 13, 10, false), paragraphs.get(1));
		assertEquals(new Paragraph(",.?word3,.", 28, 10, false), paragraphs.get(2));
	}

	@Test
	@DisplayName("paragraphs separated by punctuations")
	public void punctuations() {
		List<Paragraph> paragraphs = service.splitAndSearch("123?567890,2345!7890123456_890", "search", 12, 8);

		assertEquals(4, paragraphs.size());
		assertEquals(new Paragraph("123?567890,", 0, 11, false), paragraphs.get(0));
		assertEquals(new Paragraph("2345!", 11, 5, false), paragraphs.get(1));
		assertEquals(new Paragraph("7890123456_", 16, 11, false), paragraphs.get(2));
		assertEquals(new Paragraph("890", 27, 3, false), paragraphs.get(3));
	}

	@Test
	@DisplayName("paragraphs separated by spaces")
	public void spaces() {
		List<Paragraph> paragraphs = service.splitAndSearch("1234567890 2345 7890123456 890", "search", 12, 8);

		assertEquals(4, paragraphs.size());
		assertEquals(new Paragraph("1234567890", 0, 10, false), paragraphs.get(0));
		assertEquals(new Paragraph("2345", 11, 4, false), paragraphs.get(1));
		assertEquals(new Paragraph("7890123456", 16, 10, false), paragraphs.get(2));
		assertEquals(new Paragraph("890", 27, 3, false), paragraphs.get(3));
	}

	@Test
	@DisplayName("splitting at the max length")
	public void maxLength() {
		List<Paragraph> paragraphs = service.splitAndSearch("123456789012345678901234567890", "search", 12, 8);

		assertEquals(3, paragraphs.size());
		assertEquals(new Paragraph("123456789012", 0, 12, false), paragraphs.get(0));
		assertEquals(new Paragraph("345678901234", 12, 12, false), paragraphs.get(1));
		assertEquals(new Paragraph("567890", 24, 6, false), paragraphs.get(2));
	}

	@Test
	@DisplayName("search in paragraph")
	public void search() {
		List<Paragraph> paragraphs = service.splitAndSearch("This is some test text\n\n" +
				"with a search term inside some of them\n\n" +
				"to test the search functionality.", "search", 1200, 800);

		assertEquals(3, paragraphs.size());
		assertEquals(new Paragraph("This is some test text", 0, 22, false), paragraphs.get(0));
		assertEquals(new Paragraph("with a search term inside some of them", 24, 38, true), paragraphs.get(1));
		assertEquals(new Paragraph("to test the search functionality.", 64, 33, true), paragraphs.get(2));
	}

	@Test
	@DisplayName("empty text")
	public void empty() {
		List<Paragraph> paragraphs = service.splitAndSearch("", "", 1200, 800);

		assertEquals(0, paragraphs.size());
	}

	@Test
	@DisplayName("no paragraphs")
	public void noParagraphs() {
		List<Paragraph> paragraphs = service.splitAndSearch(". \t,?\n\n. ,\n!!", "", 1200, 800);

		assertEquals(0, paragraphs.size());
	}

	@Test
	@DisplayName("empty search term")
	public void emptySearchTerm() {
		List<Paragraph> paragraphs = service.splitAndSearch("wholeText", "", 1200, 800);

		assertEquals(1, paragraphs.size());
		assertEquals(new Paragraph("wholeText", 0, 9, false), paragraphs.get(0));
	}

	@Test
	@DisplayName("negative limit")
	public void negativeLimit() {
		assertThrows(IllegalArgumentException.class, () -> service.splitAndSearch("", "", -12, 8));
	}

	@Test
	@DisplayName("secondary limit greater than the main limit")
	public void secondaryGreaterThenMain() {
		assertThrows(IllegalArgumentException.class, () -> service.splitAndSearch("", "", 800, 1200));
	}

	@Test
	@DisplayName("null text")
	public void nullText() {
		assertThrows(NullPointerException.class, () -> service.splitAndSearch(null, "", 8, 12));
	}
}
