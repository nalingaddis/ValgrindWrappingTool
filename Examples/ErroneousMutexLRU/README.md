# lab3-kirby_and_ruchi
lab3-kirby_and_ruchi created by GitHub Classroom

Authors: Matthew Kirby, Ruchi Sarkar 

We struggled on lab 3, so in this README we wanted to outline our thought processes for each exercise and what we were trying to achieve. We also had some trouble getting into office hours, so we just tried to follow ideas from the lectures. 

Ex1: We tried to implement coarse-grain locking by just locking all of the code in reference() and all of the code in clean(). By locking the whole function, we were trying to ensure that only one thread modifies the list at a time. 

Ex2: With the condition variables, we tried to implement it similar to an example from lecture. For reference(), we tried to have the condition variable wait as long as count was above the HIGH_WATER_MARK. For clean(), we tried to wait as long as count was below the LOW_WATER_MARK. We then implemented reference() and clean() the same way as ex1, except we signalled our condition variables immediately after changing the value of count. Our rationale was that once count was modified, we would want to alert any threads waiting on the condition variables. 

For our unit tests, we tried to implement a test that referenced each element once and cleaned all of them, and another that referenced each 3rd element twice. 

Ex3: We had the most trouble with this one. Our understanding was that we needed a global lock to protect count, and we needed a lock to protect each modification to a node. In reference(), our node-specific lock would lock while ref_count was being incremented, and in clean(), it would lock while ref_count was being decremented - in both cases, it'd release the lock immediately after changing ref_count. In reference(), our global lock locked when count was being incremented, and released the lock immediately after. clean() was similar but it locked when count was being decremented.

We then tried to add condition variable support similar to what we did in ex2. Our reasoning is that we still need the condition variables to wait on the global lock when count is not between the HIGH_WATER_MARK and LOW_WATER_MARK, so we mimicked the while loop format that we used for ex2. We also signal the condition variables when the value of count has changed. We chose to use the global lock in the call to wait() because that is the lock we use to protect the value of count. 
