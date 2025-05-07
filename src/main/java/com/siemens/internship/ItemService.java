package com.siemens.internship;

import com.siemens.internship.validators.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    private final EmailValidator emailValidator;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    // removed processed items
    private AtomicInteger processedCount = new AtomicInteger(0);

    public ItemService() {
        this.emailValidator =  new EmailValidator();
    }


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        // Validate email format
        if(!emailValidator.validate(item.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     *
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        List<CompletableFuture<Item>> futures = new ArrayList<>();
        // we modified the runAsync with the supplyAsync and then collect the results.
        for (Long id : itemIds) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                    // removed thread sleep

                    Item item = itemRepository.findById(id).orElse(null);
                    if (item == null) {
                        return null;
                    }

                    processedCount.incrementAndGet();
                    // logic is the same
                    item.setStatus("PROCESSED");
                    itemRepository.save(item);
                    return item;

            }, executor));
        }

        // collect the results of all futures
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))       // intialise as a CompletableFuture array
                .thenApply(v -> {
                    List<Item> result = new ArrayList<>();
                    for (CompletableFuture<Item> future : futures) {
                        try {
                            Item item = future.get();
                            if (item != null) {
                                result.add(item);
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            // handle exceptions

                            e.printStackTrace();
                        }
                    }
                    return result;
                });
    }

}

