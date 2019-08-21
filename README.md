# 🔧 WRENCH

Wrench is a micro test framework for developing compiler plugins for [Dotty](https://github.com/lampepfl/dotty/).

## Usage

### SBT setting

Add Wrench as a test dependency in SBT:

```Scala
    libraryDependencies ++= Seq(
      "ch.epfl.lamp" %% "dotty-compiler" % dottyVersion % "test",
      "com.novocode" % "junit-interface" % "0.11" % "test",
      "xmid.org" % "scala-wrench" % "0.11" % "test"
    )
```

Also, set the `fork` option to `true`:

```Scala
    fork := true,    // important
```

The file [build.sbt](./build.sbt) can serve as an example.

### Writing tests

Now, you can specify the tests like the following:

```Scala
    class Tests {
        @Test def posTests = filesInDir("tests/pos").shouldCompile

        @Test def negTests = filesInDir("tests/neg").shouldNotCompile

        @Test def runTests = filesInDir("tests/run").shouldRun

        @Test def pluginDivZeroTests = withPlugin("tests/plugins/divideZero") {
            filesInDir("tests/plugins/divideZeroTests").shouldNotCompile
        }
    }
```

For details, please refer to the source file: [Tests.scla](./wrench/src/test/scala/Tests.scala)

## Credits

The code was adapted from the test framework for Dotty, which was initially
developed by @felixmulder .

