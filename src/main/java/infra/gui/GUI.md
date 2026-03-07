# GUI

<!-- NOTE TO CODING AGENTS: Please keep this document up to date. -->

This application is for users who already understand Pathery. The GUI should be understandable
at a glance, keep the board as the focus, and avoid design choices that hurt performance on large
boards.

After loading a puzzle, the user will often leave the application running and return later to
inspect the current best board. The GUI is therefore board-first rather than a rich live-monitoring
dashboard, though input and restart flows still matter because users may try multiple maps in one
session.

## ADR

The board must be rendered with a single `Canvas`. A one-node-per-cell JavaFX implementation made
window resizing catastrophically slow, while the current canvas-based board performs acceptably.

Controller-owned input handling is preferred over view-owned IO. The view should surface user
intent, while clipboard access, file loading, chooser policy, and user-facing validation live with
the UI controller so the layout can be redesigned without moving application behavior.

The board must occupy most of the screen and remain visually stable while solving. Solver updates
should change cell contents only, not move or resize the board. Users need to see as much of the
board as possible because wall effects are highly non-local. Dragging is only meaningful when the
board overflows, and the current fit behavior is acceptable.

The sidebar is intentionally minimal. The interface should expose only a few actions and a small
amount of text. Both file-open and paste-MapCode flows must exist because both are useful in real
workflows. Zoom and manual theme controls should not be added. Text styling should stay restrained,
and the legend should remain visible because the GUI colors do not match Pathery's colors.

Button state should clearly communicate whether an action is available. The current `Stop` to
`Restart` behavior is deliberate. The `Stop` action must exist because the solver is compute-heavy
and may continue changing the board after the user returns to inspect it.

Status messaging should generally prefer the latest important event. Layout and styling may change,
including the choice of a right sidebar, as long as performance, board dominance, low information
density, and the minimal control surface are preserved. Mouse-first interaction is acceptable;
keyboard support is optional and not currently planned.

Rendering should be event-driven where possible. Solver updates, puzzle lifecycle events, theme
changes, and viewport changes should trigger real board renders, while timer text can refresh on a
separate coarse cadence. The GUI should not rely on a continuous high-frequency repaint loop.

## Future direction

The color scheme is bad, but we might just be doomed to live with it.

It's weird that GUI thinking bleeds into the Application launch point.

It'd be nice if the user knew how many threads of theirs we were using, since that's a good
indicator of the impact we're having on their machine.
