package Controllers;

import Controllers.Interfaces.ISaveAnswersController;
import DAO.Implementations.AnswerDAO;
import DAO.Implementations.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import services.EmailService;
import services.interfaces.MessageGenerator;
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
public class SaveAnswersController implements ISaveAnswersController, Serializable
{

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
    private MessageGenerator mesg;

    @Inject
    private SaltGenerator sg;

    @Inject
    private EmailService es;

    private final String emailText = "Laba diena, jums atsiusta nebaigtos pildyti apklausos nuoroda" +
            " Noredami uzbaigti pildyti apklausa spauskite sia nuoroda: " +
            "http://localhost:8080/surveyAnswers/showSurvey.html?id=%s";

    private final String emailSubject = "Survey System: Nebaigta pildyti apklausa";

    @Getter
    @Setter
    private String sessionId="";

    @Getter @Setter
    private String email;

    @Getter @Setter private List<Answer> sessionAnswerList = new ArrayList<>();

    @Getter
    @Setter
    Map<OfferedAnswer, Boolean> selections = new HashMap<>();

    @Getter @Setter
    private boolean error;

    public void init()
    {
        // Prasideda conversation, kai atidaromas puslapis
        conversation.begin();
        for (Question q : survey.getQuestionList())
        {
            if (!questions.containsKey(q.getPage()))
            {
                questions.put(q.getPage(), new ArrayList<>());
            }
            if (q.getParentOfferedAnswers().size() == 0)
            { //Add only parent questions
                questions.get(q.getPage()).add(q);
            }
            if (q.getType().equals(Question.QUESTION_TYPE.CHECKBOX.toString()) ||
                    q.getType().equals(Question.QUESTION_TYPE.MULTIPLECHOICE.toString()))
            {
                for (OfferedAnswer o : q.getOfferedAnswerList())
                {
                    Set<Question> hs = new HashSet<>();
                    hs.addAll(o.getChildQuestions());
                    o.getChildQuestions().clear();
                    o.getChildQuestions().addAll(hs);
                    boolean matched = false;
                    for(Answer answer: sessionAnswerList)
                    {
                        if(answer.getOfferedAnswerID().getOfferedAnswerID().equals(o.getOfferedAnswerID()))
                        {
                            matched = true;
                            selections.put(o, false);
                            if (q.getType().equals(Question.QUESTION_TYPE.CHECKBOX.toString())) {
                                changeCheckBoxValue(q, o);
                            } else if (q.getType().equals(Question.QUESTION_TYPE.MULTIPLECHOICE.toString())) {
                                changeMultipleValue(q, o);
                            }
                        }
                    }
                    if(!matched)
                    {
                        selections.put(o, false);
                    }

                }
            }
            addToTextAndScaleAnswerList(q);
        }
    }

    public void nextPage() {

        page++;
    }

    public void prevPage()
    {
        page--;
        prevPage = true;
    }

    private void deleteChildAndTheirChildQuestions(OfferedAnswer offeredAnswer)
    {
        for (Question childQuestion : offeredAnswer.getChildQuestions())
        {
            questions.get(childQuestion.getPage()).remove(childQuestion);
            for (OfferedAnswer oa : childQuestion.getOfferedAnswerList())
            {
                deleteChildAndTheirChildQuestions(oa);
                selections.remove(oa);
            }
            if (checkboxAndMultipleAnswersList.containsKey(childQuestion.getQuestionID()))
            {
                checkboxAndMultipleAnswersList.remove(childQuestion.getQuestionID());
            }
            if (textAndScaleAnswersList.containsKey(childQuestion.getQuestionID()))
            {
                textAndScaleAnswersList.remove(childQuestion.getQuestionID());
            }
        }
    }

