package edu.utfpr.guilhermej.sd1.av2.model;

import java.io.Serializable;
import java.util.UUID;

public class Stockholder implements Serializable{
    protected Long version = 0L;
    private final UUID id;
    private String name;

    public Stockholder() {
        id = UUID.randomUUID();
    }

    public Long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public Stockholder setName(String name) {
        synchronized (version) {
            this.name = name;
            version++;
        }
        return this;
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(!Stockholder.class.isInstance(obj))
            return false;
        Stockholder other = Stockholder.class.cast(obj);

        if(!id.equals(other.id))
            return false;
        if(!name.equalsIgnoreCase(other.name))
            return false;

        return true;
    }
}
