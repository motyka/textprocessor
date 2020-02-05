package com.github.motyka.textprocessor.paragraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParagraphService {

	private final Logger logger = LoggerFactory.getLogger(ParagraphService.class);

	private static final Pattern PUNCTUATION = Pattern.compile("\\p{Punct}");
	// there can't be alphanumeric characters between new line characters
	private static final Pattern NEW_LINES = Pattern.compile("\n[^\\p{Alnum}]*\n");
	// there needs to be a paragraph (alphanumeric characters) before the dot
	private static final Pattern DOT = Pattern.compile("\\p{Alnum}.*?\\.");
	// only keep punctuation that adhere to the paragraph
	private static final Pattern TRIM = Pattern.compile("\\p{Punct}*\\p{Alnum}");

	public List<Paragraph> splitAndSearch(String text, String searchTerm, int mainLimit, int secondaryLimit) {
		logger.info("split {} - {} and search for:{}", secondaryLimit, mainLimit, searchTerm);

		validate(text, mainLimit, secondaryLimit);
		StringBuilder textLeft = new StringBuilder(text);
		List<Paragraph> paragraphs = new ArrayList<>();

		trimEnd(textLeft);
		// the starting position of paragraph
		int offset = 0;
		offset += trimStart(textLeft);

		while(textLeft.length() > 0) {
			// always split at double new line before 1200 character (can be separated by non alphanumeric characters)
			Matcher matcher = NEW_LINES.matcher(textLeft);
			if(matcher.find() && isInRange(matcher.start(), mainLimit)) {
				logger.info("double new line before *mainLimit*");
				offset += addParagraph(paragraphs, textLeft, offset, matcher.start(), searchTerm);
				continue;
			}

			// no need to split anymore if too small
			if(textLeft.length() < mainLimit) {
				addParagraph(paragraphs, textLeft, offset, textLeft.length(), searchTerm);
				break;
			}

			CharSequence mainSubstring = textLeft.subSequence(0, Math.min(textLeft.length(), mainLimit));
			CharSequence secondarySubstring = textLeft.subSequence(0, Math.min(textLeft.length(), secondaryLimit));

			// NEW LINE
			Optional<Integer> newLineResult = characterMatching("\n", textLeft, mainLimit, secondaryLimit);
			if(newLineResult.isPresent()) {
				logger.info("separating because of new line");
				offset += addParagraph(paragraphs, textLeft, offset, newLineResult.get(), searchTerm);
				continue;
			}

			// DOT
			Optional<Integer> dotResult = patternMatching(DOT, mainSubstring, secondarySubstring, mainLimit, secondaryLimit);
			if(dotResult.isPresent()) {
				logger.info("separating because of dot");
				offset += addParagraph(paragraphs, textLeft, offset, dotResult.get(), searchTerm);
				continue;
			}

			// PUNCTUATION
			Optional<Integer> punctuationResult = patternMatching(PUNCTUATION, mainSubstring, secondarySubstring, mainLimit, secondaryLimit);
			if(punctuationResult.isPresent()) {
				logger.info("separating because of punctuation");
				offset += addParagraph(paragraphs, textLeft, offset, punctuationResult.get(), searchTerm);
				continue;
			}

			// SPACE
			Optional<Integer> spaceResult = characterMatching(" ", textLeft, mainLimit, secondaryLimit);
			if(spaceResult.isPresent()) {
				logger.info("separating because of space");
				offset += addParagraph(paragraphs, textLeft, offset, spaceResult.get(), searchTerm);
				continue;
			}

			// split into *mainLimit* chunk
			int chunkSize = Math.min(textLeft.length(), mainLimit);
			offset += addParagraph(paragraphs, textLeft, offset, chunkSize, searchTerm);
		}

		return paragraphs;
	}

	private void validate(String text, int mainLimit, int secondaryLimit) {
		Objects.requireNonNull(text, "The text can't be null");

		if(mainLimit <= 0 || secondaryLimit <= 0) {
			throw new IllegalArgumentException("The main and secondary limits muse be greater than 0.");
		}
		if(secondaryLimit > mainLimit) {
			throw new IllegalArgumentException("The secondary limit can't be greater than the main limit.");
		}
	}

	/**
	 * Add a new paragraph and removes it from the passed text.
	 * @param paragraphs
	 * @param text
	 * @param start
	 * @param length
	 * @return number of characters removed from the passed text
	 */
	private int addParagraph(List<Paragraph> paragraphs, StringBuilder text, int start, int length, String searchTerm) {
		logger.info("new paragraph start {}, length {}", start, length);
		StringBuilder paragraph = new StringBuilder(text.substring(0, length));
		int trimmedLength = length - trimEnd(paragraph);
		boolean contains = search(paragraph, searchTerm);

		paragraphs.add(new Paragraph(paragraph.toString(), start, trimmedLength, contains));
		text.delete(0, length);
		return length + trimStart(text);
	}

	private Optional<Integer> patternMatching(Pattern pattern, CharSequence mainSubstring, CharSequence secondarySubstring, int mainLimit, int secondaryLimit) {
		// split at first match between *secondaryLimit* - *mainLimit* character
		Matcher matcher = pattern.matcher(mainSubstring);
		int index = matcher.results().filter(m -> m.end() >= secondaryLimit).map(MatchResult::end).findFirst().orElse(-1);
		if(isInRange(index, mainLimit)) {
			logger.info("first pattern match between {} - {}: {}", secondaryLimit, mainLimit, index);
			return Optional.of(index);
		}
		// split at last match before *secondaryLimit* character
		matcher = pattern.matcher(secondarySubstring);
		index = matcher.results().reduce((f, s) -> s).map(MatchResult::end).orElse(-1);
		if(isInRange(index, secondaryLimit)) {
			logger.info("last pattern match before {}: {}", secondaryLimit, index);
			return Optional.of(index);
		}
		return Optional.empty();
	}

	private Optional<Integer> characterMatching(String separator, StringBuilder text, int mainLimit, int secondaryLimit) {
		// split at first match between *secondaryLimit* - *mainLimit* character
		int index = text.indexOf(separator, secondaryLimit);
		if(isInRange(index, mainLimit)) {
			logger.info("first character match between {} - {}: {}", secondaryLimit, mainLimit, index);
			return Optional.of(index);
		}
		// split at last match before *secondaryLimit* character
		index = text.lastIndexOf(separator, secondaryLimit);
		if(isInRange(index, secondaryLimit)) {
			logger.info("last character match before {}: {}", secondaryLimit, index);
			return Optional.of(index);
		}
		return Optional.empty();
	}

	private boolean isInRange(int index, int upperBound) {
		// index == -1 means the term was not found
		return index >= 0 && index < upperBound;
	}

	private boolean search(StringBuilder paragraph, String searchTerm) {
		// assumed that the search team can't be empty to do the search
		return searchTerm != null && !searchTerm.isEmpty() && paragraph.indexOf(searchTerm) >= 0;
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
