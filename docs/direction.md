# Astrogeist 2: direction for a restart

Status: a record of direction for future work. The existing Astrogeist 2 code
is an early experiment, not the foundation this document approves.

## The idea worth preserving

Astrogeist is a filesystem-backed, scanner-assembled observation timeline.
It does not import or make private copies of astronomical captures. It scans
the files where the observing equipment or reader placed them and assembles a
view of what happened.

The original Astrogeist expressed this particularly well:

- every scanner contributed partial facts at an instant;
- contributions from several scanners were merged at that instant;
- capture files remained the authority for facts extracted from them;
- reader-authored data was saved in a companion file named for the same
  instant; and
- a user-data scanner read that companion file back through the same path as
  every other source.

The resulting timeline could be discarded and reconstructed from capture
files plus the reader's companion files. That is a product property, not an
implementation detail.

## Stable centre, specific edges

The centre should define one small contribution contract and an assembler.
The edges should be explicit scanners for real sources:

```text
SharpCap scanner ---\
SeeStar scanner -----+--> contributions keyed by time --> observation timeline
User-data scanner --/
```

A universal scanner configuration language is not a prerequisite. SharpCap
and SeeStar differ in workflow, directory structure, clocks, session meaning,
metadata and failure cases, not merely syntax. A direct scanner for each
source is easier to read, test and update. Shared parsing should be extracted
only after more than one scanner demonstrates the same need.

AI-assisted maintenance makes this approach practical: retain representative,
anonymised fixtures from real equipment, update the affected scanner when an
output format changes, and prove the change against those fixtures.

Every scanner must make clear:

- which files it recognises;
- how it derives time;
- which files belong to one capture or session;
- which facts it observed and which source supports each fact; and
- what it could not read or correlate.

Scanners contribute facts; they do not construct an authoritative, completed
observation independently. The assembled index may be cached, but must remain
disposable and reproducible.

## Time and identity

Time to the second remains the primary correlation key because it gives the
timeline and the companion-file mechanism their clarity. It must not be
mistaken for a guarantee that only one source or capture can occupy a second.
The model must allow multiple contributions at an instant and retain source
identity, such as scanner and path, so collisions and questionable equipment
clocks can be represented rather than overwritten.

Facts should retain provenance and distinguish at least:

- metadata read from source material;
- values inferred by a scanner; and
- statements or corrections authored by the reader.

Reader annotations remain durable companion files. Astrogeist writes them and
the user-data scanner reads them on the next reconstruction. They must not
exist only as rows in an internal database.

## First useful release

The first release should be deliberately narrow: scan SeeStar S50 observation
folders in place and present a bucket list of what has been seen.

Its useful minimum is:

- one explicit SeeStar scanner built from real output;
- a chronological, reconstructible observation timeline;
- an observed-object view with count, first observation and latest observation;
- the observations beneath each object;
- source paths and the metadata the files actually carry;
- reader notes written as timestamped companion files and re-ingested by their
  scanner; and
- visible reporting of files that could not be recognised or correlated.

Deleting the derived index and rescanning the SeeStar sources and companion
files must reproduce the same bucket list. That is the first architectural
acceptance test.

Solar work, richer session concepts, planning, statistics and capture
comparison follow only after this slice establishes the foundation.

## Relationship with JUranometria

Astrogeist is a separate repository, application and release line.
JUranometria owns cartography: catalogue identities, projection, navigation,
working selection and removable sky overlays. Astrogeist owns observational
evidence: discovered files, extracted metadata, timelines, notes and observing
history. Astrogeist must remain useful for solar work where an atlas adds
nothing, and JUranometria must not acquire file-scanning responsibilities.

Their future connection should be a small, versioned exchange boundary based
on ordinary data rather than shared internal classes. Likely first routes are:

- Astrogeist asks JUranometria to show or centre on a recorded target; and
- JUranometria sends a working selection to Astrogeist as observing intention.

The exchange should work when the applications are released independently and
should not require either repository to absorb the other's domain. Its exact
form waits for the first real use to demand it.

## Restart consequence

Useful discoveries and fixtures from the current Astrogeist 2 experiment may
be retained, but its configurable-scanner premise and replacement-style
timeline are not constraints on the restart. The original merge model is the
starting point: sources contribute, the timeline assembles, and user sidecars
participate as sources.
