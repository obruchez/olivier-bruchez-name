# olivier-bruchez-name

[![Scala CI](https://github.com/obruchez/olivier-bruchez-name/actions/workflows/scala.yml/badge.svg?branch=master)](https://github.com/obruchez/olivier-bruchez-name/actions/workflows/scala.yml)

Source code for my personal website [olivier.bruchez.name](https://olivier.bruchez.name/), written in [Scala 3](https://www.scala-lang.org/), using [Play](https://www.playframework.com/), [Pekko](https://pekko.apache.org/) and [Bootstrap](https://getbootstrap.com/).

The content lives in the [`data`](data) directory (XML and Markdown files) and is fetched from GitHub by the running site, so updating a list only takes a commit. To work on a local copy of the data instead, point `url.base` to a `file:` URL in your own configuration file and run `sbt -Dconfig.file=/path/to/your.conf run` (JDK 21).
