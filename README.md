# Valgrind++

Created By Nalin Gaddis

## About

Valgrind++ is a novel testing solution that combines existing state-of-the-art tools, Valgrind and Docker, via software written in Java, a high-level and easily debug-able language. Valgrind++ allows users to write application-specific tests for testing-unaware, low-level language programs in a high-level language. Furthermore these tests are executed in a controlled and consistent environment provided by Docker containers. 

## Setup

The steps to setup Valgrind++ on your machine are as follows:

* Clone this git respository
* [Download Docker](https://docs.docker.com/get-docker/)
    * You will need to create a docker accont so you can download the Valgrind++ docker image
    * Once docker is downloaded run: `docker pull nalingaddis/valgrind`. This will download the custom docker image needed to run Valgrind++.
* [Download Java 15 JDK](https://www.oracle.com/java/technologies/javase/jdk15-archive-downloads.html) 

## Example: MutexLru

 Navigate to the directory `(GIT_ROOT)/GradeScope`, ensure that docker is running, and run the command 
 
 `java -jar valgrindpp.jar ../Examples/MutexLru/CorrectMutexLRU`
 
 If you receive an error asking you to specify a path to the docker exectuable, provide the absolute path to your docker executable as the third command line argument after the project directory.
 
 This command will run a preconfigured binary of the autograder on the example project `CorrectMutexLRU`. You will see that three tests are run and their output printed in the console. Similarly a `results.json` is generated in the project directory containing the test results. Feel free to run the autograder on the other example projects as well and observe that they produce different autograder results.

 ## Configuration

 To configure Valgrind++ for a new project the steps are as follows:
 
 1. __WrapperDefinitions__: This file contains the definitions for which functions to instrument in the target project as well as any headers or libraries to include when compiling the wrapped binary. This file should be located at `(GIT_ROOT)/src/Definitions/(PROJECT_NAME)/WrapperDefinitions`. Examples of how to instrument functions can be found [here](https://github.com/nalingaddis/ValgrindWrappingTool/blob/main/src/Definitions/MutexLru/WrapperDefinitions). 
    * When specifying functions defined in librarys (ex: `pthread_mutex_init`) we have to also specify the library name. In example above we do this with `*, pthread_mutex_init` where the `*` represents a wildcard pattern for the library name (i.e. we want to instrument the function `pthread_mutex_init` regardless of what library it comes from). It is recommended that the `*` be used to match most library names unless there is reason to further specify the name.
    * In addition to specifying library names with wildcard patterns, we can also specify function names with wildcard patterns. For example `exec*` can be used to trace all calls to the `exec` function family in `C` if we don't care about which specific version of the function is used.

2. __Makefile__ (Optional): If your project is built with a `Makefile` you will need to specify a rule called `wrapped` that makes your project while including the `ValgrindWrapper.o` object file. This rule will be called by Valgrind++ to build your project after the `ValgrindWrapper.o` file has been created. Additionally, you will need to specify the command to execute the binary produced by your `Makefile` (see step 5). Here is an example [`Makefile`](https://github.com/nalingaddis/ValgrindWrappingTool/blob/main/Examples/MutexLru/CorrectMutexLRU/Makefile) used in the `MutexLru` project. 

2. __Test Inputs__: To specify test inputs to be run against the function specify them as text files located in the directory: `(GIT_ROOT)/src/Definitions/(PROJECT_NAME)/Inputs/`. The project will be run once for each test input provided and all traces will be grouped together in a single list provided to the testing class (see below). An example of inputs can be found [here](https://github.com/nalingaddis/ValgrindWrappingTool/tree/main/src/Definitions/ProducerConsumer/Inputs).

3. __Tester__: Exact target project will need to define an implementation of `Tester` that extends `AbstractTester` and should be located in the `valgrindpp.tester.implementations` package (this is not technically necessary but helps with organization). The `Tester` class implements a single method, `List<Test> test()`, and should leverage the inherited properties `traces` and `stdout` which contain the instrumented function traces and program's StdOut respectively. The example `MutexLruTester` can be found [here](https://github.com/nalingaddis/ValgrindWrappingTool/blob/main/src/valgrindpp/tester/implementations/MutexLruTester.java).

4. __ValgrindConfiguration__: After the three resources mentioned above have been created, an implementation of the `ValgrindConfiguration` class should be created or the existing implementation modified. The only three properties needing editting are `DefinitionsDirectory`, `Tester`, and `ExecutionCommand`. The `ExecutionCommand` is the command that Valgrind++ will run to execute your project's binary (not including testfiles to write to StdIn). Examples of configurations can be found in the comments of the existing [`ValgrindConfiguration.java` file](https://github.com/nalingaddis/ValgrindWrappingTool/blob/main/src/valgrindpp/main/ValgrindConfiguration.java).

## Runnable JAR File

After configuring Valgrind++ for your project, you can easily distrbute this grading tool by compiling the `(GIT_ROOT)/src` directory into a _Runnable Jar File_. Developers with a copy of this JAR file can easily test their version of the project by running:

`java -jar RUNNABLE_JAR_FILENAME.jar path/to/project/directory/...`

A precompiled JAR has been provided at `(GIT_ROOT)/GradeScope/valgrindpp.jar`, to run this JAR on an example assignment execute the following the command at `(GIT_ROOT)`:

`java -jar GradeScope/valgrindpp.jar Examples/MutexLru/CorrectMutexLru`

## Makefile

A Makefile has been provided for your convince to easily compile the source code and create a runnable jar located at `GradeScope/valgrindpp.jar`

## GradeScope

This project has been specially modified to run in the GradeScope autograder environment. The steps to set up a Valgrind++ autograder are as follows:

1. Configure Valgrind++ for your assignment (see above).
2. Compile `(GIT_ROOT)/src` into a runnable jar file located at `(GIT_ROOT)/GradeScope/valgrindpp.jar`.
3. Compress the three files in `(GIT_ROOT)/GradeScope/` into a ZIP file and upload to GradeScope.

By placing all configuration and testcase files within the Jar file, we are able to create an autograder binary that can be easily run both locally and on GradeScope.
