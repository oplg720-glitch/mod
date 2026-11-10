# Build status

- Source files and cloud build configuration have been generated.
- Static JSON validation and project structure checks can be performed locally.
- A real Gradle compile has **not** been run in the current authoring environment: it has OpenJDK 21, while Minecraft 26.1.2 requires Java 25; Gradle is also not installed and outbound dependency downloads are unavailable here.
- Run `gradle --no-daemon clean build` in the configured GitHub Codespace or let the included GitHub Actions workflow run. Only mark the mod as built after that process succeeds.
