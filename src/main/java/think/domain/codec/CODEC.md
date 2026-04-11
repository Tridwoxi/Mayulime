# Codec: Pathery MapCode parsing and serialization

A Pathery MapCode is a string representation of a puzzle. MapCodes are used by the map editor
(https://www.pathery.com/mapeditor) and supposedly some API too. Simplified grammar:

```txt
MapCode         -> Metadata : Maze
Metadata        -> NumCols . NumRows . BlockingBudget . Name . string . string . string
NumCols         -> int
NumRows         -> int
BlockingBudget  -> int
Name            -> string
Maze            -> Token Maze | nothing
Token           -> Skip , Kind Order .
Skip            -> int | nothing
Kind            -> s | f | r | c
Order           -> int
```

Integers contain only digits and are strictly positive, except for BlockingBudget, which may also
be 0. Strings are alphanumeric and may contain spaces or be empty. {@code |} is a metasymbol
representing alternation. There are Kinds beyond those defined here, but they are not supported.
The trailing Metadata strings are not understood.

Puzzles in this system also need to be semantically correct: tiles stay within bounds, waypoints
must have unique orders, and blocking budget cannot exceed blank cells. Pathery supports variants
with multiple starts / finishes / waypoints, but we do not yet. Player walls are silently converted
to system walls and deducted from the blocking budget. Doing so enables the user to copy a
partially solved puzzle and send it back to this system but with walls locked in place.
