package com.github.motyka.textprocessor;

import com.github.motyka.textprocessor.paragraph.SearchCriteria;
import com.github.motyka.textprocessor.utils.ResourceUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TextProcessorApplicationTest {

	private String baseUrl;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@BeforeEach
	public void setUp() {
		baseUrl = "http://localhost:" + port + "/paragraphs/splitAndSearch";
	}

	@Test
	public void bigFile() {
		String text = ResourceUtils.readResource("big_test.txt");
		String expected = ResourceUtils.readResource("expected/big_test.txt");

		List<?> result = restTemplate.postForObject(baseUrl, new SearchCriteria(text, "ipsum"), List.class);

		assertEquals(expected, result.toString());
	}

	@Test
	public void separators() {
		String text = ResourceUtils.readResource("separators.txt");
		String expected = ResourceUtils.readResource("expected/separators.txt");
		String url = this.baseUrl + "?mainLimit=20&secondaryLimit=10";

		List<?> result = restTemplate.postForObject(url, new SearchCriteria(text, "separated"), List.class);

		assertEquals(expected, result.toString());
	}
}