    public void changeCheckBoxValue(Question q, OfferedAnswer o)
     {
        if (selections.get(o) == true)
        { // To false
            selections.put(o, false);
            // Remove old choice
            if (checkboxAndMultipleAnswersList.containsKey(q.getQuestionID()))
            {
                Answer removeAnswer = null;
                checkboxAndMultipleAnswersList.get(o.getQuestionID().getQuestionID()).remove(removeAnswer);
                for(Iterator<Map.Entry<Long, List<Answer>>> iterator = checkboxAndMultipleAnswersList.entrySet().iterator(); iterator.hasNext(); )
                {
                    int indRemove = 0;
                    Map.Entry<Long, List<Answer>> entry  = iterator.next();
                    for(Answer a: entry.getValue())
                    {
                        if(a.getOfferedAnswerID().getOfferedAnswerID().equals(o.getOfferedAnswerID()))
                        {
                            a.setSessionID(null);
                            removeAnswer = a;
                            break;
                        }
                        indRemove++;
                    }
                    if (removeAnswer != null){
                        removeAnswer.getOfferedAnswerID().getAnswerList().remove(removeAnswer);
                        entry.getValue().remove(indRemove);
                        break;
                    }
                }
            }

            for(Answer a: sessionAnswerList)
            {
                if(a.getOfferedAnswerID().getOfferedAnswerID().equals(o.getOfferedAnswerID()))
                {
                    a.setSessionID(null);
                    o.getAnswerList().remove(a);
                }
            }
            deleteChildAndTheirChildQuestions(o);

        }
        else
        { // To true
            selections.put(o, true);
            Answer answer = new Answer();
            answer.setSessionID(null);
            for(Answer a: sessionAnswerList)
            {
                if(a.getOfferedAnswerID().getOfferedAnswerID().equals(o.getOfferedAnswerID()))
                {
                    answer = a;
                    answer.setSessionID(sessionId);
                }
            }
            answer.setOfferedAnswerID(o);
            o.getAnswerList().add(answer);
            if (!checkboxAndMultipleAnswersList.containsKey(o.getQuestionID().getQuestionID()))
            {
                checkboxAndMultipleAnswersList.put(o.getQuestionID().getQuestionID(), new ArrayList<>());
            }
            checkboxAndMultipleAnswersList.get(o.getQuestionID().getQuestionID()).add(answer);
            for (Question childQuestion : o.getChildQuestions())
            {
                if (childQuestion.getQuestionNumber()-1 > questions.get(childQuestion.getPage()).size()){
                    questions.get(childQuestion.getPage()).add(childQuestion);
                } else {
                    questions.get(childQuestion.getPage()).add(childQuestion.getQuestionNumber() - 1, childQuestion);
                }
                for (OfferedAnswer oo : childQuestion.getOfferedAnswerList())
                {
                    selections.put(oo, false);
                }
                addToTextAndScaleAnswerList(childQuestion);
            }
        }
    }

    public void changeMultipleValue(Question q, OfferedAnswer o)
    {
        if (selections.get(o) == false)
        { // To true new choice
            selections.put(o, true);
            Answer answer = new Answer();
            answer.setOfferedAnswerID(o);
            answer.setSessionID(null);
            o.getAnswerList().add(answer);

            if (checkboxAndMultipleAnswersList.containsKey(q.getQuestionID()))
            {
                Answer oldAnswer = checkboxAndMultipleAnswersList.get(q.getQuestionID()).get(0);
                for(Answer a: sessionAnswerList)
                {
                    if(a.getOfferedAnswerID().getOfferedAnswerID().equals(oldAnswer.getOfferedAnswerID().getOfferedAnswerID()))
                    {
                        a.setSessionID(null);
                    }
                }
                checkboxAndMultipleAnswersList.get(q.getQuestionID()).clear();
                checkboxAndMultipleAnswersList.get(q.getQuestionID()).add(answer);
                OfferedAnswer oldOfferedAnswer = oldAnswer.getOfferedAnswerID();
                selections.put(oldOfferedAnswer, false);
                oldOfferedAnswer.getAnswerList().remove(oldAnswer);

                // Removes old child questions
                deleteChildAndTheirChildQuestions(oldOfferedAnswer);

            } else
            {
                checkboxAndMultipleAnswersList.put(q.getQuestionID(), new ArrayList<>());
                checkboxAndMultipleAnswersList.get(q.getQuestionID()).add(answer);
            }

            // Add new child questions
            for (Question childQuestion : o.getChildQuestions())
            {
                for (OfferedAnswer oo : childQuestion.getOfferedAnswerList())
                {
                    selections.put(oo, false);
                }
                if (childQuestion.getQuestionNumber() - 1 > questions.get(childQuestion.getPage()).size()){
                    questions.get(childQuestion.getPage()).add(childQuestion);
                } else {
                    questions.get(childQuestion.getPage()).add(childQuestion.getQuestionNumber() - 1, childQuestion);
                }
                addToTextAndScaleAnswerList(childQuestion);
            }
        }
    }

