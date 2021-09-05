package com.xabe.choreography.common.infrastructure.repository;

import com.xabe.choreography.common.infrastructure.Entity;
import io.smallrye.mutiny.Uni;
import java.util.List;

public interface Repository<T extends Entity<U>, U> {

  Uni<T> load(U id);

  Uni<List<T>> getAll();

  Uni<T> save(T entity);

  Uni<T> update(T entity);
}
