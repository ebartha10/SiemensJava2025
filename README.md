## Hi there 👋


## Siemens Java Internship - Code Refactoring Project

This repository contains a Spring Boot application that implements a simple CRUD system with some asynchronous processing capabilities. The application was created by a development team in a hurry and while it implements all required features, the code quality needs significant improvement.

## Features
- Created an email validator and used it in the service
## Bugfixes
- Changed many return statuses to the correct ones for CREATE, UPDATE, DELETE
## Tests
- Added unit tests for ItemService
## Refactors - processItems
- Used thread safe variable AtomicInteger
- Made the processItems method return a CompletableFuture
- Changed the runAsync to supplyAsync
- Returned the list once all the items are processed

The initial implementation had a few problems as follows:
- The processItems method was not thread safe, as it used a non-thread-safe variable to keep track of the number of processed items.
- The processItems method was returning directly after calling the runAsync method, which meant that it was not waiting for the processing to complete before returning.
- There was no way to know when the processing was complete, as the tasks were not kept track of.
