package com.danhaywood.datanucleus.testcase.nuccore1326;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.log4j.PropertyConfigurator;
import org.datanucleus.NucleusContext;
import org.datanucleus.StoreNucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.MetaDataListener;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;
import org.datanucleus.util.NucleusLogger;
import org.junit.Test;

import static org.junit.Assert.fail;

public class SimpleTest
{

    static {
//        BasicConfigurator.configure();
        PropertyConfigurator.configure(SimpleTest.class.getResource("logging.properties"));
    }

    @Test
    public void testSimple()
    {
        createOrRecreateSchema();
        createOrRecreateSchema();
    }

    void createOrRecreateSchema() {
        NucleusLogger.GENERAL.info(">> test START");
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("NUCCORE-1326");

        final JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory)pmf;
        final NucleusContext nucleusContext = jdopmf.getNucleusContext();

        Set<String> classNames = Sets.newLinkedHashSet();
        classNames.addAll(Lists.newArrayList(
                Other.class.getName(), Person.class.getName()));

        if (nucleusContext instanceof StoreNucleusContext) {

            StoreNucleusContext storeNucleusContext = (StoreNucleusContext) nucleusContext;

            final StoreManager storeManager = storeNucleusContext.getStoreManager();
            final MetaDataManager metaDataManager = nucleusContext.getMetaDataManager();

            CreateSchemaListener listener = new CreateSchemaListener(jdopmf);

            // doesn't seem to fire, because pmf eagerly has already found and loaded all metadata
            //metaDataManager.registerListener(listener);

            // so instead, call manually
            for (String className : classNames) {
                AbstractClassMetaData cmd = metaDataManager.getMetaDataForClass(className, null);
                listener.loaded(cmd);
            }

            SchemaAwareStoreManager schemaAwareStoreManager = (SchemaAwareStoreManager) storeManager;
            schemaAwareStoreManager.createSchemaForClasses(classNames, new Properties());
        }

        createObject(pmf);

        pmf.close();
        NucleusLogger.GENERAL.info(">> test END");
    }

    private void createObject(final PersistenceManagerFactory pmf) {
        PersistenceManager pm = pmf.getPersistenceManager();

        Transaction tx = pm.currentTransaction();
        try
        {
            tx.begin();

            //Person person = new Person(123, "Fred");
            //pm.makePersistent(person);

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
    }

    private static class CreateSchemaListener implements MetaDataListener {

        private final JDOPersistenceManagerFactory jdopmf;
        public String url;

        public CreateSchemaListener(final JDOPersistenceManagerFactory jdopmf) {
            this.jdopmf = jdopmf;
        }

        @Override
        public void loaded(final AbstractClassMetaData cmd) {

            final String schemaName = cmd.getSchema();
            if (Strings.isNullOrEmpty(schemaName)) {
                return;
            }

            String driverName = jdopmf.getConnectionDriverName();
            this.url = jdopmf.getConnectionURL();
            String userName = jdopmf.getConnectionUserName();
            String password = jdopmf.getConnectionPassword();

            Connection connection = null;
            Statement statement = null;
            try {
                connection = DriverManager.getConnection(url, userName, password);
                statement = connection.createStatement();
                if(skip(cmd, statement)) {
                    return;
                }
                exec(cmd, statement);
            } catch (SQLException e) {
                NucleusLogger.GENERAL.warn("Unable to create schema", e);
            } finally {
                closeSafely(statement);
                closeSafely(connection);
            }
        }

        protected boolean skip(final AbstractClassMetaData cmd, final Statement statement) throws SQLException {
            final String schemaName = cmd.getSchema();
            if(Strings.isNullOrEmpty(schemaName)) {
                return true;
            }
            final String sql = buildSqlToCheck(cmd);
            try (final ResultSet rs = statement.executeQuery(sql)) {
                rs.next();
                final int cnt = rs.getInt(1);
                return cnt > 0;
            }
        }

        protected String buildSqlToCheck(final AbstractClassMetaData cmd) {
            final String schemaName = schemaNameFor(cmd);
            return String.format("SELECT count(*) FROM INFORMATION_SCHEMA.SCHEMATA where SCHEMA_NAME = '%s'", schemaName);
        }
        protected boolean exec(final AbstractClassMetaData cmd, final Statement statement) throws SQLException {
            final String sql = buildSqlToExec(cmd);
            return statement.execute(sql);
        }

        protected String buildSqlToExec(final AbstractClassMetaData cmd) {
            final String schemaName = schemaNameFor(cmd);
            return String.format("CREATE SCHEMA \"%s\"", schemaName);
        }

        protected String schemaNameFor(final AbstractClassMetaData cmd) {
            String schemaName = cmd.getSchema();

            // DN uses different casing for identifiers.
            //
            // http://www.datanucleus.org/products/accessplatform_3_2/jdo/orm/datastore_identifiers.html
            // http://www.datanucleus.org/products/accessplatform_4_0/jdo/orm/datastore_identifiers.html
            //
            // the following attempts to accommodate heuristically for the "out-of-the-box" behaviour for three common
            // db vendors without requiring lots of complex configuration of DataNucleus
            //

            if(url.contains("postgres")) {
                schemaName = schemaName.toLowerCase(Locale.ROOT);
            }
            if(url.contains("hsqldb")) {
                schemaName = schemaName.toUpperCase(Locale.ROOT);
            }
            if(url.contains("h2")) {
                schemaName = schemaName.toUpperCase(Locale.ROOT);
            }
            if(url.contains("sqlserver")) {
                // unchanged
            }
            return schemaName;
        }

        protected void closeSafely(final AutoCloseable connection) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }

    }
}
