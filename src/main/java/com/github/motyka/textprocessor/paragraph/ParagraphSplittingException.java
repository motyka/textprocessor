package com.github.motyka.textprocessor.paragraph;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ParagraphSplittingException extends RuntimeException {
	public ParagraphSplittingException(Throwable throwable) {
		super(throwable);
	}
}
