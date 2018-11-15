package dao;


import model.Account.Account;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.PersistenceException;
import java.util.List;


/**
 * Created by maxim on 03.10.18.
 */
public class AccountDAOImpl implements AccountDAO {

    private SessionFactory sessionFactory;

    public AccountDAOImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }


    @Override
    public boolean insert(Account account){

        Session session = sessionFactory.openSession();

        Transaction transaction = null;


        transaction = session.getTransaction();


        transaction.begin();
        session.save(account);
        try {
            transaction.commit();
        }catch (PersistenceException exc){
            session.close();
            return false;
        }

        session.close();

        return true;
    }

    @Override
    public Account getAccountByLogin(String login) throws WrongParameterException{
        Session session = sessionFactory.openSession();

        Account acc = session.get(Account.class, login);

        session.close();

        if(acc == null)
            throw new WrongParameterException();

        return acc;
    }

    @Override
    public Account getAccountByToken(String token) throws UnauthorizedException{
        Session session = sessionFactory.openSession();

        Query query = session.createQuery("FROM Account WHERE access_token = :token");
        query.setParameter("token", token);
        List result = query.getResultList();
        session.close();

        if(result.get(0) == null)
            throw new UnauthorizedException();

        return (Account) result.get(0);
    }

    @Override
    public boolean update(Account account) throws UnauthorizedException{
        Session session = sessionFactory.openSession();

        Transaction transaction = null;


        transaction = session.getTransaction();


        transaction.begin();
        session.update(account);
        try {
            transaction.commit();
        }catch (PersistenceException exc){
            session.close();
            return false;
        }

        session.close();

        return true;

    }

    @Override
    public boolean update(Account[] accounts) throws UnauthorizedException {
        Session session = sessionFactory.openSession();

        Transaction transaction = null;


        transaction = session.getTransaction();


        transaction.begin();
        for(int i = 0; i< accounts.length; ++i)
            session.update(accounts[i]);
        try {
            transaction.commit();
        }catch (PersistenceException exc){
            transaction.rollback();
            session.close();
            return false;
        }

        session.close();

        return true;

    }

    @Override
    public void addFriend(String token, String login) throws UnauthorizedException, WrongParameterException{
        Session session = sessionFactory.openSession();

        Query query = session.createQuery("FROM Account WHERE access_token = :token");
        query.setParameter("token", token);
        Account acc1 = (Account) query.getResultList().get(0);

        Account acc2 = session.get(Account.class, login);

        if(acc1 == null) {
            session.close();
            throw new UnauthorizedException();
        }
        if (acc2 == null
                ||acc1.getLogin().equals(acc2.getLogin())
                ||acc1.getFriends().contains(acc2.getLogin())
                ||acc1.getOutRequest().contains(acc2.getLogin())){
            session.close();
            throw new WrongParameterException();
        }

        Transaction transaction = session.getTransaction();
        transaction.begin();
        if(acc2.getOutRequest().contains(acc1.getLogin())){
            acc2.getOutRequest().remove(acc1.getLogin());
            acc2.getFriends().add(acc1.getLogin());
            acc1.getFriends().add(acc2.getLogin());
            session.update(acc1);
            session.update(acc2);
        }else{
            acc1.getOutRequest().add(acc2.getLogin());
            session.update(acc1);
        }

        try {
            transaction.commit();
        }catch (PersistenceException exc){
            transaction.rollback();
        }

        session.close();


    }

    @Override
    public void deleteFriend(String token, String login) throws UnauthorizedException, WrongParameterException{
        Session session = sessionFactory.openSession();

        Query query = session.createQuery("FROM Account WHERE access_token = :token");
        query.setParameter("token", token);
        Account acc1 = (Account) query.getResultList().get(0);

        Account acc2 = session.get(Account.class, login);

        if(acc1 == null) {
            session.close();
            throw new UnauthorizedException();
        }
        if (acc2 == null
                ||!acc1.getFriends().contains(acc2.getLogin())){
            session.close();
            throw new WrongParameterException();
        }

        Transaction transaction = session.getTransaction();
        transaction.begin();
        acc1.getFriends().remove(acc2.getLogin());
        acc2.getFriends().remove(acc1.getLogin());

        session.update(acc1);
        session.update(acc2);
        try {
            transaction.commit();
        }catch (PersistenceException exc){
            transaction.rollback();
        }

        session.close();
    }

    @Override
    public void deleteFriendRequest(String token, String login) throws UnauthorizedException, WrongParameterException{
        Session session = sessionFactory.openSession();

        Query query = session.createQuery("FROM Account WHERE access_token = :token");
        query.setParameter("token", token);
        Account acc1 = (Account) query.getResultList().get(0);

        Account acc2 = session.get(Account.class, login);

        if(acc1 == null) {
            session.close();
            throw new UnauthorizedException();
        }
        if (acc2 == null
                ||!(acc1.getOutRequest().contains(acc2.getLogin())
                    ||acc1.getInRequest().contains(acc2.getLogin()))){
            session.close();
            throw new WrongParameterException();
        }

        Transaction transaction = session.getTransaction();
        transaction.begin();

        if(acc1.getInRequest().contains(acc2.getLogin()))
            acc1.getInRequest().remove(acc2.getLogin());
        else
            acc1.getOutRequest().remove(acc2.getLogin());

        session.update(acc1);

        try {
            transaction.commit();
        }catch (PersistenceException exc){
            transaction.rollback();
        }

        session.close();
    }

    @Override
    public boolean deleteAccount(String token) throws UnauthorizedException{
        return true;
    }


    @Override
    public boolean checkAuthorize(String token){
        Session session = sessionFactory.openSession();

        Query query = session.createQuery("FROM account WHERE access_token = :token");
        query.setParameter("token", token);

        boolean result = !query.getResultList().isEmpty();

        session.close();

        return result;
    }
}
