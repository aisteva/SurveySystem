package controller;

import DAO.Implementations.AnswerDAO;
import DAO.Implementations.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import interceptor.LogInterceptor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by Aiste on 2017-04-06.
 */

@Slf4j
@Named
@ConversationScoped
@LogInterceptor
public class SaveAnswersController implements Serializable{

    @Inject private Conversation conversation;

    @Getter @Setter
    private Survey survey = new Survey();

    @Getter @Setter
    private Map<Long, Answer> textAndScaleAnswersList = new HashMap<>();

    private OfferedAnswer[] selectedOfferedAnswers = new OfferedAnswer[999];

    @Getter @Setter
    private Map<Long, List<Answer>> checkboxAndMultipleAnswersList = new HashMap<>();

    @Getter @Setter
    private OfferedAnswer selectedOfferedAnswer = new OfferedAnswer();

    @Getter @Setter
    int min = 0;

    @Getter @Setter
    int max = 0;

    @Getter @Setter
    private boolean prevPage;

    @Getter
    private int page = 1;

    @Inject
    private AnswerDAO answerDAO;

    @Inject
    private SurveyDAO surveyDAO;

    private Long tempQuestionId;

    public void setTempQuestionId(Long tempQuestionId){
        this.tempQuestionId = tempQuestionId;
    }

    public void init() {
        //prasideda conversation, kai atidaromas puslapis
        conversation.begin();
        for (Question q : survey.getQuestionList()){
            Hibernate.initialize(q.getOfferedAnswerList());
            {
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

    // Check box question
    public void setSelectedOfferedAnswer(OfferedAnswer offered){
        Answer answer = new Answer();
        answer.setOfferedAnswerID(offered);
        answer.setSessionID(null);
        offered.getAnswerList().add(answer);
        checkboxAndMultipleAnswersList.put(offered.getQuestionID().getQuestionID(), new ArrayList<>());
        checkboxAndMultipleAnswersList.get(offered.getQuestionID().getQuestionID()).add(answer);
    }

    public OfferedAnswer getSelectedOfferedAnswer(){
        if (checkboxAndMultipleAnswersList.containsKey(tempQuestionId)) {
            return checkboxAndMultipleAnswersList.get(tempQuestionId).get(0).getOfferedAnswerID();
        }
        return null;
    }

    //Multiple question
    public void setSelectedOfferedAnswers(OfferedAnswer[] offered){
        for (OfferedAnswer o : offered){
            Answer answer = new Answer();
            answer.setOfferedAnswerID(o);;
            answer.setSessionID(null);
            o.getAnswerList().add(answer);
            if (checkboxAndMultipleAnswersList.containsKey(o.getQuestionID().getQuestionID()) == false){
                checkboxAndMultipleAnswersList.put(o.getQuestionID().getQuestionID(), new ArrayList<>());
            }
            checkboxAndMultipleAnswersList.get(o.getQuestionID().getQuestionID()).add(answer);
        }
    }

    public OfferedAnswer[] getSelectedOfferedAnswers(){
        List<OfferedAnswer> offeredList = new ArrayList<OfferedAnswer>();
        if (checkboxAndMultipleAnswersList.containsKey(tempQuestionId)) {
            for (Answer a : checkboxAndMultipleAnswersList.get(tempQuestionId)) {
                offeredList.add(a.getOfferedAnswerID());
            }
        }
        return offeredList.toArray(new OfferedAnswer[offeredList.size()]);
    }


    public List<Question> getQuestionList() {
        return survey.getQuestionList().stream().filter(x -> x.getPage() == page).collect(Collectors.toList());
    }


    @Transactional
    public String saveAnswer() {

        try {
            for (Long l : textAndScaleAnswersList.keySet()) {
                Answer aa = textAndScaleAnswersList.get(l);
                if (aa.getText() != "")
                    answerDAO.save(aa);
            }

            for (Long l : checkboxAndMultipleAnswersList.keySet()) {
                List<Answer> answerList = checkboxAndMultipleAnswersList.get(l);
                for (Answer a : answerList){
                    if (a.getText() != "")
                        answerDAO.save(a);
                }
            }

            if((textAndScaleAnswersList.isEmpty())&&(checkboxAndMultipleAnswersList.isEmpty()))
                log.error("Niekas neišsaugota");
        }
        catch (Exception e){
            FacesContext.getCurrentInstance().addMessage("show-survey-form:show-survey-message",
                    new FacesMessage("Nepavyko išsaugoti atsakymų"));
        }
        finally {
            conversation.end();
            return "/index.xhtml";
        }
    }

    //isparsina gautus scale skacius
    public void processLine(List<OfferedAnswer> list) {
        if(!list.isEmpty()) {
            String aLine = list.get(0).getText();
            Scanner scanner = new Scanner(aLine);
            scanner.useDelimiter(";");
            if(scanner.hasNext()) {
                min = Integer.parseInt(scanner.next());
                max = Integer.parseInt(scanner.next());
            }
            else {
                setCode(FacesContext.getCurrentInstance(), "Nepavyko atvaizduoti apklausos", 400);
            }
        }
    }

    public void validate(FacesContext context, UIComponent component, Object object) {
        //surandam apklausą pagal url
        try {
            survey = surveyDAO.getSurveyByUrl((String) object);
            if(survey == null){
                setCode(context, "Nėra tokios apklausos", 400);
            }
        } catch (Exception e) {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }
    }

    //Funkcija HTTP 400 Error kvietimui
    private void setCode(FacesContext context, String message, int code)
    {
        try
        {
            context.getExternalContext().responseSendError(code, message);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        context.responseComplete();
    }
}



