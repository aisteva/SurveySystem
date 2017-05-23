package controller;

import DAO.Implementations.AnswerDAO;
import DAO.Implementations.SurveyDAO;
import entitiesJPA.Answer;
import entitiesJPA.OfferedAnswer;
import entitiesJPA.Question;
import entitiesJPA.Survey;
import log.SurveySystemLog;
import entitiesJPA.*;
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

    private Long tempQuestionId;

    public void setTempQuestionId(Long tempQuestionId) {
        this.tempQuestionId = tempQuestionId;
    }

    public void init() {
        //prasideda conversation, kai atidaromas puslapis
        conversation.begin();
        for (Question q : survey.getQuestionList()) {
            if (!questions.containsKey(q.getPage())) {
                questions.put(q.getPage(), new ArrayList<>());
            }
            if (q.getAnswerConnectionList().size() == 0) { //Add only parent questions
                questions.get(q.getPage()).add(q);
            }
            addToTextAndScaleAnswerList(q);
        }
    }

    public void nextPage() {
        page++;
        if (questions.containsKey(page)==false){
            questions.put(page, new ArrayList<>());
        }
    }

    public void prevPage() {
        page--;
        prevPage = true;
//        Map<String,String> params =
//                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
//        String action = params.get("action");
    }

    //Checkbox question - several answers
    public void setSelectedOfferedAnswers(OfferedAnswer[] offered) {
        List<Question> toRemove = new ArrayList<>();
        List<Question> exist = new ArrayList<>();
        for (Question q : questions.get(page)) {
            for (AnswerConnection ac : q.getAnswerConnectionList()) {
                // If Father question == editing question
                if (ac.getOfferedAnswerID().getQuestionID().getQuestionID() == tempQuestionId) {
                    toRemove.add(q);
                    for (OfferedAnswer o : offered) {
                        // Question's answerConnection has offeredAnswer which is in offered array.
                        if (ac.getOfferedAnswerID().getOfferedAnswerID() == o.getOfferedAnswerID()) {
                            toRemove.remove(q);
                            exist.add(q);
                            break;
                        }
                    }
                }
            }
        }
        for (Question q : toRemove) {
            if (textAndScaleAnswersList.containsKey(q.getQuestionID())){
                textAndScaleAnswersList.remove(q.getQuestionID());
            }
            if (checkboxAndMultipleAnswersList.containsKey(q.getQuestionID())){
                checkboxAndMultipleAnswersList.remove(q.getQuestionID());
            }
            questions.get(page).remove(q);
        }
        if (offered.length == 0){
            textAndScaleAnswersList.remove(tempQuestionId);
            checkboxAndMultipleAnswersList.get(tempQuestionId).clear();
            return;
        }

        if (checkboxAndMultipleAnswersList.containsKey(tempQuestionId) == false) {
            checkboxAndMultipleAnswersList.put(tempQuestionId, new ArrayList<>());
        }

        for (OfferedAnswer o : offered) {
            Answer answer = new Answer();
            answer.setOfferedAnswerID(o);
            answer.setSessionID(null);
            o.getAnswerList().add(answer);

            List<Answer> removeUnselected = new ArrayList<>();
            boolean addNewSelected = true;
            for (Answer a : checkboxAndMultipleAnswersList.get(o.getQuestionID().getQuestionID())){
                if (Arrays.asList(offered).contains(a.getOfferedAnswerID()) == false){
                    removeUnselected.add(a);
                }
                if (a.getOfferedAnswerID() == answer.getOfferedAnswerID()){
                    addNewSelected = false;
                }
            }
            for (Answer a : removeUnselected){
                Iterator<Answer> i = checkboxAndMultipleAnswersList.get(tempQuestionId).iterator();
                while (i.hasNext()){
                    if (i.next().getOfferedAnswerID().getOfferedAnswerID() == a.getOfferedAnswerID().getOfferedAnswerID()){
                        i.remove();
                    }
                }
            }
            if (addNewSelected == true){
                checkboxAndMultipleAnswersList.get(tempQuestionId).add(answer);
            }

            for (AnswerConnection ac : o.getAnswerConnectionList()) {
                Question q = ac.getQuestionID();
                if (exist.contains(q)) continue;
                questions.get(page).add(questions.get(page).indexOf(o.getQuestionID())+1,q); //
                addToTextAndScaleAnswerList(q);
            }
        }
    }

    public OfferedAnswer[] getSelectedOfferedAnswers() {
        List<OfferedAnswer> offeredList = new ArrayList<>();
        if (checkboxAndMultipleAnswersList.containsKey(tempQuestionId)) {
            for (Answer a : checkboxAndMultipleAnswersList.get(tempQuestionId)) {
                offeredList.add(a.getOfferedAnswerID());
            }
        }
        return offeredList.toArray(new OfferedAnswer[offeredList.size()]);
    }

    // Multiple question - Only one answer
    public void setSelectedOfferedAnswer(OfferedAnswer offered) {
        if (offered == null){ // Null only in the beginning when checkboxAndMultipleAnswersList is empty
            return;
        }

        OfferedAnswer oldOfferedAnswer; // Removes child question under old offered answer
        if (checkboxAndMultipleAnswersList.containsKey(offered.getQuestionID().getQuestionID())) {
            oldOfferedAnswer = checkboxAndMultipleAnswersList.get(offered.getQuestionID().getQuestionID()).get(0).getOfferedAnswerID();
            for (AnswerConnection ac : oldOfferedAnswer.getAnswerConnectionList()) {
                questions.get(page).remove(ac.getQuestionID());
            }
        }

        Answer answer = new Answer();
        answer.setOfferedAnswerID(offered);
        answer.setSessionID(null);
        offered.getAnswerList().add(answer);

        checkboxAndMultipleAnswersList.put(offered.getQuestionID().getQuestionID(), new ArrayList<>());
        checkboxAndMultipleAnswersList.get(offered.getQuestionID().getQuestionID()).add(answer);

        for (AnswerConnection ac : offered.getAnswerConnectionList()) {
            Question q = ac.getQuestionID();
            questions.get(page).add(questions.get(page).indexOf(offered.getQuestionID())+1, q); //
            addToTextAndScaleAnswerList(q);
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

    public OfferedAnswer getSelectedOfferedAnswer() {
        if (checkboxAndMultipleAnswersList.containsKey(tempQuestionId)) {
            if (checkboxAndMultipleAnswersList.get(tempQuestionId).size() != 0)
                return checkboxAndMultipleAnswersList.get(tempQuestionId).get(0).getOfferedAnswerID();
        }
        return null;
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
                for (Answer a : answerList) {
                    if (a.getText() != "")
                        answerDAO.save(a);
                }
            }

            if ((textAndScaleAnswersList.isEmpty()) && (checkboxAndMultipleAnswersList.isEmpty()))
                log.error("Niekas neišsaugota");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage("show-survey-form:show-survey-message",
                    new FacesMessage("Nepavyko išsaugoti atsakymų"));
        } finally {
            conversation.end();
            return "/index.xhtml";
        }
    }

    //isparsina gautus scale skacius
    public void processLine(List<OfferedAnswer> list) {
        if (!list.isEmpty()) {
            String aLine = list.get(0).getText();
            Scanner scanner = new Scanner(aLine);
            scanner.useDelimiter(";");
            if (scanner.hasNext()) {
                min = Integer.parseInt(scanner.next());
                max = Integer.parseInt(scanner.next());
            } else {
                setCode(FacesContext.getCurrentInstance(), "Nepavyko atvaizduoti apklausos", 400);
            }
        }
    }

    public void validate(FacesContext context, UIComponent component, Object object) {
        //surandam apklausą pagal url
        try {
            survey = surveyDAO.getSurveyByUrl((String) object);
            if (survey == null) {
                setCode(context, "Nėra tokios apklausos", 400);
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



