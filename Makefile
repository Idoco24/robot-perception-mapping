# Makefile for Maven project

# Variables
MVN = mvn
MAIN_CLASS = bgu.spl.mics.application.GurionRockRunner
CONFIG_FILE ?= ./configuration_file.json


# Default target
all: clean compile run

# Clean target
clean:
	$(MVN) clean

# Compile target
compile:
	$(MVN) compile

# Run target
run:
	$(MVN) exec:java -Dexec.mainClass=$(MAIN_CLASS) -Dexec.args="$(CONFIG_FILE)"

test:
	$(MVN) test

# Phony targets
.PHONY: all clean compile run test
