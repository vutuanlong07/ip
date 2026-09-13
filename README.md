# Marquee

## Overview

Marquee is an API for a task-keeping assistant that tries to mimic natural language as close as possible to give you a smooth conversation.
This repo comes with a basic CLI build out of the box for Marquee.

## Table of Contents

* [Features](#features)
* [Installation](#installation)
* [Usage](#usage)

## Features

- [x] Manage tasks, deadlines and events
- [x] Search for upcoming tasks
- [ ] Update tasks
- [x] Batch task operations
- [x] Recognize many date-time formats
- [x] Allow easy extension by extending `marquee.base` classes

## Installation

The core JAR is available in Releases
CLI application is available in Releasse

## Usage

1. Download the JAR from Releases
1. Add the following to your `build.gradle` or `build.gradle.kts`:

#### Groovy

```groovy
dependencies {
    implementation files('/path/to/jar/marquee-core-v1.0.0.jar')
}
```

#### Kotlin

```kotlin
dependencies {
    implementation(files("/path/to/jar/marquee-core-v1.0.0.jar"))
}
```
