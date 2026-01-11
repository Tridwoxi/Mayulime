# Repository guidelines

Consult `README.md` for an overview. If you edit code, do `./gradlew build` to check the project didn't break.

It is permitted but discouraged to mutate custom classes. Prefer copying with modification instead. If you mutate, prefer to do so privately.

Use `assert` statements for things that should never fail: these serve as Errors and indicate a programming mistake. Throw custom Oops Exceptions for things that might fail.

It is a design error to write a subroutine that intends to accept or return null. Hence, there is usually no need to check. However, you may produce and check for nulls within subroutines (e.g. when a library function returns null) as long as the null does not escape.
