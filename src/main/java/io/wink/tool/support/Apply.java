package io.wink.tool.support;

@FunctionalInterface
public interface Apply<T> {
    void apply(T t);
}
