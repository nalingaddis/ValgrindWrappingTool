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

## Test Run

1. Open the git repository in Eclipse (or other Java Editor)
2. Set the project's Java Version to Java 15
3. Set the `DOCKER_PATH` variable in `src/valgrindpp/helpers/DockerHelper.java` equal to the path to your Docker executable.
    * This can be quickly found by running `which docker` on Mac or `where docker` on Windows.
4. Run `valgrind.pp.Main`
5. When the GUI appears, press `Browse` Navigate to `(GIT_ROOT)/Examples/` and select of the `*MutexLRU` directories.
    * The three `MutexLRU` directories represent three example solutions to the UNC Comp530 MutexLru assigment, each with varying degress of correctness.
6. Press `Test` and let Valgrind++ grade the assignment you selected. In the console you should see tracing information from the run. When the grading is complete the GUI will update to display which testcases passed or failed.