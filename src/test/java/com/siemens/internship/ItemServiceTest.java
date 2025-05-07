package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        Item item = new Item();
        when(itemRepository.findAll()).thenReturn(Collections.singletonList(item));
        List<Item> result = itemService.findAll();
        assertEquals(1, result.size());
        verify(itemRepository).findAll();
    }

    @Test
    void testFindById() {
        Item item = new Item();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Optional<Item> result = itemService.findById(1L);
        assertTrue(result.isPresent());
        verify(itemRepository).findById(1L);
    }

    @Test
    void testSaveValidEmail() {
        Item item = new Item();
        item.setEmail("test@example.com");
        when(itemRepository.save(item)).thenReturn(item);
        Item saved = itemService.save(item);
        assertEquals(item, saved);
        verify(itemRepository).save(item);
    }

    @Test
    void testSaveInvalidEmail() {
        Item item = new Item();
        item.setEmail("invalid-email");
        assertThrows(IllegalArgumentException.class, () -> itemService.save(item));
        verify(itemRepository, never()).save(any());
    }

    @Test
    void testDeleteById() {
        itemService.deleteById(1L);
        verify(itemRepository).deleteById(1L);
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        Item item1 = new Item();
        item1.setId(1L);
        item1.setEmail("a@a.com");
        Item item2 = new Item();
        item2.setId(2L);
        item2.setEmail("b@b.com");
        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> processed = future.get();
        assertEquals(2, processed.size());
        assertTrue(processed.stream().allMatch(i -> "PROCESSED".equals(i.getStatus())));
        verify(itemRepository, times(2)).save(any(Item.class));
    }
} 