package com.github.motyka.textprocessor.paragraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("paragraphs")
public class ParagraphController {

	private final Logger logger = LoggerFactory.getLogger(ParagraphController.class);

	private ParagraphService service;

	public ParagraphController(ParagraphService service) {
		this.service = service;
	}

	@PostMapping("/splitAndSearch")
	public List<Paragraph> splitAndSearch(
			@RequestBody SearchCriteria searchCriteria,
			@RequestParam(defaultValue = "1200") int mainLimit,
			@RequestParam(defaultValue = "800") int secondaryLimit) {
		logger.info("splitAndSearch: {}, mainLimit: {}, secondaryLimit: {}", searchCriteria, mainLimit, secondaryLimit);
		try {
			return service.splitAndSearch(searchCriteria.getText(), searchCriteria.getSearchTerm(), mainLimit, secondaryLimit);
		} catch(NullPointerException | IllegalArgumentException ex) {
			// this exception will override Spring's default HTTP code for NPE and IAE from 500 to 400 (BAD_REQUEST)
			throw new ParagraphSplittingException(ex);
		}
	}
}
