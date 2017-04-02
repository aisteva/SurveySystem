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
@Table(name = "answer")
@NamedQueries({
    @NamedQuery(name = "Answer.findAll", query = "SELECT a FROM Answer a"),
    @NamedQuery(name = "Answer.findByAnswerID", query = "SELECT a FROM Answer a WHERE a.answerID = :answerID"),
    @NamedQuery(name = "Answer.findBySessionID", query = "SELECT a FROM Answer a WHERE a.sessionID = :sessionID"),
    @NamedQuery(name = "Answer.findByText", query = "SELECT a FROM Answer a WHERE a.text = :text")})
public class Answer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "AnswerID")
    private Long answerID;
    @Basic(optional = false)
    @Column(name = "SessionID")
    private int sessionID;
    @Column(name = "Text")
    private String text;
    @JoinColumn(name = "OfferedAnswerID", referencedColumnName = "OfferedAnswerID")
    @ManyToOne(optional = false)
    private Offeredanswer offeredAnswerID;

    public Answer() {
    }

    public Answer(Long answerID) {
        this.answerID = answerID;
    }

    public Answer(Long answerID, int sessionID) {
        this.answerID = answerID;
        this.sessionID = sessionID;
    }

    public Long getAnswerID() {
        return answerID;
    }

    public void setAnswerID(Long answerID) {
        this.answerID = answerID;
    }

    public int getSessionID() {
        return sessionID;
    }

    public void setSessionID(int sessionID) {
        this.sessionID = sessionID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
        hash += (answerID != null ? answerID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Answer)) {
            return false;
        }
        Answer other = (Answer) object;
        if ((this.answerID == null && other.answerID != null) || (this.answerID != null && !this.answerID.equals(other.answerID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "surveydbjpa.Answer[ answerID=" + answerID + " ]";
    }
    
}
