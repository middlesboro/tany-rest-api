package sk.tany.rest.api.domain;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

@Slf4j
public abstract class AbstractInMemoryRepository<T> {

    protected final Nitrite nitrite;
    protected final Class<T> type;
    protected final Map<String, T> memoryCache = new ConcurrentHashMap<>();
    protected ObjectRepository<T> repository;

    protected AbstractInMemoryRepository(Nitrite nitrite, Class<T> type) {
        this.nitrite = nitrite;
        this.type = type;
    }

    @PostConstruct
    public void init() {
        this.repository = nitrite.getRepository(type);
        log.info("Loading {} into memory...", type.getSimpleName());
        for (T entity : repository.find()) {
            String id = getId(entity);
            if (id != null) {
                memoryCache.put(id, entity);
            }
        }
        log.info("Loaded {} {} entities.", memoryCache.size(), type.getSimpleName());
    }

    public List<T> findAll() {
        return new ArrayList<>(memoryCache.values());
    }

    public Page<T> findAll(Pageable pageable) {
        List<T> all = findAll();
        if (pageable.getSort().isSorted()) {
            sort(all, pageable.getSort());
        }

        if (pageable.isUnpaged()) {
            return new PageImpl<>(all, pageable, all.size());
        }
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), all.size());
        if (start > all.size()) {
             return new PageImpl<>(List.of(), pageable, all.size());
        }
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }

    public Optional<T> findById(String id) {
        return Optional.ofNullable(memoryCache.get(id));
    }

    public List<T> findAllById(Iterable<String> ids) {
        if (ids == null) return List.of();
        return StreamSupport.stream(ids.spliterator(), false)
                .map(memoryCache::get)
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    public boolean existsById(String id) {
        return memoryCache.containsKey(id);
    }

    public T save(T entity) {
        String id = getId(entity);

        if (id == null) {
            id = UUID.randomUUID().toString();
            setId(entity, id);
            setCreatedDate(entity);
        }
        setLastModifiedDate(entity);

        memoryCache.put(id, entity);

        repository.update(entity, true);

        return entity;
    }

    public void saveAll(Iterable<T> entities) {
        for (T entity : entities) {
            save(entity);
        }
    }

    public void deleteById(String id) {
        if (id != null) {
            memoryCache.remove(id);
            repository.remove(ObjectFilters.eq("id", id));
        }
    }

    public void deleteAll() {
        memoryCache.clear();
        repository.remove(ObjectFilters.ALL);
    }

    public void delete(T entity) {
        String id = getId(entity);
        if (id != null) {
            deleteById(id);
        }
    }

    public long count() {
        return memoryCache.size();
    }

    // Helper methods using Reflection to access common fields without a base class

    private String getId(T entity) {
        try {
            Field field = type.getDeclaredField("id");
            field.setAccessible(true);
            return (String) field.get(entity);
        } catch (Exception e) {
            // Try superclass if needed, or assume standard field exists
             try {
                Field field = type.getSuperclass().getDeclaredField("id");
                field.setAccessible(true);
                return (String) field.get(entity);
            } catch (Exception ex) {
                log.error("Could not get ID for {}", type.getSimpleName(), ex);
                return null;
            }
        }
    }

    private void setId(T entity, String id) {
        try {
            Field field = type.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            try {
                Field field = type.getSuperclass().getDeclaredField("id");
                field.setAccessible(true);
                field.set(entity, id);
            } catch (Exception ex) {
                 log.error("Could not set ID for {}", type.getSimpleName(), ex);
            }
        }
    }

    private void setCreatedDate(T entity) {
        try {
            Field field = type.getDeclaredField("createdDate"); // Common naming convention
            field.setAccessible(true);
            if (field.get(entity) == null) {
                field.set(entity, Instant.now());
            }
        } catch (NoSuchFieldException e) {
             try {
                Field field = type.getDeclaredField("createDate"); // Alternate naming
                field.setAccessible(true);
                if (field.get(entity) == null) {
                    field.set(entity, Instant.now());
                }
            } catch (Exception ex) {
                // Ignore if field doesn't exist
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    private void setLastModifiedDate(T entity) {
        try {
            Field field = type.getDeclaredField("updateDate"); // Common naming
            field.setAccessible(true);
            field.set(entity, Instant.now());
        } catch (NoSuchFieldException e) {
             try {
                Field field = type.getDeclaredField("lastModifiedDate"); // Alternate naming
                field.setAccessible(true);
                field.set(entity, Instant.now());
            } catch (Exception ex) {
                 // Ignore
            }
        } catch (Exception e) {
             // Ignore
        }
    }

    protected void sort(List<T> list, Sort sort) {
        if (sort.isUnsorted()) {
            return;
        }

        Comparator<T> comparator = null;

        for (Sort.Order order : sort) {
            Comparator<T> currentComparator = (o1, o2) -> {
                Object v1 = getFieldValue(o1, order.getProperty());
                Object v2 = getFieldValue(o2, order.getProperty());

                if (v1 == null && v2 == null) {
                    return 0;
                } else if (v1 == null) {
                    return order.getNullHandling() == Sort.NullHandling.NULLS_FIRST ? -1 : 1;
                } else if (v2 == null) {
                    return order.getNullHandling() == Sort.NullHandling.NULLS_FIRST ? 1 : -1;
                }

                if (v1 instanceof Comparable && v2 instanceof Comparable) {
                    int result;
                    if (order.isIgnoreCase() && v1 instanceof String && v2 instanceof String) {
                        result = ((String) v1).compareToIgnoreCase((String) v2);
                    } else {
                        // Unchecked cast is safe because of check above
                        result = ((Comparable) v1).compareTo(v2);
                    }
                    return result;
                }
                return 0;
            };

            if (order.isDescending()) {
                currentComparator = currentComparator.reversed();
            }

            if (comparator == null) {
                comparator = currentComparator;
            } else {
                comparator = comparator.thenComparing(currentComparator);
            }
        }

        if (comparator != null) {
            list.sort(comparator);
        }
    }

    private Object getFieldValue(T entity, String fieldName) {
        try {
            Field field = getField(type, fieldName);
            field.setAccessible(true);
            return field.get(entity);
        } catch (Exception e) {
            log.warn("Could not get value for field {} on {}", fieldName, type.getSimpleName());
            return null;
        }
    }

    private Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                return getField(superclass, fieldName);
            }
            throw new RuntimeException(e);
        }
    }
}
