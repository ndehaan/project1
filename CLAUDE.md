# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

A single-file Python CLI for managing todos, persisted to `tasks.json` (gitignored).

## Running the app

```
python todo.py add "task title" [--due YYYY-MM-DD]  # create a task
python todo.py list                                  # incomplete tasks only
python todo.py list-all                              # all tasks
python todo.py due <id> YYYY-MM-DD                  # set/update due date
python todo.py complete <id>
python todo.py delete <id>
```

## Architecture

[todo.py](todo.py) has no external dependencies and uses manual `sys.argv` parsing (no argparse). Tasks are stored in `tasks.json` as a JSON array of objects with fields: `id`, `title`, `done`, `due`.

`tasks.json` is created relative to the working directory where the script is invoked, not the script's own directory.

Task IDs are assigned as `len(tasks) + 1` at insert time and are never reassigned after deletion, so IDs can have gaps after deletes.
