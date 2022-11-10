#include "/valgrind/include/valgrind.h"
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <semaphore.h>

void traceFunctionCall(const char* format, ...) {
    printf("%ld - Thread: %lu - ",
        time(NULL),
        pthread_self()
    );

    va_list args;
    va_start(args, format);
    vprintf(format, args);

    printf("\n");
}

int I_WRAP_SONAME_FNNAME_ZZ(Za, pthreadZumutexZuinit)
(pthread_mutex_t *mutex, const pthread_mutexattr_t *attr)
{
	OrigFn fn;
	VALGRIND_GET_ORIG_FN(fn);
	int result;
	CALL_FN_W_WW(result, fn, mutex, attr);
	traceFunctionCall("pthread_mutex_init: %p, %p -> %i",mutex, attr, result);
	return result;
}

int I_WRAP_SONAME_FNNAME_ZZ(Za, pthreadZumutexZulock)
(pthread_mutex_t *mutex)
{
	OrigFn fn;
	VALGRIND_GET_ORIG_FN(fn);
	int result;
	CALL_FN_W_W(result, fn, mutex);
	traceFunctionCall("pthread_mutex_lock: %p -> %i",mutex, result);
	return result;
}

int I_WRAP_SONAME_FNNAME_ZZ(Za, pthreadZumutexZuunlock)
(pthread_mutex_t *mutex)
{
	OrigFn fn;
	VALGRIND_GET_ORIG_FN(fn);
	int result;
	CALL_FN_W_W(result, fn, mutex);
	traceFunctionCall("pthread_mutex_unlock: %p -> %i",mutex, result);
	return result;
}

