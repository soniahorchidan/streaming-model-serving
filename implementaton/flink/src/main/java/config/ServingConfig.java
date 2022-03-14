package config;

import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServingConfig<T> {
    private HashMap<String, ConfigOption<?>> configs = new HashMap<>();

    public ServingConfig(String confFile, List<ConfigOption<?>> defaults) {
        loadDefaults(defaults);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(confFile)));
        Yaml yaml = new Yaml();
        Map<String, Object> values = (Map<String, Object>) yaml.load(reader);

        for (Map.Entry<String, Object> mp : values.entrySet()) {
            ConfigOption<?> b = this.configs.get(mp.getKey());
            if (b != null) {
                String value = mp.getValue().toString();
                OptionParser<?> op = new OptionParser<>(b);
                this.configs.put(mp.getKey(), new ConfigOption<>(mp.getKey(), op.parse(value)));
            } else throw new IllegalArgumentException(
                    "Configuration parameter " + mp.getKey() + " could not be found in default Nd4jDefaultOptions");
        }
    }


    public T getParam(String key) {
        if (this.configs.get(key) == null) {
            throw new IllegalArgumentException("Parameter " + key + " not set in the benchmark configuration");
        }
        return (T) this.configs.get(key).getValue();
    }

    public void printConfiguration() {
        for (Map.Entry<String, ConfigOption<?>> e : configs.entrySet()) {
            System.out.println(e.getKey() + " || " + e.getValue());
        }
    }

    private void loadDefaults(List<ConfigOption<?>> defaults) {
        for (ConfigOption<?> t : defaults) {
            this.configs.put(t.getKey(), t);
        }
    }
}