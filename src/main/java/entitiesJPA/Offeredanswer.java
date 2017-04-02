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
import java.util.List;

@Entity
@Table(name = "offeredanswer")
@NamedQueries({
    @NamedQuery(name = "Offeredanswer.findAll", query = "SELECT o FROM Offeredanswer o"),
    @NamedQuery(name = "Offeredanswer.findByOfferedAnswerID", query = "SELECT o FROM Offeredanswer o WHERE o.offeredAnswerID = :offeredAnswerID"),
    @NamedQuery(name = "Offeredanswer.findByText", query = "SELECT o FROM Offeredanswer o WHERE o.text = :text")})
@Getter
@Setter
@EqualsAndHashCode(of = "offeredAnswerID")
@ToString(of = "offeredAnswerID")
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
}
