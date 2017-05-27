package controller;

import DAO.Implementations.AnswerDAO;
import DAO.Implementations.SurveyDAO;
import entitiesJPA.*;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import services.MessageCreator;
import services.SaltGenerator;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;


/**
 * Created by Aiste on 2017-04-06.
 */

@Slf4j
@Named
@ConversationScoped
@SurveySystemLog
public class SaveAnswersController implements Serializable{

    @Inject
    private Conversation conversation;

    @Getter
    @Setter
    private Survey survey = new Survey();

    @Getter
    @Setter
    private Map<Integer, List<Question>> questions = new HashMap<>();

    @Getter
    @Setter
    private Map<Long, Boolean> childQuestions = new HashMap<>();

    @Getter
    @Setter
    private Map<Long, Answer> textAndScaleAnswersList = new HashMap<>();

    private OfferedAnswer[] selectedOfferedAnswers = new OfferedAnswer[99];

    @Getter
    @Setter
    private Map<Long, List<Answer>> checkboxAndMultipleAnswersList = new HashMap<>();

    @Getter
    @Setter
    private OfferedAnswer selectedOfferedAnswer;

    @Getter
    private Survey conflictingSurvey;

    @Getter
    @Setter
    int min = 0;

    @Getter
    @Setter
    int max = 0;

    @Getter
    @Setter
    private boolean prevPage;

    @Getter
    private int page = 1;

    @Inject
    private AnswerDAO answerDAO;

    @Inject
    private SurveyDAO surveyDAO;

    @Inject
    private SaveAnswersController self;

    @Inject
    private MessageCreator mesg;

    @Inject
    private SaltGenerator sg;

    @Getter @Setter
    Map<OfferedAnswer, Boolean> selections = new HashMap<>();

    public void init() {
        // Prasideda conversation, kai atidaromas puslapis
        conversation.begin();
        for (Question q : survey.getQuestionList()) {
            if (!questions.containsKey(q.getPage())) {
                questions.put(q.getPage(), new ArrayList<>());
            }
            if (q.getParentOfferedAnswers().size() == 0) { //Add only parent questions
                questions.get(q.getPage()).add(q);
            }
            if (q.getType().equals(Question.QUESTION_TYPE.CHECKBOX.toString()) ||
                    q.getType().equals(Question.QUESTION_TYPE.MULTIPLECHOICE.toString()) ){
                for (OfferedAnswer o: q.getOfferedAnswerList()){
                    selections.put(o, false);
                }
            }
            addToTextAndScaleAnswerList(q);
        }
    }

    public void nextPage() {
        page++;
    }

    public void prevPage() {
        page--;
        prevPage = true;
//        Map<String,String> params =
//                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
//        String action = params.get("action");
    }


    public void changeCheckBoxOrMultipleValue(Question q, OfferedAnswer o){
        if (selections.get(o)==true){ // To false
            if (q.getType().equals(Question.QUESTION_TYPE.CHECKBOX)) {
                selections.put(o, false);
                Iterator<Answer> i = checkboxAndMultipleAnswersList.get(q.getQuestionID()).iterator();
                while (i.hasNext()) {
                    if (i.next().getOfferedAnswerID().getOfferedAnswerID() == o.getOfferedAnswerID()) {
                        i.remove();
                        break;
                    }
                }/*
                for (AnswerConnection ac : o.getAnswerConnectionList()) {
                    questions.get(page).remove(ac.getQuestionID());
                    checkboxAndMultipleAnswersList.get(q.getQuestionID()).clear();
                    if (textAndScaleAnswersList.containsKey(q.getQuestionID())) {
                        textAndScaleAnswersList.remove(q.getQuestionID());
                    }
                }
                */
            }
        } else { // To true
            selections.put(o, true);
            Answer answer = new Answer();
            answer.setOfferedAnswerID(o);
            answer.setSessionID(null);
            o.getAnswerList().add(answer);
            if (q.getType().equals(Question.QUESTION_TYPE.MULTIPLECHOICE)){
                for (Answer a : checkboxAndMultipleAnswersList.get(q.getQuestionID())){
                    OfferedAnswer oa = answer.getOfferedAnswerID();
                    selections.put(oa, false);
                    oa.getAnswerList().remove(a);
                 /*   for (AnswerConnection ac : oa.getAnswerConnectionList()) {
                        questions.get(page).remove(ac.getQuestionID());
                        checkboxAndMultipleAnswersList.get(q.getQuestionID()).clear(); //
                        if (textAndScaleAnswersList.containsKey(q.getQuestionID())) {
                            textAndScaleAnswersList.remove(q.getQuestionID());
                        }
                    }
                    */
                }
                checkboxAndMultipleAnswersList.get(q.getQuestionID()).clear();
            }
            if (q.getType().equals(Question.QUESTION_TYPE.MULTIPLECHOICE) || checkboxAndMultipleAnswersList.containsKey(q.getQuestionID())==false){
                checkboxAndMultipleAnswersList.put(q.getQuestionID(), new ArrayList<>());
            }

            checkboxAndMultipleAnswersList.get(q.getQuestionID()).add(answer);
/*
            for (AnswerConnection ac : o.getAnswerConnectionList()) {
                Question question = ac.getQuestionID();
                questions.get(page).add(questions.get(page).indexOf(o.getQuestionID())+1, question); //
                addToTextAndScaleAnswerList(q);
            } */
        }
    }

