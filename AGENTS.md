# Repository guidelines

Consult `README.md` for an overview. If you edit code, run `./gradlew build` as a test.

Do not worry about constant-factor performance wins. Prefer ArrayLists and Streams.

Use `assert` statements for invariants. Since the solver is a self-contained system,
all errors are actually AssertionErrors. Assertions must never fail. `throw` custom
exceptions for things that might fail but are not programmer errors and can be handled.

Use descriptive variable names. Never use one letter variable names. Abbreviation is acceptable but discouraged unless easily understood.

Be hesitant to create custom classes and interfaces. Do not extend or implement more
than one layer deep. Some code reuse is acceptable.

Never pass `null`. It is a design error to write a subroutine that intends to accept or
return null. Hence, there is usually no need to check. You may still produce and check
for nulls within subroutines, such as if a library function produces null.
