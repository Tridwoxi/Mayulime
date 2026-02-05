# Repository guidelines

If you need information about system architecture, the task specification, coding
style, or theory, consult `docs/` or `README.md`.

Do not write documentation unless asked. Write comments as needed. If you stumble upon
lies (either in the docs or in Javadoc), please inform the human.

If you edit code, do `./gradlew build`. This will compile and run Checkstyle and
Spotbugs. Fix any violations, unless they are exceptionally annoying, in which case
stop and inform the human. Do not attempt to launch the project as a whole unless asked.

If the gradle wrapper or jar is not available, ask the human to create it. If you are
Codex, the system gradle is unlikely to work.
