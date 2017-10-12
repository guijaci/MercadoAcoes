package edu.utfpr.guilhermej.sd1.av2.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Representa um acionista, contendo um identificador universal único e um nome. Cada cliente deve ser relacionado à um
 */
public class Stockholder implements Serializable{
    protected Long version = 0L;
    private final UUID id;
    private String name;

    /**
     * Constroi um novo acionista, inserindo um identificador único universal gerado aleatoriamente
     */
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

    /**
     * Compara os nomes e os IDs de dois acionistas para saber se são iguais
     * @param obj possível mesmo acionista
     * @return true se iguais, false caso contrario
     */
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
