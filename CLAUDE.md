# Astrogeist 2

Desktop app for astrophotographers. Scans observation session folders, builds a chronological timeline, browse metadata and annotate sessions.

> **Restart note:** the current code is an early experiment. Before extending
> it, read `docs/direction.md`. That document records the approved direction
> for a future restart and supersedes the configurable-scanner rule below.

## Build

```
make          # compile + package
make run      # build and launch
make test     # run unit tests
make clean    # delete build/
```

First time setup — download dependencies:
```
./scripts/download-libs.sh
```

## Project layout

```
src/astrogeist/
  app/        entry point, AppInfo, Resources
  model/      Timeline, Snapshot, TimelineValue, Type hierarchy
  scanner/    ConfigurableScanner engine + config model
  persist/    XML readers/writers
  service/    SnapshotSelectionService
  ui/         Swing panels and dialogs

test/astrogeist/
  mirrors src/ — one test class per logical unit

lib/          runtime JARs (not in git — run download-libs.sh)
lib/test/     JUnit JAR (not in git)
```

## Key architectural rules

- `engine` packages (model, scanner, persist, service) have **no Swing imports**
- All Swing work runs on the EDT
- The two scanner rules below describe this experiment only. They are not the
  direction approved for the restart:
  - Scanner behaviour is expressed as XML config files, not Java classes
  - New data sources need a new XML config, not new code

## Workflow

- Run `make test` before committing
- Run `make run` after UI changes
- Scanner configs live in `src/astrogeist/scanner/configs/` (bundled) and `~/.astrogeist2/scanners/` (user)
