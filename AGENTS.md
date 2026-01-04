# Repository guidelines

Consult `README.md` for an overview. If you edit code, do `gradle build` to check the project didn't break.

It is permitted but discouraged to mutate custom classes. Prefer copying with modification instead. If you mutate, prefer to do so privately.

It is a design error to write a subroutine that can accept or return null. Hence, there is no need to check for null in project code, though it is still important to check library code.
