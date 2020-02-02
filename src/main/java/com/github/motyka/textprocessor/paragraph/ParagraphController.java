package com.github.motyka.textprocessor.paragraph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("paragraphs")
public class ParagraphController {

	private final Logger logger = LoggerFactory.getLogger(ParagraphController.class);

	@PostMapping("/splitAndSearch")
	public Paragraph split(@RequestBody SearchCriteria text) {
		logger.info("Split: " + text);
		return new Paragraph("Test", 1, 20, true);
	}

	// TODO
	@GetMapping("/check")
	public String index() {
		return "Greetings from Spring Boot!";
	}
}
