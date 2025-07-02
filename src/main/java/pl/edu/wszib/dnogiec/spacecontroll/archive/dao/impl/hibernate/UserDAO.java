//package pl.edu.wszib.dnogiec.spacecontroll.dao.impl.hibernate;
//
//import jakarta.persistence.NoResultException;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.query.Query;
//import pl.edu.wszib.dnogiec.spacecontroll.dao.impl.IUserDAO;
//import pl.edu.wszib.dnogiec.spacecontroll.model.User;
//
//import java.util.List;
//import java.util.Optional;
//
//public class UserDAO implements IUserDAO {
//
//    private final SessionFactory sessionFactory;
//
//    public UserDAO(SessionFactory sessionFactory) {
//        this.sessionFactory = sessionFactory;
//    }
//
//    @Override
//    public Optional<User> getById(Long id) {
//        Session session = sessionFactory.openSession();
//        Query<User> query = session.createQuery("FROM pl.edu.wszib.dnogiec.spacecontroll.model.User WHERE id = :id", User.class);
//        query.setParameter("id", id);
//        try {
//            User user = query.getSingleResult();
//            return Optional.of(user);
//        } catch (NoResultException e) {
//            return Optional.empty();
//        } finally {
//            session.close();
//        }
//    }
//
//    @Override
//    public Optional<User> getByLogin(String login) {
//        Session session = sessionFactory.openSession();
//        Query<User> query =
//                session.createQuery("FROM pl.edu.wszib.dnogiec.spacecontroll.model.User WHERE login = :login", User.class);
//        query.setParameter("login", login);
//        try {
//            User user = query.getSingleResult();
//            return Optional.of(user);
//        } catch (NoResultException e) {
//            return Optional.empty();
//        } finally {
//            session.close();
//        }
//    }
//
//    @Override
//    public List<User> getAll() {
//        Session session = sessionFactory.openSession();
//        Query<User> query = session.createQuery("FROM pl.edu.wszib.dnogiec.spacecontroll.model.User", User.class);
//        List<User> resultList = query.getResultList();
//        session.close();
//        return resultList;
//    }
//
//    @Override
//    public void save(User user) {
//        Session session = sessionFactory.openSession();
//        try {
//            session.beginTransaction();
//            session.save(user);
//            session.getTransaction().commit();
//        } catch (Exception e) {
//            session.getTransaction().rollback();
//        } finally {
//            session.close();
//        }
//    }
//
//    @Override
//    public void remove(Long id) {
//        Session session = sessionFactory.openSession();
//        try {
//            session.beginTransaction();
//            session.remove(new User(id));
//            session.getTransaction().commit();
//        } catch (Exception e) {
//            session.getTransaction().rollback();
//        } finally {
//            session.close();
//        }
//    }
//
//    @Override
//    public void update(User user) {
//        Session session = sessionFactory.openSession();
//        try {
//            session.beginTransaction();
//            session.merge(user);
//            session.getTransaction().commit();
//        } catch (Exception e) {
//            session.getTransaction().rollback();
//        } finally {
//            session.close();
//        }
//
//    }
//}
