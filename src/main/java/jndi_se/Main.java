package jndi_se;

import entity.Employee;
import org.apache.commons.dbcp.BasicDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.sql.*;

public class Main {
    private static final String JNDI = "java:comp/env/jdbc/database";

    public static void main(String[] args) throws Exception {
        bind();
        lookup();
        final EntityManagerFactory emf = Persistence.createEntityManagerFactory("myPU");
        populate(emf);
        query(emf);
    }

    private static void bind() throws NamingException {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

        final BasicDataSource ds = new BasicDataSource();
        ds.setUrl("jdbc:derby:memory:myDB;create=true");

        final Context context = new InitialContext();
        try {
            context.createSubcontext("java:");
            context.createSubcontext("java:comp");
            context.createSubcontext("java:comp/env");
            context.createSubcontext("java:comp/env/jdbc");
            context.bind(JNDI, ds);
        } finally {
            context.close();
        }
    }

    private static void lookup() throws NamingException, SQLException {
        final Context context = new InitialContext();
        try {
            final DataSource ds = (DataSource) context.lookup(JNDI);
            try (final Connection cn = ds.getConnection();
                 final Statement st = cn.createStatement();
                 final ResultSet rs = st.executeQuery("SELECT CURRENT_TIMESTAMP FROM SYSIBM.SYSDUMMY1")) {
                while (rs.next()) {
                    System.out.println(rs.getTimestamp(1));
                }
            }
        } finally {
            context.close();
        }
    }

    private static void populate(final EntityManagerFactory emf) {
        final EntityManager em = emf.createEntityManager();
        try {
            final EntityTransaction tx = em.getTransaction();
            tx.begin();
            final Employee emp = new Employee();
            emp.setId(1l);
            emp.setFirstName("Jane");
            emp.setLastName("Doe");
            em.persist(emp);
            tx.commit();
        } finally {
            em.close();
        }
    }

    private static void query(final EntityManagerFactory emf) {
        final EntityManager em = emf.createEntityManager();
        try {
            System.out.println(em.find(Employee.class, 1l));
        } finally {
            em.close();
        }
    }
}
