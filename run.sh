#!/bin/bash

set -e

ant
java -cp bin dk.itu.mario.engine.PlayCustomized $@
