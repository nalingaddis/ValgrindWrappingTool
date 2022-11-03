#include "/valgrind/include/valgrind.h"
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>

void trace(const char* format, ...) {
    printf("%ld - Thread: %lu - ",
        time(NULL),
        pthread_self()
    );

    va_list args;
    va_start(args, format);
    vprintf(format, args);

    printf("\n");
}

int I_WRAP_SONAME_FNNAME_ZZ(libpthreadZdsoZd0, pthreadZucreate)
(pthread_t *restrict thread, const pthread_attr_t *restrict attr, void *(*start_routine)(void *), void *restrict arg)
{
	OrigFn fn;
	VALGRIND_GET_ORIG_FN(fn);
	int result;
	CALL_FN_W_WWWW(result, fn, thread, attr, start_routine, arg);
	trace("pthread_create: %p, %p, %p, %p -> %i",thread, attr, start_routine, arg, result);
	return result;
}

int I_WRAP_SONAME_FNNAME_ZZ(libpthreadZa, pthreadZujoin)
(pthread_t thread, void **retval)
{
	OrigFn fn;
	VALGRIND_GET_ORIG_FN(fn);
	int result;
	CALL_FN_W_WW(result, fn, thread, retval);
	trace("pthread_join: %p, %p -> %i",&thread, retval, result);
	return result;
}

int I_WRAP_SONAME_FNNAME_ZZ(Za, pthreadZumutexZuinit)
(pthread_mutex_t *mutex, const pthread_mutexattr_t *attr)
{
	OrigFn fn;
	VALGRIND_GET_ORIG_FN(fn);
	int result;
	CALL_FN_W_WW(result, fn, mutex, attr);
	trace("pthread_mutex_init: %p, %p -> %i",mutex, attr, result);
	return result;
}

int I_WRAP_SONAME_FNNAME_ZZ(Za, pthreadZumutexZulock)
(pthread_mutex_t *mutex)
{
	OrigFn fn;
	VALGRIND_GET_ORIG_FN(fn);
	int result;
	CALL_FN_W_W(result, fn, mutex);
	trace("pthread_mutex_lock: %p -> %i",mutex, result);
	return result;
}

int I_WRAP_SONAME_FNNAME_ZZ(Za, pthreadZumutexZuunlock)
(pthread_mutex_t *mutex)
{
	OrigFn fn;
	VALGRIND_GET_ORIG_FN(fn);
	int result;
	CALL_FN_W_W(result, fn, mutex);
	trace("pthread_mutex_unlock: %p -> %i",mutex, result);
	return result;
}

