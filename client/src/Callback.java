package client;

@FunctionalInterface
public interface Callback {
    void callback(Object... args);
}
