package me.ryanhamshire.GriefPrevention.config;

public class ConfigOption<T>
{
    T value;
    String key;

    ConfigOption(String key, T value)
    {
        this.key = key;
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }
}
