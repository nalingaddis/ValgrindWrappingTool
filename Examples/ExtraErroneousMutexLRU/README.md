What we did
- We tried to do the coarse lru, but failed with unknown errors. We think there was a problem with our locks
- We tried to do the fine-grained lru

Approach
- We implemented a coarse mutex lock on the list when calling any of the functions: print, reference, or clean.
- The lock worked by checking the condition variable as well as the done value. We used done here to define whether any threads were not done.
- The lock was released at the end of every function call, and the condition variables signaled

-For the fine-grained lru, we had a lock for each node, and only acquired the lock for the next node if we are currently holding the lock for the current node (in a hand-over-hand method). Essentially, it was like lock coupling. Then, we included the same logic for hooking in the linked list from mutex-lru.c.

