#!/bin/bash

# gcc compiler
apt install build-essential -y

# valgrind
DEBIAN_FRONTEND=noninteractive apt install g++ valgrind -y
apt install automake -y

# git
DEBIAN_FRONTEND=noninteractive apt install git-all -y

# valgrind source code
git clone git://sourceware.org/git/valgrind.git \
    && cd valgrind \
    && ./autogen.sh \
    && ./configure \
    && make