package app.daos;

import java.util.Set;

public interface IDAO <T> {
    void create(T t);
    Set<T> getAll();
    T getByID(int id);
    T update(T t);
    int delete(T t);
}