    private void addToTextAndScaleAnswerList(Question q)
    {
        Hibernate.initialize(q.getOfferedAnswerList());
        for (OfferedAnswer o : q.getOfferedAnswerList())
        {
            Hibernate.initialize(o.getAnswerList());
            Answer a = new Answer();
            for(Answer answer: sessionAnswerList)
            {
                //tikrinimas apklausos atsakymo pratęsimui: jei nesutaps nė vieną kartą, kuriam naują answer
                if(o.getOfferedAnswerID().equals(answer.getOfferedAnswerID().getOfferedAnswerID()))
                {
                    a = answer;
                }
            }

            if (q.getType().equals("TEXT"))
            {
                o.getAnswerList().add(a);
                a.setOfferedAnswerID(o);
                textAndScaleAnswersList.put(q.getQuestionID(), a);
            }
            if (q.getType().equals("SCALE"))
            {

                o.getAnswerList().add(a);
                a.setOfferedAnswerID(o);
                textAndScaleAnswersList.put(q.getQuestionID(), a);
            }
        }
    }

    //patikrina, ar yra atsakytą nors į vieną klausimą
    public void saveAnswer(boolean isFinished)
    {
        int number=0;

        System.out.println(textAndScaleAnswersList.size());

        //iteruoja per mapą ir ištrina, jei atsakymas yra tuščias, kadangi prieš tai visiems atsakymas liste buvo užsetintas id
        for (Iterator<Map.Entry<Long, Answer>> it = textAndScaleAnswersList.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<Long, Answer> entry = it.next();
            if (entry.getValue().getText() == null || entry.getValue().getText() == "")
            {
               number++;
            }

        }

        //conversation.end();
        //jei neatsakyta nei i viena klausima metama zinute
        if ((textAndScaleAnswersList.size() == number) && (checkboxAndMultipleAnswersList.isEmpty()))
        {
            if(isFinished == true)
                mesg.sendMessage(FacesMessage.SEVERITY_ERROR, "Neatsakyta nei į vieną klausimą, todėl atsakymas neišsaugotas ");
            else
                mesg.redirectToErrorPage("Neatsakyta nei į vieną klausimą, todėl atsakymas neišsaugotas ");
            error = true;
            log.error("Niekas neišsaugota");
        } else
        {
            self.saveAnswerTransaction(isFinished);
            conversation.end();
        }
    }

    //metodas, kuris tikrina, ar atsakytą į privalomus klausimus
    public void checkIfAnsweredCorrectlly(boolean isFinished) throws IOException {



        for (Iterator<Map.Entry<Long, Answer>> it = textAndScaleAnswersList.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<Long, Answer> entry = it.next();
            OfferedAnswer of = entry.getValue().getOfferedAnswerID();
            Question q = of.getQuestionID();
            if(q.isRequired() && (entry.getValue().getText() == null|| entry.getValue().getText() =="")){
                mesg.sendMessage(FacesMessage.SEVERITY_ERROR, "Neatsakėte į privalomą klausimą");
                return;
            }

            //tikrina scale reikšmę
            if(q.getType().equals("SCALE") && entry.getValue().getText() != null && entry.getValue().getText() !="" && (Integer.parseInt(entry.getValue().getText())<processLine(q.getOfferedAnswerList()).getMin() || Integer.parseInt(entry.getValue().getText())>processLine(q.getOfferedAnswerList()).getMax())){
                mesg.sendMessage(FacesMessage.SEVERITY_ERROR, "Įvesta bloga scale reiškmė");
                return;
            }
        }


        for(Question q: survey.getQuestionList())
        {
            if(q.isRequired() && (q.getType().equals("MULTIPLECHOICE") || q.getType().equals("CHECKBOX")) && checkboxAndMultipleAnswersList.containsKey(q.getQuestionID())== false) {
                mesg.sendMessage(FacesMessage.SEVERITY_ERROR, "Neatsakėte į privalomą klausimą");
                return;
            }
        }
    if(isFinished==true)
        saveAnswer(isFinished);
    }


    @Transactional
    public void saveAnswerTransaction(boolean isFinished)
    {
        try
        {
            if(sessionId == "") sessionId = sg.getRandomString(15);
            for (Long l : textAndScaleAnswersList.keySet())
            {
                Answer a = textAndScaleAnswersList.get(l);
                if (a.getText() != null && a.getText() != "")
                {
                    //nusetina sesijos id
                    a.setSessionID(sessionId);
                    //nustato, kad i apklausa baigta atsakineti
                    a.setFinished(isFinished);
                } else
                {
                    OfferedAnswer of = a.getOfferedAnswerID();
                    of.getAnswerList().remove(a);
                    //a.setOfferedAnswerID(null);
                }
            }

            for (Long l : checkboxAndMultipleAnswersList.keySet())
            {
                List<Answer> answerList = checkboxAndMultipleAnswersList.get(l);
                for (Answer a : answerList)
                {
                    if (a.getOfferedAnswerID() != null)
                    {
                        //nusetina sesijos id
                        a.setSessionID(sessionId);
                        //nustato, kad i apklausa baigta atsakineti
                        a.setFinished(isFinished);
                    }
                }
            }

                self.increaseSubmits(isFinished);
            //conversation.end();
            mesg.redirectToSuccessPage("Apklausa išsaugota");

        } catch (Exception e)
        {
            //conversation.end();
            mesg.redirectToErrorPage("Nepavyko išsaugoti apklausos");
        }
    }

