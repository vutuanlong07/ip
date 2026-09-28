---
layout: page
title: Marquee
---

[![CLI Build](https://github.com/vutuanlong07/ip/actions/workflows/build-cli.yaml/badge.svg)](https://github.com/vutuanlong07/ip/releases)
[![GUI Build](https://github.com/vutuanlong07/ip/actions/workflows/build-gui.yaml/badge.svg)](https://github.com/vutuanlong07/ip/releases)

![Ui](Ui.png)

## Overview

Marquee is an API for a task-keeping assistant that tries to mimic natural language as close as possible to give you a smooth conversation.
This repo comes with a basic CLI build out of the box for Marquee.

- Guide for CLI version: [User Guide - CLI version](cli.md)

There is an additional GUI build that doesn't operate using commands if that is preferred.

- Guide for GUI version: *TBA*

For developers, a JAR containing the base features excluding the CLI and GUI application is available for download.
The JAR is fully annotated with Javadoc. Refer to the Javadoc for help.

## Features

- Manage tasks, deadlines and events
- Search for upcoming tasks
- Update tasks
- Batch task operations
- Uses CSV file format compatible with spreadsheet programs
- (CLI) Recognize many date-time formats
- (GUI) Create tags on-the-fly and add tags seamlessly

## Installation

All downloadables are located in [GitHub Release](https://github.com/vutuanlong07/ip/releases).

**Acknowledgements**

* Libraries used: [JavaFX](https://openjfx.io/), [JUnit5](https://github.com/junit-team/junit5)