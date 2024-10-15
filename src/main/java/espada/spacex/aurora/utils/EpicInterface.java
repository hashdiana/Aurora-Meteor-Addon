package espada.spacex.aurora.utils;

@FunctionalInterface
public interface EpicInterface<T, E> {
    E get(T t);
}