    //metodas padidinantis atsakytu apklausu skaiciu + survey submits optimistic locking
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void increaseSubmits(Boolean isFinished) throws Exception
    {
        try
        {
            //tikrinama, ar survey nebuvo ištrinta
            if (surveyDAO.getSurveyByUrl(survey.getSurveyURL()) == null)
            {
                //exception metimas, kad būtų užbaigtas conversation
                throw new Exception();
            }
            if(isFinished==true){
                survey.setSubmits(survey.getSubmits() + 1);
            }

            surveyDAO.update(survey);
        } catch (OptimisticLockException ole)
        {
            conflictingSurvey = surveyDAO.getSurveyByUrl(survey.getSurveyURL());
            self.solveSubmits(isFinished);
        }

    }

    //metodas perraso naujai survey su konfliktuojancio submits skaiciaus survey versija
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void solveSubmits(Boolean isFinished) throws Exception
    {
        survey.setOptLockVersion(conflictingSurvey.getOptLockVersion());
        increaseSubmits(isFinished);
    }

    //isparsina gautus scale skacius
    public ScaleLimits processLine(List<OfferedAnswer> list) throws IOException
    {
        min = 0;
        max = 0;
        if (!list.isEmpty())
        {
            String aLine = list.get(0).getText();
            Scanner scanner = new Scanner(aLine);
            scanner.useDelimiter(";");
            if (scanner.hasNext())
            {
                min = Integer.parseInt(scanner.next());
                max = Integer.parseInt(scanner.next());
            } else
            {

                mesg.redirectToErrorPage("Nepavyko atvaizduoti apklausos");

            }
        }
        return new ScaleLimits(min, max);
    }

    public void validate(FacesContext context, UIComponent component, Object object) throws IOException
    {
        //surandam apklausą pagal url
        try
        {
            survey = surveyDAO.getSurveyByUrl((String) object);
            //gaunam šiandien dienos datą
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            //tikrinam ar apklausa dar galioja
            if (survey.getEndDate() != null)
            {
                if (survey.getEndDate().before(date))
                    mesg.redirectToErrorPage("Apklausa nebegalioja");
            }
            //tikrinam ar apklausa dar galioja
            if (survey.getStartDate() != null)
            {
                if (survey.getStartDate().after(date))
                    mesg.redirectToErrorPage("Apklausa dar negalioja");
            }
            if (survey == null)
            {
                mesg.redirectToErrorPage("Tokios apklausos nėra");
            }
        } catch (Exception e)
        {
            mesg.redirectToErrorPage("Kažkas nutiko...");
            mesg.redirectToErrorPage("Tokios apklausos nėra");
        }
    }

    //tikrina pagla sessionID, jei norima atsakyti i nebaigta apklausa
    public void validateSession(FacesContext context, UIComponent component, Object object)
    {
        try
        {
            if (!object.equals(""))
            {
                //gauna pagal sesija
                sessionAnswerList = answerDAO.getSessionAnswers((String) object);
                if (sessionAnswerList.isEmpty())
                {
                    mesg.redirectToErrorPage("Tokios apklausos nėra");
                } else
                {   //patikrina, ar nebaigti atsakyti
                    for (Answer answer : sessionAnswerList)
                    {
                        if (answer.isFinished())
                        {
                            mesg.redirectToErrorPage("Apklausa jau atsakyta");
                            break;
                        }
                    }
                }
            }

        } catch (Exception e)
        {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }
    }

    public void sendUnfinishedSurvey(){

        try{
            saveAnswer(false);
            if(error==false) {
                String text = String.format(emailText, survey.getSurveyURL());
                String lastText = text + "&sessionId=%s";
                System.out.println(sessionId);
                if (email != null)
                    //System.out.println(lastText);
                    es.sendEmail(email, emailSubject, String.format(lastText, sessionId));
                else {
                    mesg.sendMessage(FacesMessage.SEVERITY_ERROR, "Neįvestas email adresas");
                }
            }

        }catch (Exception e){

        }

    }

}



