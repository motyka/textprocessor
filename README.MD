**TO DO:**

In case of performance problems the method that splits the paragraphs could be refactored to iterate over the text character by character, remembering last new line, punctuation, dot etc. and do the splitting based on it.
I didn't want to do what is called premature optimization. I assume a lower level solution would be harder to maintain, so as long the performance isn't a problem it is better to have a clearer solution which uses build in Java's methods.