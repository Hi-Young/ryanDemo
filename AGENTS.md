# Repository Guidelines

如需更详细信息，请参考 `CLAUDE.md`。

## Project Structure & Module Organization

- `src/main/java/`: Spring Boot source code. Entry point: `src/main/java/com/RyanDemoApplication.java`.
  - `com/ryan/`: primary “business” code (controller/service/entity/mapper).
  - `com/geektime/`: learning modules (algorithm, concurrency, design patterns, JVM, etc.).
  - `com/arthas/`: Arthas diagnostics demos.
- `src/main/resources/`: application config and assets:
  - `application.yml`, `application-dev.yml`, `logback-spring.xml`
  - `mapper/`: MyBatis XML (e.g., `mapper/UserMapper.xml`)
  - `sql/`: schema/scripts used by exercises
- `src/test/java/`: tests.
- Generated/runtime: `target/` (build output) and `logs/` (local logs) — do not commit.

## Build, Test, and Development Commands

- `mvn clean test`: run the test suite.
- `mvn spring-boot:run`: start the app locally (default profile is `dev`, port `18888`).
- `mvn spring-boot:run -Ptest` / `-Pprod`: run with a different Spring profile (Maven profiles are `dev|test|prod`).
- `mvn clean package`: build a runnable jar under `target/` (`-DskipTests` to skip tests).
- Optional Node utilities (MCP scripts in repo root): `npm ci` then `node mysql-mcp.js`.

## Coding Style & Naming Conventions

- Java 8 project; prefer 4-space indentation and avoid tabs.
- Naming: packages `lowercase`, classes `UpperCamelCase`, methods/fields `lowerCamelCase`, constants `UPPER_SNAKE_CASE`.
- Keep production Spring components under `com.ryan`; put demo/learning code under `com.geektime` or `com.arthas`.

## Testing Guidelines

- Testing comes from `spring-boot-starter-test`; this repo contains both JUnit 4 and JUnit 5 usage.
- Prefer JUnit 5 (`org.junit.jupiter.api.Test`) for new tests unless extending an existing JUnit 4 suite.
- Conventions: name tests `*Test` / `*Tests` and mirror the main package structure under `src/test/java`.

## Commit & Pull Request Guidelines

- Commit history favors short, topic-style messages (often Chinese). Keep the first line brief; add rationale/details in the body when needed.
- PRs should include: summary of changes, how to verify (commands run), and any config impact (DB/Redis/profile changes).
