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
@Table(name = "person")
@NamedQueries({
    @NamedQuery(name = "Person.findAll", query = "SELECT p FROM Person p"),
    @NamedQuery(name = "Person.findByInviteUrl", query = "SELECT p FROM Person p WHERE p.inviteUrl = :inviteUrl"),
    @NamedQuery(name = "Person.findByPersonID", query = "SELECT p FROM Person p WHERE p.personID = :personID"),
    @NamedQuery(name = "Person.findByFirstName", query = "SELECT p FROM Person p WHERE p.firstName = :firstName"),
    @NamedQuery(name = "Person.findByLastName", query = "SELECT p FROM Person p WHERE p.lastName = :lastName"),
    @NamedQuery(name = "Person.findByEmail", query = "SELECT p FROM Person p WHERE p.email = :email"),
    @NamedQuery(name = "Person.findByPassword", query = "SELECT p FROM Person p WHERE p.password = :password"),
    @NamedQuery(name = "Person.findByUserType", query = "SELECT p FROM Person p WHERE p.userType = :userType"),
    @NamedQuery(name = "Person.findByInviteExpiration", query = "SELECT p FROM Person p WHERE p.inviteExpiration = :inviteExpiration"),
    @NamedQuery(name = "Person.findByIsBlocked", query = "SELECT p FROM Person p WHERE p.isBlocked = :isBlocked"),
    @NamedQuery(name = "Person.findByEmailAndPassword", query = "SELECT p FROM Person p WHERE (p.email = :email) AND (p.password = :password)"),
    @NamedQuery(name = "Person.findPendingPersons", query = "SELECT p FROM Person p WHERE (p.firstName <> :PENDING) AND (p.lastName <> :PENDING)")})
@Getter
@Setter
@EqualsAndHashCode(of = "personID")
@ToString(of = "personID")
public class Person implements Serializable {

    public Person(){
        this.userType = "USER";
        this.isBlocked = false;
    };

    public Person(String firstName, String lastName, String email, String password, String userType, Date inviteExpiration) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.userType = userType;
        this.inviteExpiration = inviteExpiration;
        this.isBlocked = false;
    };

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "PersonID")
    private Long personID;
    @Version
    @Column(name = "OPT_LOCK_VERSION")
    private Integer optLockVersion;
    @Basic(optional = false)
    @Column(name = "FirstName")
    private String firstName;
    @Basic(optional = false)
    @Column(name = "LastName")
    private String lastName;
    @Basic(optional = false)
    @Column(name = "Email")
    private String email;
    @Column(name = "Password")
    private String password;
    @Basic(optional = false)
    @Column(name = "UserType")
    private String userType;
    @Basic(optional = false)
    @Column(name = "InviteExpiration")
    @Temporal(TemporalType.TIMESTAMP)
    private Date inviteExpiration;
    @Basic(optional = false)
    @Column(name = "isBlocked")
    private boolean isBlocked;
    @Basic(optional = false)
    @Column(name = "inviteURL")
    private String inviteUrl;
    @OneToMany(cascade = CascadeType.MERGE, fetch = FetchType.EAGER, mappedBy = "personID")
    private List<Survey> surveyList = new ArrayList<>();

    public enum USER_TYPE {
        ADMIN,
        USER
    };

    public USER_TYPE[] getUserTypesEnum(){
        return USER_TYPE.values();
    };

}
