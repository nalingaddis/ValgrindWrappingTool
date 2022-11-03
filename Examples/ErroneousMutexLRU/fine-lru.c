/* -*- mode:c; c-file-style:"k&r"; c-basic-offset: 4; tab-width:4; indent-tabs-mode:nil; mode:auto-fill; fill-column:78; -*- */
/* vim: set ts=4 sw=4 et tw=78 fo=cqt wm=0: */

/* @* Place your name here, and any other comments *@
 * @* that deanonymize your work inside this syntax *@
 */

#include <stddef.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include "lru.h"

/* Define the simple, singly-linked list we are going to use for tracking lru */
struct list_node {
	struct list_node* next;
	int key;
	int refcount;
	// Protects this node's contents
	pthread_mutex_t mutex;
};

static struct list_node* list_head = NULL;

/* A static mutex; protects the count and head.
 * XXX: We will have to tolerate some lag in updating the count to avoid
 * deadlock. */
static pthread_mutex_t mutex_count;
static pthread_mutex_t mutex_node;
static int count = 0;
static pthread_cond_t cv_low, cv_high;

static volatile int done = 0;

/* Initialize the mutex. */
int init (int numthreads) {
	/* Your code here */
	pthread_mutex_init(&mutex_count, NULL);
	pthread_mutex_init(&mutex_node, NULL);
	/* Temporary code to suppress compiler warnings about unused variables.
	 * You should remove the following lines once this file is implemented.
	 */
	(void)cv_low;
	(void)cv_high;
	/* End temporary code */
	return 0;
}

/* Return 1 on success, 0 on failure.
 * Should set the reference count up by one if found; add if not.*/
int reference (int key) {
	/* Your code here */
	while(count > HIGH_WATER_MARK){
	    pthread_cond_wait(&cv_high, &mutex_count);
	}
	int found = 0;
	struct list_node* cursor = list_head;
	struct list_node* last = NULL;


	while(cursor) {
		if (cursor->key < key) {
			last = cursor;
			cursor = cursor->next;
		} else {
			if (cursor->key == key) {
				pthread_mutex_lock(&mutex_node);
				cursor->refcount++;
				found++;
				pthread_cond_signal(&cv_low);
				pthread_mutex_unlock(&mutex_node);

			}
			break;
		}
	}

	if (!found) {
		// Handle 2 cases: the list is empty/we are trying to put this at the
		// front, and we want to insert somewhere in the middle or end of the
		// list.
		struct list_node* new_node = malloc(sizeof(struct list_node));
		if (!new_node) return 0;
		pthread_mutex_lock(&mutex_count);
		count++;
		pthread_mutex_unlock(&mutex_count);
		new_node->key = key;
		new_node->refcount = 1;
		new_node->next = cursor;

		if (last == NULL)
			list_head = new_node;
		else
			last->next = new_node;
	}
	return 1;
}

/* Do a pass through all elements, either decrement the reference count,
 * or remove if it hasn't been referenced since last cleaning pass.
 *
 * check_water_mark: If 1, block until there are more elements in the cache
 * than the LOW_WATER_MARK.  This should only be 0 during self-testing or in
 * single-threaded mode.
 */
void clean(int check_water_mark) {
	/* Your code here */
	struct list_node* cursor = list_head;
	struct list_node* last = NULL;
	while(check_water_mark && count < LOW_WATER_MARK){
		pthread_cond_wait(&cv_low, &mutex_count);
	}
	while(cursor) {

		pthread_mutex_lock(&mutex_node);
		cursor->refcount--;
		pthread_mutex_unlock(&mutex_node);
		if (cursor->refcount == 0) {
			struct list_node* tmp = cursor;
			if (last) {
				last->next = cursor->next;
			} else {
				list_head = cursor->next;
			}
			tmp = cursor->next;
			free(cursor);
			cursor = tmp;
			pthread_mutex_lock(&mutex_count);
			count--;
			pthread_cond_signal(&cv_high);
			pthread_mutex_unlock(&mutex_count);
		} else {
			last = cursor;
			cursor = cursor->next;
		}
	}
}


/* Optional shut-down routine to wake up blocked threads.
   May not be required. */
void shutdown_threads (void) {
	/* Your code here */
	return;
}

/* Print the contents of the list.  Mostly useful for debugging. */
void print (void) {
	/* Your code here */
	pthread_mutex_lock(&mutex_count);
	printf("=== Starting list print ===\n");
	printf("=== Total count is %d ===\n", count);
	struct list_node* cursor = list_head;
	while(cursor) {
		printf ("Key %d, Ref Count %d\n", cursor->key, cursor->refcount);
		cursor = cursor->next;
	}
	printf("=== Ending list print ===\n"); 

	pthread_mutex_unlock(&mutex_count);
}
