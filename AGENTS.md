# Repository guidelines

Consult `README.md` for an overview. If you edit code, do `./gradlew build` to check the project didn't break.

It is permitted but discouraged to mutate custom classes. Prefer copying with modification instead. If you mutate, prefer to do so privately.

It is a design error to write a subroutine that can accept or return null. However, you may use nulls from library code and check for it within a subroutine as long as it does not escape.
