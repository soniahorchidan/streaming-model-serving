package config;

public class ConfigOption<T> {
    private String key;
    private T value;
    private Class<T> type;

    public ConfigOption(String key, T value) {
        this.key = key;
        this.value = value;
        this.type = (Class<T>) value.getClass();
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "BenchmarkOption{" +
               "key='" + key + '\'' +
               ", value=" + value +
               ", type=" + type +
               '}';
    }
}