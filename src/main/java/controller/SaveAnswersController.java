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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Aiste on 2017-04-06.
 */

@Slf4j
@Named
@ConversationScoped
@SurveySystemLog
public class SaveAnswersController implements Serializable {

    @Inject
    private Conversation conversation;

    @Getter
    @Setter
    private Survey survey = new Survey();

    @Getter
    @Setter //Page, questions
    private Map<Integer, List<Question>> questions = new HashMap<>();

    @Getter
    @Setter //QuestionId, Answer
    private Map<Long, Answer> textAndScaleAnswersList = new HashMap<>();

    @Getter
    @Setter //QuestionId, Answer
    private Map<Long, List<Answer>> checkboxAndMultipleAnswersList = new HashMap<>();

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

    @Getter @Setter private String sessionId;

    @Getter
    @Setter
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
                    q.getType().equals(Question.QUESTION_TYPE.MULTIPLECHOICE.toString())) {
                for (OfferedAnswer o : q.getOfferedAnswerList()) {
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
    }

    private void deleteChildAndTheirChildQuestions(OfferedAnswer offeredAnswer){
        for (Question childQuestion : offeredAnswer.getChildQuestions()){
            questions.get(childQuestion.getPage()).remove(childQuestion);
            for (OfferedAnswer oa : childQuestion.getOfferedAnswerList()){
                deleteChildAndTheirChildQuestions(oa);
                selections.remove(oa);
            }
            if (checkboxAndMultipleAnswersList.containsKey(childQuestion.getQuestionID())){
                checkboxAndMultipleAnswersList.remove(childQuestion.getQuestionID());
            }
            if (textAndScaleAnswersList.containsKey(childQuestion.getQuestionID())){
                textAndScaleAnswersList.remove(childQuestion.getQuestionID());
            }
        }
    }

    public void changeCheckBoxValue(Question q, OfferedAnswer o) {
        if (selections.get(o) == true) { // To false
            selections.put(o, false);
            // Remove old choice
            if (checkboxAndMultipleAnswersList.containsKey(q.getQuestionID())){
                checkboxAndMultipleAnswersList.get(q.getQuestionID()).remove(o);
            }
            deleteChildAndTheirChildQuestions(o);

        } else { // To true
            selections.put(o, true);
            Answer answer = new Answer();
            answer.setOfferedAnswerID(o);
            answer.setSessionID(null);
            o.getAnswerList().add(answer);
            if (!checkboxAndMultipleAnswersList.containsKey(o.getQuestionID().getQuestionID())) {
                checkboxAndMultipleAnswersList.put(o.getQuestionID().getQuestionID(), new ArrayList<>());
            }
            checkboxAndMultipleAnswersList.get(o.getQuestionID().getQuestionID()).add(answer);
            for (Question childQuestion : o.getChildQuestions()){
                questions.get(childQuestion.getPage()).add(childQuestion);
                for (OfferedAnswer oo : childQuestion.getOfferedAnswerList()){
                    selections.put(oo, false);
                }
                addToTextAndScaleAnswerList(childQuestion);
            }
        }
    }

    public void changeMultipleValue(Question q, OfferedAnswer o) {
        if (selections.get(o) == false) { // To true new choice
            selections.put(o, true);
            Answer answer = new Answer();
            answer.setOfferedAnswerID(o);
            answer.setSessionID(null);
            o.getAnswerList().add(answer);

            if (checkboxAndMultipleAnswersList.containsKey(q.getQuestionID())) {
                Answer oldAnswer = checkboxAndMultipleAnswersList.get(q.getQuestionID()).get(0);
                checkboxAndMultipleAnswersList.get(q.getQuestionID()).clear();
                checkboxAndMultipleAnswersList.get(q.getQuestionID()).add(answer);
                OfferedAnswer oldOfferedAnswer = oldAnswer.getOfferedAnswerID();
                selections.put(oldOfferedAnswer, false);
                oldOfferedAnswer.getAnswerList().remove(oldAnswer);

                // Removes old child questions
                deleteChildAndTheirChildQuestions(oldOfferedAnswer);

            } else {
                checkboxAndMultipleAnswersList.put(q.getQuestionID(), new ArrayList<>());
                checkboxAndMultipleAnswersList.get(q.getQuestionID()).add(answer);
            }

            // Add new child questions
            for (Question childQuestion : o.getChildQuestions()){
                for (OfferedAnswer oo : childQuestion.getOfferedAnswerList()) {
                    selections.put(oo, false);
                }
                questions.get(childQuestion.getPage()).add(childQuestion);
                addToTextAndScaleAnswerList(childQuestion);
            }
        }
    }

    private void addToTextAndScaleAnswerList(Question q) {
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
    public void saveAnswer() {

        //iteruoja per mapą ir ištrina, jei atsakymas yra tuščias, kadangi prieš tai visiems atsakymas liste buvo užsetintas id
        for (Iterator<Map.Entry<Long, Answer>> it = textAndScaleAnswersList.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, Answer> entry = it.next();
            if (entry.getValue().getText() == null) {
                OfferedAnswer of = entry.getValue().getOfferedAnswerID();
                of.getAnswerList().remove(entry.getValue());
                 it.remove();
            }

        }
        System.out.println(textAndScaleAnswersList.toString());

        //conversation.end();
        //jei neatsakyta nei i viena klausima metama zinute

        if ((textAndScaleAnswersList.isEmpty()) && (checkboxAndMultipleAnswersList.isEmpty())) {
            mesg.sendMessage(FacesMessage.SEVERITY_INFO, "Neatsakyta nei į vieną klausimą, todėl atsakymas neišsaugotas ");
            log.error("Niekas neišsaugota");
        } else {
            self.saveAnswerTransaction(true);
        }
    }



    @Transactional
    public void saveAnswerTransaction(boolean isFinished) {
        try {
            String session = sg.getRandomString(15);
            for (Long l : textAndScaleAnswersList.keySet()) {
                Answer a = textAndScaleAnswersList.get(l);
                System.out.println(a.toString());
                if (a.getText() != null && a.getText() != "") {
                    //nusetina sesijos id
                    a.setSessionID(session);
                    //nustato, kad i apklausa baigta atsakineti
                    a.setFinished(isFinished);
                } else {
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
                        a.setFinished(isFinished);
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
    public void increaseSubmits() throws Exception {
        try {
            //tikrinama, ar survey nebuvo ištrinta
            if(surveyDAO.getSurveyByUrl(survey.getSurveyURL())== null){
                //exception metimas, kad būtų užbaigtas conversation
                throw new Exception();
            }

            survey.setSubmits(survey.getSubmits() + 1);
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
    public void solveSubmits() throws Exception {
        survey.setOptLockVersion(conflictingSurvey.getOptLockVersion());
        //System.out.println("priskirta: " +survey.toString()); //kol kas netrinkit, pasilikau pratestavimui, kai veiks isaugojimas
        increaseSubmits();
    }

    //isparsina gautus scale skacius
    public ScaleLimits processLine(List<OfferedAnswer> list) throws IOException {
        min = 0;
        max = 0;
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
            //gaunam šiandien dienos datą
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            System.out.println(dateFormat.format(date));
            //tikrinam ar apklausa dar galioja
            if(survey.getEndDate() != null) {
                if(survey.getEndDate().before(date))
                    mesg.redirectToErrorPage("Apklausa nebegalioja");
            }
            if (survey == null) {
                mesg.redirectToErrorPage("Tokios apklausos nėra");
            }
        } catch (Exception e) {
           mesg.redirectToErrorPage("Kažkas nutiko...");
           mesg.redirectToErrorPage("Tokios apklausos nėra");
        }
    }

    public void validateSession(FacesContext context, UIComponent component, Object object){
        try{
            System.out.println(object);
            if(!object.equals(""))
            {
                List<Answer> answersList = answerDAO.getSessionAnswers((String) object);
                System.out.println(answersList);
                if(answersList.isEmpty()){
                    mesg.redirectToErrorPage("Tokios apklausos nėra");
                }
                else
                {
                    for(Answer answer: answersList)
                    {
                        if(answer.isFinished())
                        {
                            mesg.redirectToErrorPage("Apklausa jau atsakyta");
                            break;
                        }
                    }
                }
            }


        } catch (Exception e) {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
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



