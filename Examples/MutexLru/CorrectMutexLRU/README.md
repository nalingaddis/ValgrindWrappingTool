# lab3-lls3
lab3-lls3 created by GitHub Classroom
#### Locking Protocol for Ex3

##### Give every list node a mutex for easier process. 
##### Create a dummy list head node with mutex when init to protect it and avoid dealing with empty list.
##### Lock the prev node and curr node when calling reference() and clean() so no deadlock will be created.
##### Maintain the least number of lock by only locking the prev node and curr node so that we can make changes to multiple thread at one time.
