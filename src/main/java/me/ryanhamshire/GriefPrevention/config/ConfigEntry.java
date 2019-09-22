package me.ryanhamshire.GriefPrevention.config;

public class ConfigEntry<T>
{
    T value;
    String key;

    ConfigEntry(String key, T value)
    {
        this.key = key;
        this.value = value;
    }

    public T getValue()
    {
        return value;
    }
}
