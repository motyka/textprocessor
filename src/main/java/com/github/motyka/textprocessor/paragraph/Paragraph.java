package com.github.motyka.textprocessor.paragraph;

import lombok.Value;

@Value
public class Paragraph {
	private String text;
	private int start;
	private int length;
	private boolean contains;
}
