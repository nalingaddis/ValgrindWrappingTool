/* -*- mode:c; c-file-style:"k&r"; c-basic-offset: 4; tab-width:4; indent-tabs-mode:nil; mode:auto-fill; fill-column:78; -*- */
/* vim: set ts=4 sw=4 et tw=78 fo=cqt wm=0: */

/* @* Place your name here, and any other comments *@
 * @* that deanonymize your work inside this syntax *@
 * @* Matthew Cahill and Raj Gandecha *@
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
    pthread_mutex_init(&mutex, NULL);
    pthread_cond_init(&cv_low, NULL);
    pthread_cond_init(&cv_high, NULL);

    struct list_node* start = malloc(sizeof(struct list_node));
    pthread_mutex_t mutex2;
    pthread_mutex_init(&mutex2, NULL);
    start->mutex = mutex2;
    list_head = start;

    count = 0;
    /* Temporary code to suppress compiler warnings about unused variables.
     * You should remove the following lines once this file is implemented.
     */
    
    /* End temporary code */
    return 0;
}

/* Return 1 on success, 0 on failure.
 * Should set the reference count up by one if found; add if not.*/
int reference (int key) {
    /* Your code here */

    struct list_node* head = list_head;
    //aquire head's lock 
    pthread_mutex_lock(&(head->mutex));
    struct list_node* pred = head;
    if (pred->next) {
        struct list_node* curr = pred->next;
        //aquire curr's lock
        pthread_mutex_lock(&(curr->mutex));

        while (curr->next) {
            if(curr->key == key) {
                break;
            }

            //unlock the pred 
            pthread_mutex_unlock(&(pred->mutex));
            pred = curr;
            curr = curr->next;
            pthread_mutex_lock(&(curr->mutex));
        }

        if(curr) {
            //node aquired
            curr->refcount++;
            pred->next = curr->next;
            curr->next = list_head->next;
            list_head->next = curr;
        } else {
            //node not in list
            struct list_node* new = malloc(sizeof(struct list_node));
            new->key = key;
            new->refcount = 1;
            new->next = list_head->next;
            pthread_mutex_t mutex3;
            pthread_mutex_init(&mutex3, NULL);
            new->mutex = mutex3;

            list_head->next = new;

            count++;

        }

        //unlock curr
        pthread_mutex_unlock(&(curr->mutex));
    }

    //unlock pred 
    pthread_mutex_unlock(&pred->mutex);

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

     if (check_water_mark) {
        while (count < LOW_WATER_MARK) {}
    }

    struct list_node* head = list_head;
    //aquire head's lock 
    pthread_mutex_lock(&(head->mutex));
    struct list_node* pred = head;

    if (pred->next) {
        struct list_node* curr = pred->next;
        //aquire curr's lock
        pthread_mutex_lock(&(curr->mutex));

        while (curr->next) {

            // if ref == 0, remove
            if(curr->refcount == 0) {
                pred->next = curr->next;
                count--;
            } else {
                //dec ref
                curr->refcount--;
            }

            //unlock the pred 
            pthread_mutex_unlock(&(pred->mutex));
            pred = curr;
            curr = curr->next;
            pthread_mutex_lock(&(curr->mutex));
        }

        //unlock curr
        pthread_mutex_unlock(&(curr->mutex));
    }

    //unlock pred 
    pthread_mutex_unlock(&pred->mutex);

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

    // ACQUIRE LOCK
    pthread_mutex_lock(&mutex);
    while (!done) {
        //pthread_cond_wait(&cv_low, &mutex);
    }
    done = 0;

    printf("=== Starting list print ===\n");
    printf("=== Total count is %d ===\n", count);
    struct list_node* cursor = list_head;
    while(cursor) {
        printf ("Key %d, Ref Count %d\n", cursor->key, cursor->refcount);
        cursor = cursor->next;
    }
    printf("=== Ending list print ===\n");

    // RELEASE LOCK
    done = 1;
    pthread_mutex_unlock(&mutex);
}
