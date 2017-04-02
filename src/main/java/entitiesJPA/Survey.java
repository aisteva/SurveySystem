/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entitiesJPA;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Aiste
 */
@Entity
@Table(name = "survey")
@NamedQueries({
    @NamedQuery(name = "Survey.findAll", query = "SELECT s FROM Survey s"),
    @NamedQuery(name = "Survey.findBySurveyID", query = "SELECT s FROM Survey s WHERE s.surveyID = :surveyID"),
    @NamedQuery(name = "Survey.findByDescription", query = "SELECT s FROM Survey s WHERE s.description = :description"),
    @NamedQuery(name = "Survey.findByStartDate", query = "SELECT s FROM Survey s WHERE s.startDate = :startDate"),
    @NamedQuery(name = "Survey.findByEndDate", query = "SELECT s FROM Survey s WHERE s.endDate = :endDate"),
    @NamedQuery(name = "Survey.findBySurveyURL", query = "SELECT s FROM Survey s WHERE s.surveyURL = :surveyURL"),
    @NamedQuery(name = "Survey.findByIsOpen", query = "SELECT s FROM Survey s WHERE s.isOpen = :isOpen"),
    @NamedQuery(name = "Survey.findByIsCreated", query = "SELECT s FROM Survey s WHERE s.isCreated = :isCreated"),
    @NamedQuery(name = "Survey.findByIsPrivate", query = "SELECT s FROM Survey s WHERE s.isPrivate = :isPrivate")})
public class Survey implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "SurveyID")
    private Long surveyID;
    @Column(name = "Description")
    private String description;
    @Basic(optional = false)
    @Column(name = "StartDate")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Column(name = "EndDate")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    @Basic(optional = false)
    @Column(name = "SurveyURL")
    private String surveyURL;
    @Basic(optional = false)
    @Column(name = "isOpen")
    private boolean isOpen;
    @Basic(optional = false)
    @Column(name = "isCreated")
    private boolean isCreated;
    @Basic(optional = false)
    @Column(name = "isPrivate")
    private boolean isPrivate;
    @JoinColumn(name = "PersonID", referencedColumnName = "PersonID")
    @ManyToOne(optional = false)
    private Person personID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "surveyID")
    private List<Question> questionList;

    public Survey() {
    }

    public Survey(Long surveyID) {
        this.surveyID = surveyID;
    }

    public Survey(Long surveyID, Date startDate, String surveyURL, boolean isOpen, boolean isCreated, boolean isPrivate) {
        this.surveyID = surveyID;
        this.startDate = startDate;
        this.surveyURL = surveyURL;
        this.isOpen = isOpen;
        this.isCreated = isCreated;
        this.isPrivate = isPrivate;
    }

    public Long getSurveyID() {
        return surveyID;
    }

    public void setSurveyID(Long surveyID) {
        this.surveyID = surveyID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getSurveyURL() {
        return surveyURL;
    }

    public void setSurveyURL(String surveyURL) {
        this.surveyURL = surveyURL;
    }

    public boolean getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean getIsCreated() {
        return isCreated;
    }

    public void setIsCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public Person getPersonID() {
        return personID;
    }

    public void setPersonID(Person personID) {
        this.personID = personID;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (surveyID != null ? surveyID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Survey)) {
            return false;
        }
        Survey other = (Survey) object;
        if ((this.surveyID == null && other.surveyID != null) || (this.surveyID != null && !this.surveyID.equals(other.surveyID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "surveydbjpa.Survey[ surveyID=" + surveyID + " ]";
    }
    
}
