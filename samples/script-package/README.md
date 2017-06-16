# Sling Script Engine Sample Package

## Features

- executing scripts using [Sling Scripting](https://sling.apache.org/documentation/bundles/scripting.html)
- supports execution of Groovy scripts

## Dependencies

The compilation and execution of the scripts relies on an installed Groovy-All bundle. Download the bundle from http://groovy-lang.org/download.html and install it.

## Usage

To execute a script place it under `META-INF/vault/definition/upgrader/<upgrade-info>/[<phase-prefix>]<script-name>.groovy`. The optional `<phase-prefix>` (`PREPARE`, `INSTALLED`, `END`) is not case sensitive and configures the phase of execution. If no phase prefix is there `INSTALLED` is the default. 

Note that the scripts will be sorted and executed by name.


