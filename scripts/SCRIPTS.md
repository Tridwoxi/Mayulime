# Scripts: workflows and MapCode handling

This directory provides `*.mapcode`, `*.curse`, and `*.cursep` file handling utilities. See
`CODEC.md` for what a MapCode is.

Curse files are a visual representation (and should only be used for visualization) used by
[t3chn0l0g1c/pathery](https://github.com/t3chn0l0g1c/pathery). Cursep replaces the whitespace of
Curse files with periods. Round-trip behaviour from MapCode is lossy.

Scripts expected to be used repeatedly, such as `profile.sh`, should be stored here. Single-use
scripts should be writen by a LLM and then deleted.
