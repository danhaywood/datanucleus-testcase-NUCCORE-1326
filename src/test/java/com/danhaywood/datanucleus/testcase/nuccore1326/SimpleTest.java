package com.danhaywood.datanucleus.testcase.nuccore1326;

import java.util.Properties;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.log4j.PropertyConfigurator;
import org.datanucleus.NucleusContext;
import org.datanucleus.StoreNucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;
import org.junit.Test;

public class SimpleTest
{

    static {
        PropertyConfigurator.configure(SimpleTest.class.getResource("logging.properties"));
    }

    @Test
    public void testSimple()
    {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("NUCCORE-1326");

        final JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory)pmf;
        final NucleusContext nucleusContext = jdopmf.getNucleusContext();

        Set<String> classNames = Sets.newLinkedHashSet();
        classNames.addAll(Lists.newArrayList(
                Person.class.getName(), Other.class.getName()));

        StoreNucleusContext storeNucleusContext = (StoreNucleusContext) nucleusContext;

        final StoreManager storeManager = storeNucleusContext.getStoreManager();
        SchemaAwareStoreManager schemaAwareStoreManager = (SchemaAwareStoreManager) storeManager;

        // programmatic API to SchemaTool
        schemaAwareStoreManager.createSchemaForClasses(classNames, new Properties());


        pmf.close();
    }

}
