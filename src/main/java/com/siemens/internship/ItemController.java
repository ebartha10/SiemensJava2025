package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);      // switched around the response
        }
        try {
            itemService.save(item);
        } catch (IllegalArgumentException e) {          // try catch for invalid email
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        return new ResponseEntity<>(item, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()) {
            item.setId(id);
            try {
                Item newItem = itemService.save(item);
                return new ResponseEntity<>(newItem, HttpStatus.CREATED);
            } catch (IllegalArgumentException e) {          // try catch for invalid email
                return ResponseEntity.badRequest().body("Invalid email format");
            }
        } else {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);     //changed to OK
    }

    @GetMapping("/process")
    public CompletableFuture<ResponseEntity<List<Item>>> processItems() {       // changed the return to a future
        return itemService.processItemsAsync().thenApply(items -> {
            if (items.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(items, HttpStatus.OK);
        });
    }
}
