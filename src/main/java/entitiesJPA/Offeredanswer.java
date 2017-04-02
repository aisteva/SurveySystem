/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entitiesJPA;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Aiste
 */
@Entity
@Table(name = "offeredanswer")
@NamedQueries({
    @NamedQuery(name = "Offeredanswer.findAll", query = "SELECT o FROM Offeredanswer o"),
    @NamedQuery(name = "Offeredanswer.findByOfferedAnswerID", query = "SELECT o FROM Offeredanswer o WHERE o.offeredAnswerID = :offeredAnswerID"),
    @NamedQuery(name = "Offeredanswer.findByText", query = "SELECT o FROM Offeredanswer o WHERE o.text = :text")})
public class Offeredanswer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "OfferedAnswerID")
    private Long offeredAnswerID;
    @Basic(optional = false)
    @Column(name = "Text")
    private String text;
    @JoinColumn(name = "QuestionID", referencedColumnName = "QuestionID")
    @ManyToOne(optional = false)
    private Question questionID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "offeredAnswerID")
    private List<Answer> answerList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "offeredAnswerID")
    private List<Answerconnection> answerconnectionList;

    public Offeredanswer() {
    }

    public Offeredanswer(Long offeredAnswerID) {
        this.offeredAnswerID = offeredAnswerID;
    }

    public Offeredanswer(Long offeredAnswerID, String text) {
        this.offeredAnswerID = offeredAnswerID;
        this.text = text;
    }

    public Long getOfferedAnswerID() {
        return offeredAnswerID;
    }

    public void setOfferedAnswerID(Long offeredAnswerID) {
        this.offeredAnswerID = offeredAnswerID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Question getQuestionID() {
        return questionID;
    }

    public void setQuestionID(Question questionID) {
        this.questionID = questionID;
    }

    public List<Answer> getAnswerList() {
        return answerList;
    }

    public void setAnswerList(List<Answer> answerList) {
        this.answerList = answerList;
    }

    public List<Answerconnection> getAnswerconnectionList() {
        return answerconnectionList;
    }

    public void setAnswerconnectionList(List<Answerconnection> answerconnectionList) {
        this.answerconnectionList = answerconnectionList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (offeredAnswerID != null ? offeredAnswerID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Offeredanswer)) {
            return false;
        }
        Offeredanswer other = (Offeredanswer) object;
        if ((this.offeredAnswerID == null && other.offeredAnswerID != null) || (this.offeredAnswerID != null && !this.offeredAnswerID.equals(other.offeredAnswerID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "surveydbjpa.Offeredanswer[ offeredAnswerID=" + offeredAnswerID + " ]";
    }
    
}
