package com.github.motyka.textprocessor.paragraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParagraphService {

	public List<Paragraph> splitAndSearch2(String text, String searchTerm, int mainLimit, int secondaryLimit) {
		boolean paragraphStarted = false;
		int startIndex = 0;
		int length = 0;

		for(int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if(!paragraphStarted && Character.isWhitespace(ch)) continue;

			if(!paragraphStarted) {
				paragraphStarted = true;
				startIndex = i;
			}


		}
		return null;
	}

	private final Logger logger = LoggerFactory.getLogger(ParagraphService.class);

	private static final Pattern PUNCTUATION = Pattern.compile("\\p{Punct}");
	private static final Pattern LAST_PUNCTUATION = Pattern.compile(".*\\p{Punct}");
	// there can't be alphanumeric characters between new line characters
	private static final Pattern NEW_LINES = Pattern.compile("\n[^\\p{Alnum}]*\n");
	// there needs to be a paragraph (alphanumeric characters) before the dot
	private static final Pattern DOT = Pattern.compile("\\p{Alnum}.*?\\.");
	// only keep punctuation that adhere to the paragraph
	private static final Pattern TRIM = Pattern.compile("\\p{Punct}*\\p{Alnum}");
	private static final Pattern TRIM_END = Pattern.compile(".*\\p{Alnum}\\p{Punct}*");

	public List<Paragraph> splitAndSearch(String text, String searchTerm, int mainLimit, int secondaryLimit) {
		StringBuilder sb = new StringBuilder(text);
		List<Paragraph> paragraphs = new ArrayList<>();

		trimEnd(sb);
		int offset = 0;
		offset += trimStart(sb);
		while(sb.length() > 0) {
			// always split at double new line before 1200 character (can be separated by non alphanumeric characters)
			Matcher matcher = NEW_LINES.matcher(sb);
			if(matcher.find() && isInRange(matcher.start(), mainLimit)) {
				logger.info("double new line before *mainLimit*");
				offset += addParagraph(paragraphs, sb, offset, matcher.start());
				continue;
			}

			// no need to split anymore if too small
			if(sb.length() < mainLimit) {
				addParagraph(paragraphs, sb, offset, sb.length());
				break;
			}

			CharSequence mainSubstring = sb.subSequence(0, Math.min(sb.length(), mainLimit));
			CharSequence secondarySubstring = sb.subSequence(0, Math.min(sb.length(), secondaryLimit));

			// SINGLE NEW LINE

			// split at first new line between 800 - 1200 character
			int index = sb.indexOf("\n", secondaryLimit);
			if(isInRange(index, mainLimit)) {
				logger.info("first new line between *secondaryLimit* - *mainLimit*");
				offset += addParagraph(paragraphs, sb, offset, index);
				continue;
			}
			// split at last new line before 800 character
			index = sb.lastIndexOf("\n", secondaryLimit);
			if(isInRange(index, secondaryLimit)) {
				logger.info("first new line before *secondaryLimit*");
				offset += addParagraph(paragraphs, sb, offset, index);
				continue;
			}

			// DOT

			// split at first dot between 800 - 1200 character
			matcher = DOT.matcher(mainSubstring);
//			boolean find = matcher.find(Math.min(sb.length(), secondaryLimit));
			index = matcher.results().filter(m -> m.end() >= secondaryLimit).map(MatchResult::end).findFirst().orElse(-1);
			if(isInRange(index, mainLimit)) {
				logger.info("first dot between *secondaryLimit* - *mainLimit*");
				offset += addParagraph(paragraphs, sb, offset, index);
				continue;
			}
			// split at last dot before 800 character
			matcher = DOT.matcher(secondarySubstring);
			index = matcher.results().reduce((f, s) -> s).map(MatchResult::end).orElse(-1);
			if(isInRange(index, secondaryLimit)) {
				logger.info("last dot before *secondaryLimit*");
				offset += addParagraph(paragraphs, sb, offset, index);
				continue;
			}

			// PUNCTUATION

			// split at first punctuation between 800 - 1200 character
			matcher = PUNCTUATION.matcher(mainSubstring);
//			if(matcher.find(Math.min(sb.length(), secondaryLimit)) && isInRange(matcher.start(), mainLimit)) {
			index = matcher.results().filter(m -> m.end() >= secondaryLimit).map(MatchResult::end).findFirst().orElse(-1);
			if(isInRange(index, mainLimit)) {
				logger.info("first punctuation between *secondaryLimit* - *mainLimit*");
				offset += addParagraph(paragraphs, sb, offset, index);
				continue;
			}
			// split at last punctuation before 800 character
			matcher = PUNCTUATION.matcher(secondarySubstring);
			index = matcher.results().reduce((f, s) -> s).map(MatchResult::end).orElse(-1);
			if(isInRange(index, secondaryLimit)) {
				logger.info("last punctuation before *secondaryLimit*");
				offset += addParagraph(paragraphs, sb, offset, index);
				continue;
			}

			// SPACE

			// split at first space between 800 - 1200 character
			index = sb.indexOf(" ", secondaryLimit);
			if(isInRange(index, mainLimit)) {
				logger.info("first space between *secondaryLimit* - *mainLimit*");
				offset += addParagraph(paragraphs, sb, offset, index);
				continue;
			}
			// split at last space before 800 character
			index = sb.lastIndexOf(" ", secondaryLimit);
			if(isInRange(index, secondaryLimit)) {
				logger.info("last space before *secondaryLimit*");
				offset += addParagraph(paragraphs, sb, offset, index);
				continue;
			}

			// split into *mainLimit* chunk
			offset += addParagraph(paragraphs, sb, offset, Math.min(sb.length(), mainLimit));
		}

		return paragraphs;
	}

	private int addParagraph(List<Paragraph> paragraphs, StringBuilder text, int start, int length) {
		logger.info("new paragraph start {}, length {}", start, length);
		StringBuilder paragraph = new StringBuilder(text.substring(0, length));
		int trimmedLength = length - trimEnd(paragraph);
		paragraphs.add(new Paragraph(paragraph.toString(), start, trimmedLength, false));
		text.delete(0, length);
		return length + trimStart(text);
	}

	private boolean isInRange(int index, int upperBound) {
		// index == -1 means the term was not found
		return index >= 0 && index < upperBound;
	}

	private int trimStart(StringBuilder sb) {
		Matcher matcher = TRIM.matcher(sb);

		if(matcher.find()) {
			sb.delete(0, matcher.start());
			return matcher.start();
		} else {
			return clear(sb);
		}
	}

	private int trimEnd(StringBuilder sb) {
		for(int i = sb.length() - 1; i >= 0; i--) {
			char ch = sb.charAt(i);

			if(!Character.isWhitespace(ch)) {
				int length = sb.length();
				sb.delete(i + 1, sb.length());
				return length - i - 1;
			}
		}

		return clear(sb);
	}

	private int clear(StringBuilder sb) {
		int length = sb.length();
		sb.delete(0, length);
		return length;
	}
}
