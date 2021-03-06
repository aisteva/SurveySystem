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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
@Getter
@Setter
@EqualsAndHashCode(of = "questionID")
@ToString(of = {"questionID", "questionNumber", "questionText", "type", "offeredAnswerList"})
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "QuestionID")
    private Long questionID;
    @Basic(optional = false)
    @Column(name = "QuestionText")
    private String questionText="";
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
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionID", fetch = FetchType.EAGER, orphanRemoval = true)
    private List<OfferedAnswer> offeredAnswerList = new ArrayList<>();
    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "childQuestions")
    private List<OfferedAnswer> parentOfferedAnswers = new ArrayList<>();
    @JoinColumn(name = "SurveyID", referencedColumnName = "SurveyID")
    @ManyToOne(optional = false)
    private Survey surveyID;

    public enum QUESTION_TYPE {
        TEXT,
        CHECKBOX,
        MULTIPLECHOICE,
        SCALE
    };

    public Map<QUESTION_TYPE, String> getQuestionTypesWithLabels() {
        Map<QUESTION_TYPE, String> questionTypesWithLabels = new LinkedHashMap<QUESTION_TYPE, String>();
        questionTypesWithLabels.put(QUESTION_TYPE.TEXT, "Tekstas");
        questionTypesWithLabels.put(QUESTION_TYPE.CHECKBOX, "Keli pasirinkimai");
        questionTypesWithLabels.put(QUESTION_TYPE.MULTIPLECHOICE, "Vienas pasirinkimas");
        questionTypesWithLabels.put(QUESTION_TYPE.SCALE, "Skalė");
        return questionTypesWithLabels;
    }

    public QUESTION_TYPE[] getTypes(){
        return Question.QUESTION_TYPE.values();
    };

    @Transient
    private String newType;



    //Excel exportui - sunumeruoti klausimus nepriklausomai nuo puslapių
    @Transient
    private int questionNumberExcludingPage;

    @Transient
    private OfferedAnswer previousScaleOfferedAnswer;

}
