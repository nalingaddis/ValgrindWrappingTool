all: lru-sequential lru-mutex lru-fine

CFLAGS = -g -Wall -Werror -pthread

%.o: %.c *.h
	gcc $(CFLAGS) -c -o $@ $<

lru-sequential: main.c sequential-lru.o
	gcc $(CFLAGS) -o lru-sequential sequential-lru.o main.c

lru-mutex: main.c mutex-lru.o
	gcc $(CFLAGS) -o lru-mutex mutex-lru.o main.c

lru-fine: main.c fine-lru.o
	gcc $(CFLAGS) -o lru-fine fine-lru.o main.c

update:
	git checkout master
	git pull https://github.com/comp530-f20/lab3.git master

clean:
	rm -f *~ *.o lru-sequential lru-mutex lru-rw lru-fine lru-fine-wrapped lru-mutex-wrapped

wrapped: lru-mutex-wrapped lru-fine-wrapped

lru-mutex-wrapped: main.c mutex-lru.o ValgrindWrapper.o
	gcc $(CFLAGS) -o lru-mutex-wrapped mutex-lru.o ValgrindWrapper.o main.c

lru-fine-wrapped: main.c fine-lru.o ValgrindWrapper.o
	gcc $(CFLAGS) -o lru-fine-wrapped fine-lru.o ValgrindWrapper.o main.c
