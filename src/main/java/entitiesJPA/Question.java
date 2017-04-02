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
@Table(name = "question")
@NamedQueries({
    @NamedQuery(name = "Question.findAll", query = "SELECT q FROM Question q"),
    @NamedQuery(name = "Question.findByQuestionID", query = "SELECT q FROM Question q WHERE q.questionID = :questionID"),
    @NamedQuery(name = "Question.findByQuestionText", query = "SELECT q FROM Question q WHERE q.questionText = :questionText"),
    @NamedQuery(name = "Question.findByQuestionNumber", query = "SELECT q FROM Question q WHERE q.questionNumber = :questionNumber"),
    @NamedQuery(name = "Question.findByPage", query = "SELECT q FROM Question q WHERE q.page = :page"),
    @NamedQuery(name = "Question.findByType", query = "SELECT q FROM Question q WHERE q.type = :type"),
    @NamedQuery(name = "Question.findByIsRequired", query = "SELECT q FROM Question q WHERE q.isRequired = :isRequired")})
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "QuestionID")
    private Long questionID;
    @Basic(optional = false)
    @Column(name = "QuestionText")
    private String questionText;
    @Basic(optional = false)
    @Column(name = "QuestionNumber")
    private int questionNumber;
    @Column(name = "Page")
    private Integer page;
    @Basic(optional = false)
    @Column(name = "Type")
    private String type;
    @Basic(optional = false)
    @Column(name = "isRequired")
    private boolean isRequired;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionID")
    private List<Offeredanswer> offeredanswerList;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionID")
    private List<Answerconnection> answerconnectionList;
    @JoinColumn(name = "SurveyID", referencedColumnName = "SurveyID")
    @ManyToOne(optional = false)
    private Survey surveyID;

    public Question() {
    }

    public Question(Long questionID) {
        this.questionID = questionID;
    }

    public Question(Long questionID, String questionText, int questionNumber, String type, boolean isRequired) {
        this.questionID = questionID;
        this.questionText = questionText;
        this.questionNumber = questionNumber;
        this.type = type;
        this.isRequired = isRequired;
    }

    public Long getQuestionID() {
        return questionID;
    }

    public void setQuestionID(Long questionID) {
        this.questionID = questionID;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public List<Offeredanswer> getOfferedanswerList() {
        return offeredanswerList;
    }

    public void setOfferedanswerList(List<Offeredanswer> offeredanswerList) {
        this.offeredanswerList = offeredanswerList;
    }

    public List<Answerconnection> getAnswerconnectionList() {
        return answerconnectionList;
    }

    public void setAnswerconnectionList(List<Answerconnection> answerconnectionList) {
        this.answerconnectionList = answerconnectionList;
    }

    public Survey getSurveyID() {
        return surveyID;
    }

    public void setSurveyID(Survey surveyID) {
        this.surveyID = surveyID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (questionID != null ? questionID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Question)) {
            return false;
        }
        Question other = (Question) object;
        if ((this.questionID == null && other.questionID != null) || (this.questionID != null && !this.questionID.equals(other.questionID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "surveydbjpa.Question[ questionID=" + questionID + " ]";
    }
    
}