    private void addToTextAndScaleAnswerList(Question q){
        Hibernate.initialize(q.getOfferedAnswerList());
        for (OfferedAnswer o : q.getOfferedAnswerList()) {
            Hibernate.initialize(o.getAnswerList());
            if (q.getType().equals("TEXT")) {
                Answer a = new Answer();
                o.getAnswerList().add(a);
                a.setOfferedAnswerID(o);
                textAndScaleAnswersList.put(q.getQuestionID(), a);
            }
            if (q.getType().equals("SCALE")) {
                Answer a = new Answer();
                o.getAnswerList().add(a);
                a.setOfferedAnswerID(o);
                textAndScaleAnswersList.put(q.getQuestionID(), a);
            }
        }
    }

    //patikrina, ar yra atsakytą nors į vieną klausimą
    public String saveAnswer(){

        //iteruoja per mapą ir ištrina, jei atsakymas yra tuščias, kadangi prieš tai visiems atsakymas liste buvo užsetintas id
        for(Iterator<Map.Entry<Long, Answer>> it = textAndScaleAnswersList.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Long, Answer> entry = it.next();
            if(entry.getValue().getText() == null)
            {
               // it.remove();
            }

        }

        //conversation.end();
        //jei neatsakyta nei i viena klausima metama zinute

        if ((textAndScaleAnswersList.isEmpty()) && (checkboxAndMultipleAnswersList.isEmpty())){
            mesg.sendMessage(FacesMessage.SEVERITY_INFO, "Neatsakyta nei į vieną klausimą, todėl atsakymas neišsaugotas ");
            log.error("Niekas neišsaugota");
            return null;
        }
        else{
            self.saveAnswerTransaction();

            return null;
            //return "/index.xhtml?faces-redirect=true";
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void saveAnswerTransaction() {
        try {
            String session = sg.getRandomString(15);
            for (Long l : textAndScaleAnswersList.keySet()) {
                Answer a = textAndScaleAnswersList.get(l);
                if (a.getText() != null && a.getText() != "") {
                    //nusetina sesijos id
                    a.setSessionID(session);
                    //nustato, kad i apklausa baigta atsakineti
                    a.setFinished(true);
                }
                else {
                    OfferedAnswer of = a.getOfferedAnswerID();
                    of.getAnswerList().remove(a);
                    a.setOfferedAnswerID(null);
                }
            }

            for (Long l : checkboxAndMultipleAnswersList.keySet()) {
                List<Answer> answerList = checkboxAndMultipleAnswersList.get(l);
                for (Answer a : answerList) {
                    if (a.getOfferedAnswerID() != null) {
                        //nusetina sesijos id
                        a.setSessionID(session);
                        //nustato, kad i apklausa baigta atsakineti
                        a.setFinished(true);
                    }
                }
            }
            self.increaseSubmits();
            conversation.end();
            mesg.redirectToSuccessPage("Apklausa išsaugota");

        } catch (Exception e) {
            conversation.end();
            mesg.redirectToErrorPage("Nepavyko išsaugoti apklausos");
        }
    }

    //metodas padidinantis atsakytu apklausu skaiciu + survey submits optimistic locking
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void increaseSubmits(){
        try {
            survey.setSubmits(survey.getSubmits()+1);
            surveyDAO.update(survey);
            //System.out.println(survey.toString()); //kol kas netrinkit, pasilikau pratestavimui, kai veiks isaugojimas
        } catch (OptimisticLockException ole) {
            conflictingSurvey = surveyDAO.getSurveyByUrl(survey.getSurveyURL());
            //System.out.println("Conflicting: " +conflictingSurvey.toString()); //kol kas netrinkit, pasilikau pratestavimui, kai veiks isaugojimas
            self.solveSubmits();
        }

    }
    //metodas perraso naujai survey su konfliktuojancio submits skaiciaus survey versija
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void solveSubmits(){
        survey.setOptLockVersion(conflictingSurvey.getOptLockVersion());
        //System.out.println("priskirta: " +survey.toString()); //kol kas netrinkit, pasilikau pratestavimui, kai veiks isaugojimas
        increaseSubmits();
    }

    //isparsina gautus scale skacius
    public ScaleLimits processLine(List<OfferedAnswer> list) throws IOException {
        min = 0; max=0;
        if (!list.isEmpty()) {
            String aLine = list.get(0).getText();
            Scanner scanner = new Scanner(aLine);
            scanner.useDelimiter(";");
            if (scanner.hasNext()) {
                min = Integer.parseInt(scanner.next());
                max = Integer.parseInt(scanner.next());
            } else {

                mesg.redirectToErrorPage("Nepavyko atvaizduoti apklausos");

            }
        }
        return new ScaleLimits(min, max);
    }

    public void validate(FacesContext context, UIComponent component, Object object) throws IOException {
        //surandam apklausą pagal url
        try {
            survey = surveyDAO.getSurveyByUrl((String) object);
            if (survey == null) {
                mesg.redirectToErrorPage("Tokios apklausos nėra");
            }
        } catch (Exception e) {
           mesg.redirectToErrorPage("Tokios apklausos nėra");
        }
    }




    //Funkcija HTTP 400 Error kvietimui
    private void setCode(FacesContext context, String message, int code) {
        try {
            context.getExternalContext().responseSendError(code, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
        context.responseComplete();
    }
}



