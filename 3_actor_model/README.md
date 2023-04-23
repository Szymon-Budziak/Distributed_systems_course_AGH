# Actor model using Python Ray

## Dependencies

- Ray
- cProfile

## Files explanation

### ray-lab-1.py

This file demonstrates how to use Ray to run a bubble sort algorithm in parallel. The code creates a list of random
integers and sorts it using the bubble_sort function. The code then uses Ray to run the same function in parallel on
multiple CPUs. Finally, the code shuts down the Ray cluster.

### ray-lab-2.py

This file demonstrates how to use Ray to process large lists and dictionaries in parallel. The code creates a large list
and a large dictionary, and then uses Ray to run functions that sum the elements of these data structures. Finally, the
code shuts down the Ray cluster.

### ray-lab-3.py

This is a code sample for Ray Lab 3. It includes examples of using Ray to invoke remote methods, compute Pi in parallel
using actors and tasks, and modifying the behavior of a remote method.

The file contains the following:

1. Initialization of a MethodStateCounter actor to keep track of method invocations by various callers.
2. Invoking the invoke method of the actor by randomly selecting callers and keeping track of their invocations.
3. Retrieving the state of individual callers and a list of values computed by specified invokers using the
   get_invoker_state and get_values_computed_by_invokers methods of the actor.
4. Retrieving the state of all invokers using the get_all_invoker_state method of the actor.
5. Modifying the invoke method of the actor to return a random integer value between 5 and 25.
6. Demonstrating parallel computation of pi using a combination of actor and task.

## Running docker

To run docker, you have to follow these steps:

1. Create .env file with WORKDIR=/path/to/your/project argument
2. Run and replace *your-image-name* with your image name

```bash
docker build --build-arg WORKDIR=$(cut -d '=' -f 2 .env) -t your-image-name .
```

3. Run and replace *your-image-name* with your image name

```bash
docker run your-image-name
```

4. Run docker compose up:

```bash
docker compose up
```

#### Credits

This project was created by Szymon Budziak.