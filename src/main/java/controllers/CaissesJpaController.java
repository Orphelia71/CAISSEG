/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import controllers.exceptions.IllegalOrphanException;
import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.RollbackFailureException;
import entities.Caisses;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import entities.Utilisateurs;
import entities.Ventes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Gloris
 */
public class CaissesJpaController implements Serializable {

    public CaissesJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Caisses caisses) throws RollbackFailureException, Exception {
        if (caisses.getVentesList() == null) {
            caisses.setVentesList(new ArrayList<Ventes>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Utilisateurs caissierId = caisses.getCaissierId();
            if (caissierId != null) {
                caissierId = em.getReference(caissierId.getClass(), caissierId.getId());
                caisses.setCaissierId(caissierId);
            }
            List<Ventes> attachedVentesList = new ArrayList<Ventes>();
            for (Ventes ventesListVentesToAttach : caisses.getVentesList()) {
                ventesListVentesToAttach = em.getReference(ventesListVentesToAttach.getClass(), ventesListVentesToAttach.getId());
                attachedVentesList.add(ventesListVentesToAttach);
            }
            caisses.setVentesList(attachedVentesList);
            em.persist(caisses);
            if (caissierId != null) {
                caissierId.getCaissesList().add(caisses);
                caissierId = em.merge(caissierId);
            }
            for (Ventes ventesListVentes : caisses.getVentesList()) {
                Caisses oldCaisseIdOfVentesListVentes = ventesListVentes.getCaisseId();
                ventesListVentes.setCaisseId(caisses);
                ventesListVentes = em.merge(ventesListVentes);
                if (oldCaisseIdOfVentesListVentes != null) {
                    oldCaisseIdOfVentesListVentes.getVentesList().remove(ventesListVentes);
                    oldCaisseIdOfVentesListVentes = em.merge(oldCaisseIdOfVentesListVentes);
                }
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

    public void edit(Caisses caisses) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Caisses persistentCaisses = em.find(Caisses.class, caisses.getId());
            Utilisateurs caissierIdOld = persistentCaisses.getCaissierId();
            Utilisateurs caissierIdNew = caisses.getCaissierId();
            List<Ventes> ventesListOld = persistentCaisses.getVentesList();
            List<Ventes> ventesListNew = caisses.getVentesList();
            List<String> illegalOrphanMessages = null;
            for (Ventes ventesListOldVentes : ventesListOld) {
                if (!ventesListNew.contains(ventesListOldVentes)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Ventes " + ventesListOldVentes + " since its caisseId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (caissierIdNew != null) {
                caissierIdNew = em.getReference(caissierIdNew.getClass(), caissierIdNew.getId());
                caisses.setCaissierId(caissierIdNew);
            }
            List<Ventes> attachedVentesListNew = new ArrayList<Ventes>();
            for (Ventes ventesListNewVentesToAttach : ventesListNew) {
                ventesListNewVentesToAttach = em.getReference(ventesListNewVentesToAttach.getClass(), ventesListNewVentesToAttach.getId());
                attachedVentesListNew.add(ventesListNewVentesToAttach);
            }
            ventesListNew = attachedVentesListNew;
            caisses.setVentesList(ventesListNew);
            caisses = em.merge(caisses);
            if (caissierIdOld != null && !caissierIdOld.equals(caissierIdNew)) {
                caissierIdOld.getCaissesList().remove(caisses);
                caissierIdOld = em.merge(caissierIdOld);
            }
            if (caissierIdNew != null && !caissierIdNew.equals(caissierIdOld)) {
                caissierIdNew.getCaissesList().add(caisses);
                caissierIdNew = em.merge(caissierIdNew);
            }
            for (Ventes ventesListNewVentes : ventesListNew) {
                if (!ventesListOld.contains(ventesListNewVentes)) {
                    Caisses oldCaisseIdOfVentesListNewVentes = ventesListNewVentes.getCaisseId();
                    ventesListNewVentes.setCaisseId(caisses);
                    ventesListNewVentes = em.merge(ventesListNewVentes);
                    if (oldCaisseIdOfVentesListNewVentes != null && !oldCaisseIdOfVentesListNewVentes.equals(caisses)) {
                        oldCaisseIdOfVentesListNewVentes.getVentesList().remove(ventesListNewVentes);
                        oldCaisseIdOfVentesListNewVentes = em.merge(oldCaisseIdOfVentesListNewVentes);
                    }
                }
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
                Integer id = caisses.getId();
                if (findCaisses(id) == null) {
                    throw new NonexistentEntityException("The caisses with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Caisses caisses;
            try {
                caisses = em.getReference(Caisses.class, id);
                caisses.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The caisses with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Ventes> ventesListOrphanCheck = caisses.getVentesList();
            for (Ventes ventesListOrphanCheckVentes : ventesListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Caisses (" + caisses + ") cannot be destroyed since the Ventes " + ventesListOrphanCheckVentes + " in its ventesList field has a non-nullable caisseId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Utilisateurs caissierId = caisses.getCaissierId();
            if (caissierId != null) {
                caissierId.getCaissesList().remove(caisses);
                caissierId = em.merge(caissierId);
            }
            em.remove(caisses);
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

    public List<Caisses> findCaissesEntities() {
        return findCaissesEntities(true, -1, -1);
    }

    public List<Caisses> findCaissesEntities(int maxResults, int firstResult) {
        return findCaissesEntities(false, maxResults, firstResult);
    }

    private List<Caisses> findCaissesEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Caisses.class));
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

    public Caisses findCaisses(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Caisses.class, id);
        } finally {
            em.close();
        }
    }

    public int getCaissesCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Caisses> rt = cq.from(Caisses.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
