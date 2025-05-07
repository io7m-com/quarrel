quarrel
===

[![Maven Central](https://img.shields.io/maven-central/v/com.io7m.quarrel/com.io7m.quarrel.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.io7m.quarrel%22)
[![Maven Central (snapshot)](https://img.shields.io/nexus/s/com.io7m.quarrel/com.io7m.quarrel?server=https%3A%2F%2Fs01.oss.sonatype.org&style=flat-square)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/io7m/quarrel/)
[![Codecov](https://img.shields.io/codecov/c/github/io7m-com/quarrel.svg?style=flat-square)](https://codecov.io/gh/io7m-com/quarrel)
![Java Version](https://img.shields.io/badge/21-java?label=java&color=e6c35c)

![com.io7m.quarrel](./src/site/resources/quarrel.jpg?raw=true)

| JVM | Platform | Status |
|-----|----------|--------|
| OpenJDK (Temurin) Current | Linux | [![Build (OpenJDK (Temurin) Current, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/quarrel/main.linux.temurin.current.yml)](https://www.github.com/io7m-com/quarrel/actions?query=workflow%3Amain.linux.temurin.current)|
| OpenJDK (Temurin) LTS | Linux | [![Build (OpenJDK (Temurin) LTS, Linux)](https://img.shields.io/github/actions/workflow/status/io7m-com/quarrel/main.linux.temurin.lts.yml)](https://www.github.com/io7m-com/quarrel/actions?query=workflow%3Amain.linux.temurin.lts)|
| OpenJDK (Temurin) Current | Windows | [![Build (OpenJDK (Temurin) Current, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/quarrel/main.windows.temurin.current.yml)](https://www.github.com/io7m-com/quarrel/actions?query=workflow%3Amain.windows.temurin.current)|
| OpenJDK (Temurin) LTS | Windows | [![Build (OpenJDK (Temurin) LTS, Windows)](https://img.shields.io/github/actions/workflow/status/io7m-com/quarrel/main.windows.temurin.lts.yml)](https://www.github.com/io7m-com/quarrel/actions?query=workflow%3Amain.windows.temurin.lts)|

## quarrel

The `quarrel` package provides a strict, opinionated command-line argument
parser.

## Features

* Strongly-typed access to command-line arguments, for program correctness.
* Simple and regular command-line parsing for easily understood command-line interfaces.
* Automatic generation of "help" and "version" commands for command-line interfaces.
* Detailed, structured, and localized user-facing error messages for clear
  explanations as to how to use the command-line interfaces correctly.
* A small, easily auditable codebase with no use of reflection or annotations.
* An extensive automated test suite with high coverage.
* Supplies a restricted form of @ syntax, for storing command-line arguments in files.
* Written in pure Java 21.
* [OSGi-ready](https://www.osgi.org/).
* [JPMS-ready](https://en.wikipedia.org/wiki/Java_Platform_Module_System).
* ISC license.

## Usage

See the [documentation](https://www.io7m.com/software/quarrel/).

