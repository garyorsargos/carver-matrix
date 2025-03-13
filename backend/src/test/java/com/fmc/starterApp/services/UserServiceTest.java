package com.fmc.starterApp.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fmc.starterApp.models.entity.PostgresExampleObject;
import com.fmc.starterApp.repositories.PostgresRepository;

class UserServiceTest {


    PostgresRepository postgresRepository = mock(PostgresRepository.class);
    PostGresExampleService service = new PostGresExampleService(postgresRepository);

    @Test
    void shouldInsert() {
        PostgresExampleObject example = new PostgresExampleObject();
        example.setId(12345L);
        example.setName("some record name");

        when(postgresRepository.save(any())).thenReturn(example);

        String actual = service.insertExample(example);
        assertEquals("some record name", actual);
    }

    @Test
    void shouldGetObjects() {
        PostgresExampleObject example = new PostgresExampleObject();
        example.setId(12345L);
        example.setName("some record name");
        when(postgresRepository.findFirstById(12345L)).thenReturn(example);
        PostgresExampleObject result = service.getObjectById(12345L);
        assertEquals("some record name", result.getName());
        assertEquals(12345L, result.getId());
    }

    @Test
    void shouldGetAllObjects() {
        PostgresExampleObject example = new PostgresExampleObject();
        example.setId(12345L);
        example.setName("some record name");

        PostgresExampleObject example2 = new PostgresExampleObject();
        example2.setId(321L);
        example2.setName("example two");

        when(postgresRepository.findAll()).thenReturn(List.of(example, example2));
        List<PostgresExampleObject> result = service.getAllPostGres();

        assertEquals("some record name", result.get(0).getName());
        assertEquals(12345L, result.get(0).getId());

        assertEquals("example two", result.get(1).getName());
        assertEquals(321L, result.get(1).getId());

    }
}