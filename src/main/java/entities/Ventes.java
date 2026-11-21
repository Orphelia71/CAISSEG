/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import jakarta.persistence.Basic;
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
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Gloris
 */
@Entity
@Table(name = "ventes")
@NamedQueries({
    @NamedQuery(name = "Ventes.findAll", query = "SELECT v FROM Ventes v"),
    @NamedQuery(name = "Ventes.findById", query = "SELECT v FROM Ventes v WHERE v.id = :id"),
    @NamedQuery(name = "Ventes.findByDescription", query = "SELECT v FROM Ventes v WHERE v.description = :description"),
    @NamedQuery(name = "Ventes.findByMontant", query = "SELECT v FROM Ventes v WHERE v.montant = :montant"),
    @NamedQuery(name = "Ventes.findByModePaiement", query = "SELECT v FROM Ventes v WHERE v.modePaiement = :modePaiement"),
    @NamedQuery(name = "Ventes.findByDateVente", query = "SELECT v FROM Ventes v WHERE v.dateVente = :dateVente")})
public class Ventes implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "description")
    private String description;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "montant")
    private BigDecimal montant;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "mode_paiement")
    private String modePaiement;
    @Column(name = "date_vente")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateVente;
    @JoinColumn(name = "caisse_id", referencedColumnName = "id")
    @ManyToOne(optional = false,fetch = FetchType.EAGER)
    private Caisses caisseId;
    @JoinColumn(name = "caissier_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private Utilisateurs caissierId;

    public Ventes() {
    }

    public Ventes(Integer id) {
        this.id = id;
    }

    public Ventes(Integer id, String description, BigDecimal montant, String modePaiement) {
        this.id = id;
        this.description = description;
        this.montant = montant;
        this.modePaiement = modePaiement;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public Date getDateVente() {
        return dateVente;
    }

    public void setDateVente(Date dateVente) {
        this.dateVente = dateVente;
    }

    public Caisses getCaisseId() {
        return caisseId;
    }

    public void setCaisseId(Caisses caisseId) {
        this.caisseId = caisseId;
    }

    public Utilisateurs getCaissierId() {
        return caissierId;
    }

    public void setCaissierId(Utilisateurs caissierId) {
        this.caissierId = caissierId;
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
        if (!(object instanceof Ventes)) {
            return false;
        }
        Ventes other = (Ventes) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.Ventes[ id=" + id + " ]";
    }
    
}
