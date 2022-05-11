<p align="center">
  <img src="https://user-images.githubusercontent.com/80321222/167834448-ded706c1-1570-46e4-b9a5-083660769ea8.png" />
</p>

# Description
GPUP is a platform that utlizes task managment given by managers to workers and processing those tasks on the worker side.
The platform is managed from two desktop applications - Admin application and Worker application, one for task creation, and the other for task execution.

# About GPUP
### The fundamental idea that stands in GPUP's core is based on two main components:

### 1. The management of a dependency graph of various data objects:

Think about a graph that contains nodes that are connected to other nodes.
Each node in the graph is called a **target**.
The target has a name and it can carry free information that can be used by the system
in the future.
Each target can depend on other targets, or other targets can depend on it. 
(The dependencies are modeled as connections in a graph)
* Targets that do not depend on any other targets are called **Leafs**.
* Targets that both depend on other targets and that other targets
  depends on them are called **Middles**.
* Targets that do not depend on any other targets are called **Roots**.
* Targets that do not depend on any other targets and no target depend on them are called **Independents**.

Here is an example of a dependency graph:

![image](https://user-images.githubusercontent.com/80321222/167838920-e33299a0-8bae-414a-8f84-a760eef8adef.png)

Given a dependency graph **GPUP** offers the user a variety of tools to analyze and gain insights on the graph:
* A quick observation of each target's status ( Leaf | Middle | Root | Independent ), which targets it depends on, which targets depend on it, direct and indirect dependencies.
* Finding paths between two targets.
* Finding circles in the graph.
* What if scenarios - finding which targets are affected from / depends on a specific target.


### 2. Executing Tasks on a dependency graph:

A **Task** is a piece of code that operates on a target and on the information that is stored in it.
The task gets the infromation in the graph as an input and does something with it.

The task execution model in **GPUP** works in Bottom-Up form:
First the system runs the tasks on all the targets that do not depend on any other target, that will of course include the Leafs and the Independets.
Each task can finish with the status : Success/ Warning (success with warning)/ Failure.
The moment a task finished running successfully on a target ( with or without a warning) the target removes itself
as a blockage for the other targets that depend on it.
The moment when all the targets that a target depends on finish running successfully - then a target is marked ready for processing the task on it.

Naturally, in the start of each task run, there are many candidates that the task to run on,
in practice there is no prevention that the task will run on several targets simultaneously.

# GPUP's abillities

## Admin

* Adding a target graph to the system (An xml file).
* Getting details about the graph - the amount of targets from each type, the payment the worker gets for each target they execute etc.
* Viewing the graph in a Table view/ Graphic view (GraphViz)/ Tree view.
* Finding a path between two targets. 
* Finding all the dependencies of a single target.
* Creating a task - choosing a type of task to run (Simulation/ Compilation) on a group of targets the admin chooses, and uploading the task to the server for the workers to assign to.
* Following the progress of running tasks, and controlling tasks uploaded by the admin (pausing, stopping, and resuming tasks).

## Worker

* Login page where the worker picks a unique user name and the amount of threads that he wants to allocate for jobs on the current login.
* Viewing details about the worker history - the amount of tasks that he assigned to, the amount of targets that he finished running on, and the current balance of money the worker earned so far.
* Viewing details about a task from the online tasks - task progress, the amount of targets in the task, the payment for each target execution, etc.
* Viewing the list of online tasks that the worker is assinged to, and the target history of the worker.
