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
static pthread_mutex_t mutex;
static int count = 0;
static pthread_cond_t cv_low, cv_high;

static volatile int done = 0;

/* Initialize the mutex. */
int init (int numthreads) {
    /* Your code here */

    /* Temporary code to suppress compiler warnings about unused variables.
     * You should remove the following lines once this file is implemented.
     */
    list_head = malloc(sizeof(struct list_node));
    list_head->next = NULL;
    pthread_mutex_init(&list_head->mutex, NULL);
    pthread_mutex_init(&mutex, NULL);
    count = 0;
    pthread_cond_init(&cv_low, NULL);
    pthread_cond_init(&cv_high, NULL);
    return 0;
}

/* Return 1 on success, 0 on failure.
 * Should set the reference count up by one if found; add if not.*/
int reference (int key) {
    pthread_mutex_lock(&mutex);
    while (count >= HIGH_WATER_MARK && !done) {
        pthread_cond_wait(&cv_high, &mutex);
    }

    if (done) {
        pthread_cond_broadcast(&cv_low);
        pthread_cond_broadcast(&cv_high);
        pthread_mutex_unlock(&mutex);
        return 0;
    }

    pthread_mutex_unlock(&mutex);

    int found = 0;

    pthread_mutex_lock(&list_head->mutex);
    struct list_node* cursor = list_head;
    while(cursor->next) {
        struct list_node* next = cursor->next;
        pthread_mutex_lock(&next->mutex);
        if (next->key < key) {
            pthread_mutex_unlock(&cursor->mutex);
            cursor = next;
        } else {
            if (next->key == key) {
                next->refcount++;
                found++;
            } 
            pthread_mutex_unlock(&next->mutex);
            break;
        }
    }

    if (!found) {
        // Handle 2 cases: the list is empty/we are trying to put this at the
        // front, and we want to insert somewhere in the middle or end of the
        // list.
        struct list_node* new_node = malloc(sizeof(struct list_node));
        if (!new_node) {
            pthread_mutex_unlock(&cursor->mutex);
            return 0;
        }

        pthread_mutex_lock(&mutex);
        count++;
        pthread_mutex_unlock(&mutex);

        new_node->key = key;
        new_node->refcount = 1;
        new_node->next = cursor->next;
        pthread_mutex_init(&new_node->mutex, NULL);

        cursor->next = new_node;

    }

    pthread_mutex_unlock(&cursor->mutex);

    // printf("end refernce %d\n", key);
    pthread_mutex_lock(&mutex);
    if (count > LOW_WATER_MARK) {
        pthread_cond_broadcast(&cv_low);
    }
    pthread_mutex_unlock(&mutex);

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
    pthread_mutex_lock(&mutex);
    if (check_water_mark) {
        while (count <= LOW_WATER_MARK && !done) {
            pthread_cond_wait(&cv_low, &mutex);
        }
    }

    if (done) {
        pthread_cond_broadcast(&cv_low);
        pthread_cond_broadcast(&cv_high);
        pthread_mutex_unlock(&mutex);
        return;
    }

    pthread_mutex_unlock(&mutex);

    pthread_mutex_lock(&list_head->mutex);

    struct list_node* cursor = list_head;
    while(cursor->next) {
        struct list_node* next = cursor->next;
        pthread_mutex_lock(&next->mutex);
        next->refcount--;
        if (next->refcount == 0) {
            cursor->next = next->next;
            pthread_mutex_unlock(&next->mutex);
            free(next);

            pthread_mutex_lock(&mutex);
            count --;
            pthread_mutex_unlock(&mutex);
        } else {
            pthread_mutex_unlock(&cursor->mutex);
            cursor = cursor->next;
        }
    }

    pthread_mutex_unlock(&cursor->mutex);

    pthread_mutex_lock(&mutex);
    if (count < HIGH_WATER_MARK) {
        pthread_cond_broadcast(&cv_high);
    }
    pthread_mutex_unlock(&mutex);

}


/* Optional shut-down routine to wake up blocked threads.
   May not be required. */
void shutdown_threads (void) {
    pthread_mutex_lock(&mutex);
    done = 1;
    pthread_mutex_unlock(&mutex);
    pthread_cond_broadcast(&cv_low);
    pthread_cond_broadcast(&cv_high);
    return;
}

/* Print the contents of the list.  Mostly useful for debugging. */
void print (void) {
    pthread_mutex_lock(&mutex);
    printf("=== Starting list print ===\n");
    printf("=== Total count is %d ===\n", count);

    pthread_mutex_lock(&list_head->mutex);

    struct list_node* cursor = list_head;
    while(cursor->next) {
        struct list_node* next = cursor->next;
        pthread_mutex_lock(&next->mutex);
        pthread_mutex_unlock(&cursor->mutex);

        printf ("Key %d, Ref Count %d\n", next->key, next->refcount);
        cursor = cursor->next;
    }

    pthread_mutex_unlock(&cursor->mutex);

    printf("=== Ending list print ===\n");
    pthread_mutex_unlock(&mutex);

}

