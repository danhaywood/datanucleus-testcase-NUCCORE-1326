package com.danhaywood.datanucleus.testcase.nuccore1326;

import javax.jdo.annotations.*;

@javax.jdo.annotations.PersistenceCapable(
        identityType=IdentityType.APPLICATION,
        schema = "people",
        table = "Person")
public class Person {

    public Person(){}

    public Person(long id)
    {
        this(id, null);
    }
    public Person(long id, String name)
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
