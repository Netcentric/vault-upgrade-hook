# Sling Script Engine Sample Package

## Features

- executing scripts using [Sling Scripting](https://sling.apache.org/documentation/bundles/scripting.html)
- supports execution of JSP, ECMA, GSP (GroovyServerPages) etc. scripts
- inside script object `resource` is defined, which represents current script resource. It can be used to get ResourceResolver and do manipulations with repository.

## Dependencies

The compilation and execution of the scripts relies on an installed ScriptEngine bundles.

## Usage

To execute a script place it under `META-INF/vault/definition/upgrader/<upgrade-info>/[<phase-prefix>]<script-name>.<extension>`. The optional `<phase-prefix>` (`PREPARE`, `INSTALLED`, `END`) is not case sensitive and configures the phase of execution. If no phase prefix is there `PREPARE` is the default. 

Note that the scripts will be sorted and executed by name.

## Versioning

Script's content is used to generate md5 hash which is used as its version.
