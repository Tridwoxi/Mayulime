# Repository guidelines

Consult `README.md` for an overview. If you edit code, run `./gradlew build` as a test.

Do not worry about constant-factor performance wins. Prefer ArrayLists, Streams, and high-level abstraction.

Use `assert` statements for invariants liberally. Assertion errors are programmer errors. `throw` custom exceptions for things that might fail but are not programmer errors and can be handled.

Use descriptive variable names. Never use one letter variable names. Abbreviation is acceptable but discouraged unless easily understood.

Make all constant variables and custom classes `final`. It is a design error to extend custom classes. You may still extend library classes.

Never pass `null`. It is a design error to write a subroutine that intends to accept or return null. Hence, there is usually no need to check. You may still produce and check for nulls within subroutines, such as if a library function produces null.
