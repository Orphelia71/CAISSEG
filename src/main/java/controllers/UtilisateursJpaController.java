/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import controllers.exceptions.IllegalOrphanException;
import controllers.exceptions.NonexistentEntityException;
import controllers.exceptions.RollbackFailureException;
import java.io.Serializable;
import jakarta.persistence.Query;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import entities.Caisses;
import entities.Utilisateurs;
import java.util.ArrayList;
import java.util.List;
import entities.Ventes;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.UserTransaction;

/**
 *
 * @author Gloris
 */
public class UtilisateursJpaController implements Serializable {

    public UtilisateursJpaController(UserTransaction utx, EntityManagerFactory emf) {
        this.utx = utx;
        this.emf = emf;
    }
    private UserTransaction utx = null;
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Utilisateurs utilisateurs) throws RollbackFailureException, Exception {
        if (utilisateurs.getCaissesList() == null) {
            utilisateurs.setCaissesList(new ArrayList<Caisses>());
        }
        if (utilisateurs.getVentesList() == null) {
            utilisateurs.setVentesList(new ArrayList<Ventes>());
        }
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            List<Caisses> attachedCaissesList = new ArrayList<Caisses>();
            for (Caisses caissesListCaissesToAttach : utilisateurs.getCaissesList()) {
                caissesListCaissesToAttach = em.getReference(caissesListCaissesToAttach.getClass(), caissesListCaissesToAttach.getId());
                attachedCaissesList.add(caissesListCaissesToAttach);
            }
            utilisateurs.setCaissesList(attachedCaissesList);
            List<Ventes> attachedVentesList = new ArrayList<Ventes>();
            for (Ventes ventesListVentesToAttach : utilisateurs.getVentesList()) {
                ventesListVentesToAttach = em.getReference(ventesListVentesToAttach.getClass(), ventesListVentesToAttach.getId());
                attachedVentesList.add(ventesListVentesToAttach);
            }
            utilisateurs.setVentesList(attachedVentesList);
            em.persist(utilisateurs);
            for (Caisses caissesListCaisses : utilisateurs.getCaissesList()) {
                Utilisateurs oldCaissierIdOfCaissesListCaisses = caissesListCaisses.getCaissierId();
                caissesListCaisses.setCaissierId(utilisateurs);
                caissesListCaisses = em.merge(caissesListCaisses);
                if (oldCaissierIdOfCaissesListCaisses != null) {
                    oldCaissierIdOfCaissesListCaisses.getCaissesList().remove(caissesListCaisses);
                    oldCaissierIdOfCaissesListCaisses = em.merge(oldCaissierIdOfCaissesListCaisses);
                }
            }
            for (Ventes ventesListVentes : utilisateurs.getVentesList()) {
                Utilisateurs oldCaissierIdOfVentesListVentes = ventesListVentes.getCaissierId();
                ventesListVentes.setCaissierId(utilisateurs);
                ventesListVentes = em.merge(ventesListVentes);
                if (oldCaissierIdOfVentesListVentes != null) {
                    oldCaissierIdOfVentesListVentes.getVentesList().remove(ventesListVentes);
                    oldCaissierIdOfVentesListVentes = em.merge(oldCaissierIdOfVentesListVentes);
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

    public void edit(Utilisateurs utilisateurs) throws IllegalOrphanException, NonexistentEntityException, RollbackFailureException, Exception {
        EntityManager em = null;
        try {
            utx.begin();
            em = getEntityManager();
            Utilisateurs persistentUtilisateurs = em.find(Utilisateurs.class, utilisateurs.getId());
            List<Caisses> caissesListOld = persistentUtilisateurs.getCaissesList();
            List<Caisses> caissesListNew = utilisateurs.getCaissesList();
            List<Ventes> ventesListOld = persistentUtilisateurs.getVentesList();
            List<Ventes> ventesListNew = utilisateurs.getVentesList();
            List<String> illegalOrphanMessages = null;
            for (Ventes ventesListOldVentes : ventesListOld) {
                if (!ventesListNew.contains(ventesListOldVentes)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Ventes " + ventesListOldVentes + " since its caissierId field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Caisses> attachedCaissesListNew = new ArrayList<Caisses>();
            for (Caisses caissesListNewCaissesToAttach : caissesListNew) {
                caissesListNewCaissesToAttach = em.getReference(caissesListNewCaissesToAttach.getClass(), caissesListNewCaissesToAttach.getId());
                attachedCaissesListNew.add(caissesListNewCaissesToAttach);
            }
            caissesListNew = attachedCaissesListNew;
            utilisateurs.setCaissesList(caissesListNew);
            List<Ventes> attachedVentesListNew = new ArrayList<Ventes>();
            for (Ventes ventesListNewVentesToAttach : ventesListNew) {
                ventesListNewVentesToAttach = em.getReference(ventesListNewVentesToAttach.getClass(), ventesListNewVentesToAttach.getId());
                attachedVentesListNew.add(ventesListNewVentesToAttach);
            }
            ventesListNew = attachedVentesListNew;
            utilisateurs.setVentesList(ventesListNew);
            utilisateurs = em.merge(utilisateurs);
            for (Caisses caissesListOldCaisses : caissesListOld) {
                if (!caissesListNew.contains(caissesListOldCaisses)) {
                    caissesListOldCaisses.setCaissierId(null);
                    caissesListOldCaisses = em.merge(caissesListOldCaisses);
                }
            }
            for (Caisses caissesListNewCaisses : caissesListNew) {
                if (!caissesListOld.contains(caissesListNewCaisses)) {
                    Utilisateurs oldCaissierIdOfCaissesListNewCaisses = caissesListNewCaisses.getCaissierId();
                    caissesListNewCaisses.setCaissierId(utilisateurs);
                    caissesListNewCaisses = em.merge(caissesListNewCaisses);
                    if (oldCaissierIdOfCaissesListNewCaisses != null && !oldCaissierIdOfCaissesListNewCaisses.equals(utilisateurs)) {
                        oldCaissierIdOfCaissesListNewCaisses.getCaissesList().remove(caissesListNewCaisses);
                        oldCaissierIdOfCaissesListNewCaisses = em.merge(oldCaissierIdOfCaissesListNewCaisses);
                    }
                }
            }
            for (Ventes ventesListNewVentes : ventesListNew) {
                if (!ventesListOld.contains(ventesListNewVentes)) {
                    Utilisateurs oldCaissierIdOfVentesListNewVentes = ventesListNewVentes.getCaissierId();
                    ventesListNewVentes.setCaissierId(utilisateurs);
                    ventesListNewVentes = em.merge(ventesListNewVentes);
                    if (oldCaissierIdOfVentesListNewVentes != null && !oldCaissierIdOfVentesListNewVentes.equals(utilisateurs)) {
                        oldCaissierIdOfVentesListNewVentes.getVentesList().remove(ventesListNewVentes);
                        oldCaissierIdOfVentesListNewVentes = em.merge(oldCaissierIdOfVentesListNewVentes);
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
                Integer id = utilisateurs.getId();
                if (findUtilisateurs(id) == null) {
                    throw new NonexistentEntityException("The utilisateurs with id " + id + " no longer exists.");
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
            Utilisateurs utilisateurs;
            try {
                utilisateurs = em.getReference(Utilisateurs.class, id);
                utilisateurs.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The utilisateurs with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Ventes> ventesListOrphanCheck = utilisateurs.getVentesList();
            for (Ventes ventesListOrphanCheckVentes : ventesListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Utilisateurs (" + utilisateurs + ") cannot be destroyed since the Ventes " + ventesListOrphanCheckVentes + " in its ventesList field has a non-nullable caissierId field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Caisses> caissesList = utilisateurs.getCaissesList();
            for (Caisses caissesListCaisses : caissesList) {
                caissesListCaisses.setCaissierId(null);
                caissesListCaisses = em.merge(caissesListCaisses);
            }
            em.remove(utilisateurs);
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

    public List<Utilisateurs> findUtilisateursEntities() {
        return findUtilisateursEntities(true, -1, -1);
    }

    public List<Utilisateurs> findUtilisateursEntities(int maxResults, int firstResult) {
        return findUtilisateursEntities(false, maxResults, firstResult);
    }

    private List<Utilisateurs> findUtilisateursEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Utilisateurs.class));
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

    public Utilisateurs findUtilisateurs(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Utilisateurs.class, id);
        } finally {
            em.close();
        }
    }

    public int getUtilisateursCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Utilisateurs> rt = cq.from(Utilisateurs.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
