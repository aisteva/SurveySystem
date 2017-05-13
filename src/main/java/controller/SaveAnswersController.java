package controller;

import dao.AnswerDAO;
import dao.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.omnifaces.cdi.ViewScoped;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
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
public class SaveAnswersController implements Serializable{

    @Inject private Conversation conversation;

    @Getter @Setter
    private Survey survey = new Survey();

    @Getter @Setter
    private List<Answer> answersList = new ArrayList<>();

    @Getter @Setter
    private Map<Long, Answer> textAndScaleAnswersList = new HashMap<>();

    @Getter @Setter
    private OfferedAnswer[] selectedOfferedAnswers = new OfferedAnswer[20];

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

    public void setSelectedOfferedAnswers(OfferedAnswer[] offered){
        for (OfferedAnswer o : offered){
            Answer answer = new Answer();
            answer.setText(o.getText());
            answer.setOfferedAnswerID(o);;
            answer.setSessionID(null);
            o.getAnswerList().add(answer);
            answersList.add(answer);
        }
    }

    public void setSelectedOfferedAnswer(OfferedAnswer offered){

        if(offered!=null)
            setSelectedOfferedAnswers(new OfferedAnswer[]{offered});
    }

    public List<Question> getQuestionList() {
        // if (survey.getQuestionList().stream().filter(x -> x.getPage() == page).collect(Collectors.toList()).size() == 0)
            //addQuestion(-1);
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
            for (Answer a : answersList) {
                if (a.getText() != "")
                    answerDAO.save(a);
            }

            if((textAndScaleAnswersList.isEmpty())&&(answersList.isEmpty()))
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



