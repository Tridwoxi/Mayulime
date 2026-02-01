# Repository guidelines

Consult `README.md` for an overview. If you edit code, run `./gradlew build` as a test.

Do not worry about constant-factor performance wins. Prefer ArrayLists and Streams.

Use `assert` statements liberally for both conditions and invariants. Since the system
is mostly self-contained, assertion errors are always design errors. You may throw
custom exceptions only when interacting with external systems, and must catch them.

Use descriptive variable names. Never use one-letter variable names. Abbreviation is
acceptable but discouraged unless easily understood.

Do not extend or implement custom classes and interfaces more than one layer deep. Some
code reuse is acceptable.

Never pass `null`. It is a design error to write a subroutine that intends to accept or
return null. Hence, there is usually no need to check. You may still produce and check
for nulls within subroutines, such as if a library function produces null.
