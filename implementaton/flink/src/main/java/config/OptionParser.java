package config;

public class OptionParser<T> {
    ConfigOption<T> bo;

    public OptionParser(ConfigOption<T> bo) {
        this.bo = bo;
    }

    T parse(String value) {
        if (bo.getType() == Integer.class) {
            return (T) new Integer(value);
        } else if (bo.getType() == Long.class) {
            return (T) new Long(value);
        } else if (bo.getType() == Double.class) {
            return (T) new Double(value);
        } else if (bo.getType() == Boolean.class) {
            return (T) new Boolean(value);
        } else {
            return (T) value;
        }
    }
}