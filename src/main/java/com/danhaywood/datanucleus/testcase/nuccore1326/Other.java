package com.danhaywood.datanucleus.testcase.nuccore1326;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PrimaryKey;

@javax.jdo.annotations.PersistenceCapable(
        identityType= IdentityType.APPLICATION,
        table = "Other")
public class Other {

    public Other(){}

    public Other(long id)
    {
        this(id, null);
    }
    public Other(long id, String name)
    {
        this.id = id;
        this.name = name;
    }

    @PrimaryKey
    Long id;
    public Long getId()
    {
        return id;
    }

    String name;
    public String getName()
    {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

}
