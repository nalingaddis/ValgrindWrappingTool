/* -*- mode:c; c-file-style:"k&r"; c-basic-offset: 4; tab-width:4; indent-tabs-mode:nil; mode:auto-fill; fill-column:78; -*- */
/* vim: set ts=4 sw=4 et tw=78 fo=cqt wm=0: */

/* @* Place your name here, and any other comments *@
 * @* that deanonymize your work inside this syntax *@
 * @* Matthew Cahill and Raj Gandecha *@
 */

/* for this exercise, it is sufficient to have one lock for the entire list. 
 * we recommend using pthread mutex functions, 
 * including pthread_mutex_init, pthread_mutex_lock, and pthread_mutex_unlock.
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
};

static struct list_node* list_head = NULL;

/* A static mutex */
static pthread_mutex_t mutex;
static int count = 0;
static pthread_cond_t cv_low, cv_high; // TODO rename these for clarity

static volatile int done = 1;

/* Initialize the mutex. */
int init (int numthreads) {
    /* Your code here */
    pthread_mutex_init(&mutex, NULL);
    pthread_cond_init(&cv_low, NULL);
    pthread_cond_init(&cv_high, NULL);

    struct list_node* start = malloc(sizeof(struct list_node));
    list_head = start;

    count = 0;
    return 0;
}

/* Return 1 on success, 0 on failure.
 * Should set the reference count up by one if found; add if not.*/
int reference (int key) {
    /* Your code here */

    // ACQUIRE LOCK
    pthread_mutex_lock(&mutex);
    while (!done) {
        pthread_cond_wait(&cv_low, &mutex);
    }
    done = 0;

    // CRITICAl SECTION
    struct list_node* node = list_head->next;
    struct list_node* parent = list_head;
    
    while(node) {

        if (node->key == key) {
            break;
        }

        parent = node;
        node = node->next;
    } 

    if (node) {
        // node acquired, inc refcount and set to beginning of list
        node->refcount++;

        parent->next = node->next;
        node->next = list_head->next;
        list_head->next = node;
    } else {
        // node not in list, add it to beginning and set refcount to 1;
        struct list_node* new = malloc(sizeof(struct list_node));
        new->key = key;
        new->refcount = 1;
        new->next = list_head->next;

        list_head->next = new;

        count++;
    }

    // RELEASE LOCK
    done = 1;
    pthread_mutex_unlock(&mutex);
    //pthread_cond_broadcast(&cv_low);
    //pthread_cond_broadcast(&cv_high);


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

    // ACQUIRE LOCK
    pthread_mutex_lock(&mutex);
    while (!done) {
        pthread_cond_wait(&cv_high, &mutex);
    }
    done = 0;

    // CRITICAL SECTION

    // pass through all elements
    struct list_node* node = list_head->next;
    struct list_node* parent = list_head;
    
    while(node) {
    
        // if ref == 0, remove
        if (node->refcount == 0) {
            parent->next = node->next;
            count--;
        } else {
            // dec ref 
            node->refcount--;
        }
        
        parent = node;
        node = node->next;
    }

    // RELEASE LOCK
    done = 1;
    pthread_mutex_unlock(&mutex);
    //pthread_cond_broadcast(&cv_low);
    //pthread_cond_broadcast(&cv_high);
}

/* Optional shut-down routine to wake up blocked threads.
   May not be required. */
void shutdown_threads (void) {
    /* Your code here */

    // Idk why but this is optional so leave it for now I guess

    return;
}

/* Print the contents of the list.  Mostly useful for debugging. */
void print (void) {

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
    //pthread_cond_broadcast(&cv_low);
    //pthread_cond_broadcast(&cv_high);
}
