# Banking AI Chat Service (`banking-ai-chat`)

A reactive, high-performance **Spring Boot WebFlux** service designed for conversational banking workflows. Built with resilience, automated retries, and flexible multi-provider AI model integration.

---

## Key Features

* **Reactive & Non-Blocking**: Built on **Spring WebFlux** and **Reactor Netty** for maximum throughput and minimal resource usage.
* **Multi-LLM Provider Support**: Supports **Google Gemini**, **OpenAI**, **Azure OpenAI**, **GitHub Models**, and **Local Ollama** through standard OpenAI-compatible endpoints.
* **Resilient Network Layer**: Automated exponential backoff retries, configurable jitter, failure filtering, and timeout management.
* **Request & Correlation Tracking**: Generates and passes unique `X-Client-Request-Id` and correlation tracking headers for auditing and observability.
* **Strict Configuration Validation**: Uses Jakarta Bean Validation (`@Validated`, `@NotBlank`, `@Min`, `@Max`) to enforce valid application settings on startup.

---

## Tech Stack

* **Java**: 17+
* **Framework**: Spring Boot 3.x (WebFlux)
* **HTTP Client**: Reactive `WebClient` with Netty Connector
* **JSON Processing**: Jackson (`JavaTimeModule`)
* **Build Tool**: Maven / Gradle

---

## Configuration Example (`application.yml`)

The application is configured using properties under the `ai.openai` prefix:

```yaml
server:
  port: 8080

ai:
  openai:
    # Google Gemini OpenAI-compatible endpoint
    base-url: ${AI_BASE_URL:[https://generativelanguage.googleapis.com/v1beta/openai](https://generativelanguage.googleapis.com/v1beta/openai)}
    api-key: ${GEMINI_API_KEY}
    model: ${AI_MODEL:gemini-2.5-flash}
    timeout: 20s
    max-output-tokens: 600
    retry:
      max-retries: 2
      min-backoff: 300ms
      max-backoff: 3s
