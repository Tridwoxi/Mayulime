# Repository guidelines

Consult `README.md` for an overview. If you edit code, do `./gradlew build` to check the project didn't break.

Do not worry about constant-factor performance wins. Prefer ArrayLists, Streams, and high-level abstraction.

Use `assert` statements for things that should never fail: these serve as Errors and indicate a programming mistake. `throw` custom Exceptions for things that might fail.

Make all constant variables and custom classes `final`. It is a design error to extend custom classes. You may still extend library classes.

Never pass `null`. It is a design error to write a subroutine that intends to accept or return null. Hence, there is usually no need to check. You may still produce and check for nulls within subroutines, such as if a library function produces null.
