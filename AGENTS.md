# Repository Guidelines

## Required Reading

Before starting work, read `docs/변수명_정의서.md` and follow its variable names and definitions when adding or modifying code.

## Project Structure & Module Organization

`ex06_2/` is the single Maven WAR module for the school cafeteria review system. Java code lives in `src/main/java/org/zerock/`: `controller` handles HTTP requests, `service` contains business logic, `mapper` defines persistence interfaces, `domain` holds VO/DTO classes, and `security` handles authentication and authorization. MyBatis SQL mappings live in `src/main/resources/org/zerock/mapper/`. JSP views are under `src/main/webapp/WEB-INF/views/`, Spring XML configuration under `WEB-INF/spring/`, and static assets under `src/main/webapp/resources/`. Tests live in `src/test/java/`, with logging configuration in `src/test/resources/`.

## Build, Test, and Development Commands

Run commands from `ex06_2/` so test configuration paths resolve correctly:

- `mvn clean compile`: compile the Java 8-targeted application.
- `mvn test`: run JUnit tests and generate JaCoCo coverage reports.
- `mvn -Dtest=MemberTests test`: run the existing member integration tests explicitly.
- `mvn clean package -DskipTests`: build a WAR in `target/` without executing database tests.

For local development, import the module into STS/Eclipse, enable Lombok support, and deploy to Apache Tomcat 9. Configure the Oracle datasource in local `WEB-INF/spring/root-context.xml` first; this file is ignored by Git. There is no Spring Boot launch command.

## Coding Style & Naming Conventions

Preserve surrounding formatting: production Java commonly uses tabs, while existing tests use two spaces. Use PascalCase class names and camelCase methods/fields. Follow existing suffixes such as `BoardController`, `BoardServiceImpl`, `BoardMapper`, `BoardVO`, and `ReplyPageDTO`. Keep mapper interfaces and XML statements synchronized. Reuse existing Spring, Lombok, and MyBatis patterns. No automated formatter is configured; compiler lint warnings are enabled.

## Testing Guidelines

Use JUnit 4 and Spring's `SpringJUnit4ClassRunner`; follow `*Tests` class naming. Add assertions for changed behavior. `MemberTests` inserts member and role records, so run it only against a disposable Oracle database. It catches database exceptions, and Surefire ignores test failures: inspect logs and `target/surefire-reports/`, not just the exit code. JaCoCo has no enforced coverage threshold.

## Commit & Pull Request Guidelines

Recent commits use short Korean descriptions without mandatory prefixes. Keep commits focused and describe the concrete change. For pull requests, include the purpose, affected pages or endpoints, verification performed, and any configuration requirements. Link relevant issues and attach screenshots for visible JSP/CSS changes. Never commit database credentials or generated `target/` output.
