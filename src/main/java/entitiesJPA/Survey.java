/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entitiesJPA;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    @NamedQuery(name = "Survey.findByIsPrivate", query = "SELECT s FROM Survey s WHERE s.isSurveyPrivate = :isPrivate")})
@Getter
@Setter
@EqualsAndHashCode(of = "surveyID")
@ToString(of = {"surveyID", "questionList"})
public class Survey implements Serializable {

    public Survey(){};

    public Survey(String description, Date startDate, String surveyURL, boolean isOpen, boolean isCreated, boolean isPrivate, Person personID) {
        this.description = description;
        this.startDate = startDate;
        this.surveyURL = surveyURL;
        this.isOpen = isOpen;
        this.isCreated = isCreated;
        this.isSurveyPrivate = isPrivate;
        this.personID = personID;
        this.questionList = new ArrayList<>();
        this.submits = 0l;
    }

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "SurveyID")
    private Long surveyID;
    @Version
    @Column(name = "OPT_LOCK_VERSION")
    private Integer optLockVersion;
    @Column(name = "Title")
    private String title="";
    @Column(name = "Description")
    private String description="";
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
    private boolean isSurveyPrivate;
    @Column(name = "submits")
    private Long submits=0l;
    @JoinColumn(name = "PersonID", referencedColumnName = "PersonID")
    @ManyToOne(optional = false)
    private Person personID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "surveyID")
    @OrderBy("QuestionNumber ASC")
    private List<Question> questionList = new ArrayList<>();
}
