# Napkin Runbook
## Curation Rules
- Re-prioritize on every read.
- Keep recurring, high-value notes only.
- Max 10 items per category.
- Each item includes date + "Do instead" line.

## Execution & Validation (Highest Priority)
1. **[2026-10-30] Preserve program behavior during UI work.**
   Do instead: keep listeners and service/repository calls unchanged when editing Swing presentation.
2. **[2026-10-30] Use project JDK 21 settings.**
   Do instead: compile with `javac --release 21` to a temporary directory, not tracked `target/classes`.

## Shell & Command Reliability
1. **[2026-10-30] Maven may be unavailable in PATH; `target/classes` is tracked.**
   Do instead: use the installed JDK 21 compiler and direct output to `%TEMP%`.

## Domain Behavior Guardrails
1. **[2026-10-30] RegiUnic uses Swing; its main screen is `src/main/java/com/regiunic/RegiUnicApp.java`.**
   Do instead: keep blocking work off the EDT and avoid calling `revalidate()` recursively from resize listeners.

## User Directives
1. **[2026-10-30] Keep existing program functions and behavior unchanged.**
   Do instead: preserve listeners, navigation outcomes, and service/repository behavior during UI work.
2. **[2026-11-04] Keep interface motion smooth, restrained, and comfortable.**
   Do instead: use short movement feedback and honor reduced-motion preferences where the platform exposes them.
