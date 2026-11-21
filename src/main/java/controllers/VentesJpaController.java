/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.RollbackFailureException;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import entities.Caisses;
import entities.Utilisateurs;
import entities.Ventes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.UserTransaction;
import java.util.List;

/**
 *
 * @author Gloris
 */
public class VentesJpaController implements Serializable {

    public VentesJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Ventes ventes) throws RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Caisses caisseId = ventes.getCaisseId();
            if (caisseId != null) {
                caisseId = em.getReference(caisseId.getClass(), caisseId.getId());
                ventes.setCaisseId(caisseId);
            }
            Utilisateurs caissierId = ventes.getCaissierId();
            if (caissierId != null) {
                caissierId = em.getReference(caissierId.getClass(), caissierId.getId());
                ventes.setCaissierId(caissierId);
            }
            em.persist(ventes);
            if (caisseId != null) {
                caisseId.getVentesList().add(ventes);
                caisseId = em.merge(caisseId);
            }
            if (caissierId != null) {
                caissierId.getVentesList().add(ventes);
                caissierId = em.merge(caissierId);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Ventes ventes) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Ventes persistentVentes = em.find(Ventes.class, ventes.getId());
            Caisses caisseIdOld = persistentVentes.getCaisseId();
            Caisses caisseIdNew = ventes.getCaisseId();
            Utilisateurs caissierIdOld = persistentVentes.getCaissierId();
            Utilisateurs caissierIdNew = ventes.getCaissierId();
            if (caisseIdNew != null) {
                caisseIdNew = em.getReference(caisseIdNew.getClass(), caisseIdNew.getId());
                ventes.setCaisseId(caisseIdNew);
            }
            if (caissierIdNew != null) {
                caissierIdNew = em.getReference(caissierIdNew.getClass(), caissierIdNew.getId());
                ventes.setCaissierId(caissierIdNew);
            }
            ventes = em.merge(ventes);
            if (caisseIdOld != null && !caisseIdOld.equals(caisseIdNew)) {
                caisseIdOld.getVentesList().remove(ventes);
                caisseIdOld = em.merge(caisseIdOld);
            }
            if (caisseIdNew != null && !caisseIdNew.equals(caisseIdOld)) {
                caisseIdNew.getVentesList().add(ventes);
                caisseIdNew = em.merge(caisseIdNew);
            }
            if (caissierIdOld != null && !caissierIdOld.equals(caissierIdNew)) {
                caissierIdOld.getVentesList().remove(ventes);
                caissierIdOld = em.merge(caissierIdOld);
            }
            if (caissierIdNew != null && !caissierIdNew.equals(caissierIdOld)) {
                caissierIdNew.getVentesList().add(ventes);
                caissierIdNew = em.merge(caissierIdNew);
            }
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = ventes.getId();
                if (findVentes(id) == null) {
                    throw new NonexistentEntityException("The ventes with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Ventes ventes;
            try {
                ventes = em.getReference(Ventes.class, id);
                ventes.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The ventes with id " + id + " no longer exists.", enfe);
            }
            Caisses caisseId = ventes.getCaisseId();
            if (caisseId != null) {
                caisseId.getVentesList().remove(ventes);
                caisseId = em.merge(caisseId);
            }
            Utilisateurs caissierId = ventes.getCaissierId();
            if (caissierId != null) {
                caissierId.getVentesList().remove(ventes);
                caissierId = em.merge(caissierId);
            }
            em.remove(ventes);
            utx.commit();
        } catch (Exception ex) {
            try {
                utx.rollback();
            } catch (Exception re) {
                throw new RollbackFailureException("An error occurred attempting to roll back the transaction.", re);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Ventes> findVentesEntities() {
        return findVentesEntities(true, -1, -1);
    }

    public List<Ventes> findVentesEntities(int maxResults, int firstResult) {
        return findVentesEntities(false, maxResults, firstResult);
    }

    private List<Ventes> findVentesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Ventes.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Ventes findVentes(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Ventes.class, id);
        } finally {
            em.close();
        }
    }

    public int getVentesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Ventes> rt = cq.from(Ventes.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
