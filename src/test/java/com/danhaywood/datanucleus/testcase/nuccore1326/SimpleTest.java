package com.danhaywood.datanucleus.testcase.nuccore1326;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import org.apache.log4j.PropertyConfigurator;
import org.datanucleus.util.NucleusLogger;
import org.junit.Test;

import static org.junit.Assert.fail;

public class SimpleTest
{

    static {
        PropertyConfigurator.configure(SimpleTest.class.getResource("logging.properties"));
    }

    @Test
    public void testSimple()
    {
        NucleusLogger.GENERAL.info(">> test START");
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("NUCCORE-1326");

        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try
        {
            tx.begin();

            Person person = new Person(123, "Fred");

            pm.makePersistent(person);


            tx.commit();
        }
        catch (Throwable thr)
        {
            NucleusLogger.GENERAL.error(">> Exception in test", thr);
            fail("Failed test : " + thr.getMessage());
        }
        finally 
        {
            if (tx.isActive())
            {
                tx.rollback();
            }
            pm.close();
        }

        pmf.close();
        NucleusLogger.GENERAL.info(">> test END");
    }
}
