# MojoHaus Truezip Maven Plugin

This is the [true-maven-plugin](http://www.mojohaus.org/truezip/).

[![Build Status](https://travis-ci.org/mojohaus/truezip.svg?branch=master)](https://travis-ci.org/mojohaus/truezip)

## About this fork

This is a fork of [mojohaus/truezip](https://github.com/mojohaus/truezip), created to
fix an archive-suffix limitation that affects manipulating NetBeans Module (`.nbm`)
files, and to bring the build up to a currently-supported Maven/Java baseline.

### Why

The plugin recognizes which file suffixes should be treated as browsable archive
"virtual directories" via a fixed suffix-to-driver mapping in
`DefaultTrueZipArchiveDetector`. `.nbm` files are zip/jar-shaped archives (produced
by `nbm-maven-plugin`), but the suffix wasn't in that list. As a result, `remove`
(and the other archive-manipulation goals) failed against a `.nbm` file with a
misleading `FileSet's directory: ... not found` error, even though the file plainly
existed on disk — TrueZIP was treating it as an ordinary file rather than an archive,
since its suffix wasn't registered.

### What changed vs. upstream

- **`.nbm` is now a recognized jar-family suffix**, alongside `jar|war|ear|sar|swc|nar|esb|par`,
  in `DefaultTrueZipArchiveDetector`. This is the core fix — a one-line suffix addition,
  not a general configurable-suffix mechanism (deliberately kept simple for now).
- **Clearer error message on failure**: `DefaultTrueZip.remove()` now distinguishes a
  genuinely missing path from a path that exists on disk but isn't recognized as an
  archive because of its suffix, and says so explicitly instead of reporting "not found"
  either way.
- **Regression coverage**: `TrueZipTest#testNbmIsJarArchive` builds a zip fixture with
  a `.nbm` suffix (using plain JDK zip APIs, not TrueZIP, so the test doesn't presuppose
  the fix) and asserts it's recognized and browsable as an archive.
- **Modernized build floor**: Maven prerequisite raised from 2.2.1 to 3.6.3, Java target
  raised from 1.6 to 1.8. This also required swapping the `maven-project` dependency for
  `maven-core`, since `maven-project` was never published past the Maven 2.x line.

### What did *not* change

- The suffix list is still hardcoded, not exposed as a Mojo parameter. A general
  user-configurable suffix mapping was considered but intentionally deferred — the `.nbm`
  fix alone was enough to unblock the motivating use case, and a full configurable-mapping
  design (with all its POM-schema and validation considerations) can come later if it's
  ever actually needed.
- The plugin still depends on `de.schlichtherle.truezip` 7.7.10, which is an unmaintained
  library — TrueZIP was succeeded years ago by [TrueVFS](https://github.com/christian-schlichtherle/truevfs).
  Migrating to TrueVFS would be a larger, riskier change (package/API renames) and is left
  as a possible future improvement rather than bundled into this fix.

## Releasing

```bash
(cd truezip-utils mvn -release:prepare release:perform)
(cd truezip-maven-plugin mvn -release:prepare release:perform)
```

For publishing the site do the following:

```bash
cd target/checkout
mvn verify site site:stage scm-publish:publish-scm
```