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
import java.util.List;

@Entity
@Table(name = "offeredanswer")
@NamedQueries({
    @NamedQuery(name = "OfferedAnswer.findAll", query = "SELECT o FROM OfferedAnswer o"),
    @NamedQuery(name = "OfferedAnswer.findByOfferedAnswerID", query = "SELECT o FROM OfferedAnswer o WHERE o.offeredAnswerID = :offeredAnswerID"),
    @NamedQuery(name = "OfferedAnswer.findByText", query = "SELECT o FROM OfferedAnswer o WHERE o.text = :text")})
@Getter
@Setter
@EqualsAndHashCode(of = "offeredAnswerID")
@ToString(of = {"offeredAnswerID", "text", "answerList"})
public class OfferedAnswer implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "OfferedAnswerID")
    private Long offeredAnswerID;
    @Basic(optional = false)
    @Column(name = "Text")
    private String text="";
    @JoinColumn(name = "QuestionID", referencedColumnName = "QuestionID")
    @ManyToOne(optional = false)
    private Question questionID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "offeredAnswerID")
    private List<Answer> answerList = new ArrayList<>();
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "offeredAnswerID")
    private List<AnswerConnection> answerConnectionList = new ArrayList<>();

    //excel importui - su duomenų baze nesusiję
    @Transient
    private int answerNumber;
}
