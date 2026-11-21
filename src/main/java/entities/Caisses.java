/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Gloris
 */
@Entity
@Table(name = "caisses")
@NamedQueries({
    @NamedQuery(name = "Caisses.findAll", query = "SELECT c FROM Caisses c"),
    @NamedQuery(name = "Caisses.findById", query = "SELECT c FROM Caisses c WHERE c.id = :id"),
    @NamedQuery(name = "Caisses.findByNumero", query = "SELECT c FROM Caisses c WHERE c.numero = :numero"),
    @NamedQuery(name = "Caisses.findByLibelle", query = "SELECT c FROM Caisses c WHERE c.libelle = :libelle"),
    @NamedQuery(name = "Caisses.findByEtat", query = "SELECT c FROM Caisses c WHERE c.etat = :etat")})
public class Caisses implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "numero")
    private int numero;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "libelle")
    private String libelle;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "etat")
    private String etat;
    @JoinColumn(name = "caissier_id", referencedColumnName = "id")
    @ManyToOne
    private Utilisateurs caissierId;
    @OneToMany(mappedBy="caisseId", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Ventes> ventesList;

    public Caisses() {
    }

    public Caisses(Integer id) {
        this.id = id;
    }

    public Caisses(Integer id, int numero, String libelle, String etat) {
        this.id = id;
        this.numero = numero;
        this.libelle = libelle;
        this.etat = etat;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public Utilisateurs getCaissierId() {
        return caissierId;
    }

    public void setCaissierId(Utilisateurs caissierId) {
        this.caissierId = caissierId;
    }

    public List<Ventes> getVentesList() {
        return ventesList;
    }

    public void setVentesList(List<Ventes> ventesList) {
        this.ventesList = ventesList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Caisses)) {
            return false;
        }
        Caisses other = (Caisses) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return libelle;
    }
    
}
