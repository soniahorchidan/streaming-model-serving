package config.defaults;

import config.ConfigOption;

import java.util.LinkedList;
import java.util.List;

public class ServingDefaultOptions {

    public static final List<ConfigOption<?>> configs = new LinkedList<>();

    static {
        configs.add(new ConfigOption<>("batch.size", 0));
        configs.add(new ConfigOption<>("model.path", ""));
        configs.add(new ConfigOption<>("initial.model.path", ""));
        configs.add(new ConfigOption<>("model.type", ""));
    }
}