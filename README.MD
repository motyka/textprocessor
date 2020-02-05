The application can be started using `mvn spring-boot:run`. The default port is `8081` and it is configured in `application.properties`. To start the application with a different port run `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9999`

**API**

While the server is running the API can be checked under `http://localhost:8081/swagger-ui.html`.

**Tests**

Unit tests are in class `ParagraphServiceTest`. In class `TextProcessorApplicationTest` are integration tests setting up the server and making API calls. All tests can be run using `mvn test`.

**To do:**

In case of performance problems the method that splits the paragraphs could be refactored to iterate over the text character by character, remembering last new line, punctuation, dot etc. and do the splitting based on it.
I didn't want to do what is called premature optimization. I assume a lower level solution would be harder to maintain, so as long the performance isn't a problem it is better to have a clearer solution which uses build in Java's methods.
The amount of tests would make the refactoring easier.