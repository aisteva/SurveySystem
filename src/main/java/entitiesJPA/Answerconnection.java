/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entitiesJPA;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author Aiste
 */
@Entity
@Table(name = "answerconnection")
@NamedQueries({
    @NamedQuery(name = "Answerconnection.findAll", query = "SELECT a FROM Answerconnection a"),
    @NamedQuery(name = "Answerconnection.findByAnswerConnectionID", query = "SELECT a FROM Answerconnection a WHERE a.answerConnectionID = :answerConnectionID")})
public class Answerconnection implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "AnswerConnectionID")
    private Long answerConnectionID;
    @JoinColumn(name = "QuestionID", referencedColumnName = "QuestionID")
    @ManyToOne(optional = false)
    private Question questionID;
    @JoinColumn(name = "OfferedAnswerID", referencedColumnName = "OfferedAnswerID")
    @ManyToOne(optional = false)
    private Offeredanswer offeredAnswerID;

    public Answerconnection() {
    }

    public Answerconnection(Long answerConnectionID) {
        this.answerConnectionID = answerConnectionID;
    }

    public Long getAnswerConnectionID() {
        return answerConnectionID;
    }

    public void setAnswerConnectionID(Long answerConnectionID) {
        this.answerConnectionID = answerConnectionID;
    }

    public Question getQuestionID() {
        return questionID;
    }

    public void setQuestionID(Question questionID) {
        this.questionID = questionID;
    }

    public Offeredanswer getOfferedAnswerID() {
        return offeredAnswerID;
    }

    public void setOfferedAnswerID(Offeredanswer offeredAnswerID) {
        this.offeredAnswerID = offeredAnswerID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (answerConnectionID != null ? answerConnectionID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Answerconnection)) {
            return false;
        }
        Answerconnection other = (Answerconnection) object;
        if ((this.answerConnectionID == null && other.answerConnectionID != null) || (this.answerConnectionID != null && !this.answerConnectionID.equals(other.answerConnectionID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "surveydbjpa.Answerconnection[ answerConnectionID=" + answerConnectionID + " ]";
    }
    
}
