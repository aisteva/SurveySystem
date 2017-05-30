package Controllers;

import Controllers.Interfaces.ICreateFormController;
import DAO.Implementations.PersonDAO;
import DAO.Implementations.SurveyDAO;
import entitiesJPA.*;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;
import services.interfaces.MessageGenerator;
import services.SaltGenerator;
import services.excel.IExcelSurveyImport;
import userModule.SignInPerson;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by vdeiv on 2017-04-07.
 */
@Named
@ViewScoped
@Getter
@SurveySystemLog
public class CreateFormController implements ICreateFormController, Serializable {

    @Inject
    private SaltGenerator sg;

    @Inject
    private MessageGenerator msg;

    private Survey survey = new Survey();

    private int page = 1;

    @Inject
    private PersonDAO personDAO;
    @Inject
    private SurveyDAO surveyDAO;

    @Inject
    private SignInPerson person;

    private List<List<Question>> questions = new ArrayList<>();

    private File excelFile;

    List<Integer> pages = new ArrayList<>(); // Starts from 0. But first value is 1.

    @Inject
    IExcelSurveyImport excelSurveyImport;

    @Getter
    @Setter
    private boolean isImported;

    @Getter
    @Setter
    boolean isEditMode = false;

    public CreateFormController() {
        pages.add(1);
        questions.add(new ArrayList<>()); //0 page is empty and not used.  // Pages starts from 1.
        questions.add(new ArrayList<>());
        addQuestion(-1, 1);
    }

    public List<OfferedAnswer> getOfferedAnswers(final int questionIndex, final int page) {
        return questions.get(page).get(questionIndex).getOfferedAnswerList();
    }

    public void addPage(final int currentpage) {
        questions.add(currentpage + 1, new ArrayList<>());
        pages.add(pages.size() + 1);
        for (int i = 0; i < pages.size(); i++) {
            pages.set(i, i + 1);
        }
        addQuestion(-1, currentpage + 1);
    }

    public void removePage(final int currentPage) {
        if (currentPage > 1) {
            questions.remove(currentPage);
            pages.remove(pages.size() - 1);
            for (int i = 0; i < pages.size(); i++) {
                pages.set(i, i + 1);
            }
        } else if (currentPage == 1 && pages.size() > 1){ // Because pages first is zero.
            questions.remove(currentPage);
            pages.remove(pages.size() - 1);
            for (int i = 0; i < pages.size(); i++) {
                pages.set(i, i + 1);
            }
        }
    }

    public void removeQuestion(final int questionIndex, final int page) {
        if (questions.get(page).size() != 1) {
            int childQuestions = 0;
            Question q = questions.get(page).get(questionIndex);
            for (OfferedAnswer offeredAnswer : q.getOfferedAnswerList()) {
                childQuestions += offeredAnswer.getChildQuestions().size();
            }
            // All other questions are child, cannot delete because list will be empty
            if (childQuestions + 1 == questions.get(page).size()) {
                msg.sendMessage(FacesMessage.SEVERITY_INFO, "Turi būti nors vienas klausimas");
                return;
            }
            for (OfferedAnswer o : q.getParentOfferedAnswers()) {
                o.getChildQuestions().remove(q);
            }
            for (OfferedAnswer offeredAnswer : q.getOfferedAnswerList()) {
                for (Question childQuestion : offeredAnswer.getChildQuestions()) {
                    removeAllChildAndChildQuestions(childQuestion);
                }
            }
            questions.get(page).remove(questionIndex);
        }
    }

    public void addQuestion(final int prevQuestionIndex, final int page) {
        Question question = new Question();
        question.setSurveyID(survey);
        String type = Question.QUESTION_TYPE.TEXT.toString();
        if (prevQuestionIndex >= 0) {
            type = questions.get(page).get(prevQuestionIndex).getType();
        }
        question.setType(type);
        question.setNewType(type);
        //question.setQuestionNumber(prevQuestionIndex + 1 + 1);  // (clicked) question index + starts with 1 + next question
        question.setPage(page);
        questions.get(page).add(prevQuestionIndex + 1, question);
        addOfferedAnswer(prevQuestionIndex + 1, page);
        if (type.equals(Question.QUESTION_TYPE.SCALE.toString())) {
            addOfferedAnswer(prevQuestionIndex + 1, page);
        }
    }

    public void addChildQuestion(int questionIndex, int page, OfferedAnswer oa) {
        addQuestion(questionIndex, page);
        Question childQuestion = questions.get(page).get(questionIndex + 1);
        childQuestion.getParentOfferedAnswers().add(oa);
        oa.getChildQuestions().add(childQuestion);
    }

    public void removeAnswer(int questionIndex, final int answerIndex, final int page) {
        if (questions.get(page).get(questionIndex).getOfferedAnswerList().size() > 1) {
            OfferedAnswer offeredAnswer = questions.get(page).get(questionIndex).getOfferedAnswerList().get(answerIndex);
            for (Question q : offeredAnswer.getChildQuestions()) {
                removeAllChildAndChildQuestions(q);
            }
            questions.get(page).get(questionIndex).getOfferedAnswerList().remove(answerIndex);
        }
    }

    public void addOfferedAnswer(final int questionIndex, final int page) {
        OfferedAnswer offeredAnswer = new OfferedAnswer();
        Question question = questions.get(page).get(questionIndex);

        offeredAnswer.setQuestionID(question);
        question.getOfferedAnswerList().add(offeredAnswer);
    }

    public void removeAllOfferedAnswers(final int questionIndex, final int page) {
        questions.get(page).get(questionIndex).getOfferedAnswerList().clear();
    }

    public void moveQuestionUp(final int questionIndex, final int page) {
        if (questionIndex != 0) {
            Question currentQuestion = questions.get(page).get(questionIndex);
            if (currentQuestion.getParentOfferedAnswers().size() > 0) { // Can't be higher than offeredAnswer.
                for (OfferedAnswer o : currentQuestion.getParentOfferedAnswers()) {
                    if (o.getQuestionID().getQuestionNumber() + 1 >= currentQuestion.getQuestionNumber()) {
                        return;
                    }
                }
            }

            questions.get(page).get(questionIndex).setQuestionNumber(questionIndex - 1 + 1);
            questions.get(page).get(questionIndex - 1).setQuestionNumber(questionIndex + 1);
            Collections.swap(questions.get(page), questionIndex, questionIndex - 1);
        }
    }

    public void moveQuestionDown(final int questionIndex, final int page) {
        if (questionIndex != questions.get(page).size() - 1) {
            Question currentQuestion = questions.get(page).get(questionIndex);
            if (currentQuestion.getOfferedAnswerList().size() > 0) {
                for (OfferedAnswer oa : currentQuestion.getOfferedAnswerList()) {
                    if (oa.getChildQuestions().size() > 0) {
                        for (Question lowerQuestion : oa.getChildQuestions()) {
                            if (currentQuestion.getQuestionNumber() + 1 <= lowerQuestion.getQuestionNumber()) {
                                return;
                            }
                        }
                    }
                }
            }
            questions.get(page).get(questionIndex).setQuestionNumber(questionIndex + 1 + 1);
            questions.get(page).get(questionIndex + 1).setQuestionNumber(questionIndex + 1);
            Collections.swap(questions.get(page), questionIndex, questionIndex + 1);
        }
    }

    public void movePageUp(final int currentPage) {
        if (currentPage > 1) {
            Collections.swap(questions, currentPage, currentPage - 1);
        }
    }

    public void movePageDown(final int currentPage) {
        if (currentPage < questions.size()-1) {
            Collections.swap(questions, currentPage, currentPage + 1);
        }
    }

    public String getQuestionParentMessage(Question question) {
        String str = "";
        if (question.getParentOfferedAnswers().size() > 0) {
            for (OfferedAnswer oa : question.getParentOfferedAnswers()) {
                int parentIndex = 0;
                for (Question q : questions.get(page)){
                    if (oa.getQuestionID() == q){
                        break;
                    }
                    parentIndex++;
                }
                parentIndex++;
                str = "Jei '" + parentIndex + ". " + oa.getQuestionID().getQuestionText() + "' klausime buvo atsakyta '" + oa.getText()+"'";
            }
            return str;
        } else {
            return "";
        }
    }

    public void validate(FacesContext context, UIComponent component, Object object) {
        //surandam apklausą pagal url
        try {
            isEditMode = true;

            survey = surveyDAO.getSurveyByUrl((String) object);

            //tikrina, ar user yra kurejas
            if (!(survey.getPersonID()).equals(person.getLoggedInPerson())) {
                msg.redirectToErrorPage("Neturite teisių koreguoti apklausą");
            }

            //tikrinam, ar yra tokia survey
            if (survey == null) {
                msg.redirectToErrorPage("Tokios apklausos nėra");
            } else {
                //tikrinam ar jau yra atsakyta
                if (survey.getSubmits() != 0)
                    msg.redirectToErrorPage("Į apklausą jau yra atsakymų, todėl jos redaguoti negalima");
                else {
                    mapQuestions();
                    survey.getQuestionList().clear();
                }
            }

        } catch (Exception e) {
            context.getExternalContext().setResponseStatus(404);
            context.responseComplete();
        }
    }

    public void mapQuestions() {
        /*
        Kadangi konstruktoriuje sukuriam pirmą klausimą, kuris yra tuščias, tai mapinam klausimus tik tokiu atveju, kai
        pirmas klausimas tuščias. Kitu atveju žinom, kad klausimai sumapinti, ir taip išvengiam galimų konfliktų.
         */
        if (questions.get(1).get(0).getQuestionText().equals("")) {
            questions.get(1).remove(questions.get(1).size() - 1); //ištrinam naujai pridėtą klausimą, kurio reikia tik naujai kuriant apklausą
            for (Question q : survey.getQuestionList()) {
                q.setNewType(q.getType()); //kadangi transient laukas, reikia nustatyti mapinant
                if (questions.size() - 1 < q.getPage()) {
                    questions.add(q.getPage(), new ArrayList<>());
                    pages.add(pages.size() + 1);
                    for (int i = 0; i < pages.size(); i++) {
                        pages.set(i, i + 1);
                    }
                }
                questions.get(q.getPage()).add(q.getQuestionNumber() - 1, q);

                if (q.getType().equals(Question.QUESTION_TYPE.SCALE.toString())) {
                    splitScaleAnswer(q);
                }
            }
        }
    }

    private void splitScaleAnswer(Question q) {

        String aLine = q.getOfferedAnswerList().get(0).getText();
        q.setPreviousScaleOfferedAnswer(q.getOfferedAnswerList().get(0));
        Scanner scanner = new Scanner(aLine);
        scanner.useDelimiter(";");
        int min = 0;
        int max = 0;
        if (scanner.hasNext()) {
            min = Integer.parseInt(scanner.next());
            max = Integer.parseInt(scanner.next());
        }
        q.getOfferedAnswerList().clear();
        OfferedAnswer offered = new OfferedAnswer();
        offered.setText(Integer.toString(min));
        offered.setQuestionID(q);
        q.getOfferedAnswerList().add(offered);
        offered = new OfferedAnswer();
        offered.setText(Integer.toString(max));
        offered.setQuestionID(q);
        q.getOfferedAnswerList().add(offered);
    }

    @Transactional
    public String createForm(final String personEmail) {
        //Merging scale offeredAnswer
        if (!surveyIsCorrect()){
            System.out.println("Survey is not correct");
            return null;
        }
        else
        {
            System.out.println("Survey is correct");
        }
        if (!isEditMode) {
            System.out.println("is not edit mode");
            Person person = personDAO.FindPersonByEmail(personEmail);
            survey.setPersonID(person);
            survey.setSurveyURL(sg.getRandomString(15));
            person.getSurveyList().add(survey);
            personDAO.UpdateUser(person);
        } else {
            System.out.println("is edit mode");
            surveyDAO.update(survey);
        }
        return "/create/formCreated.xhtml?faces-redirect=true&id=" + survey.getSurveyURL();
    }

    private boolean surveyIsCorrect() {

        if (survey.getStartDate() == null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date();
            System.out.println(dateFormat.format(date));
            survey.setStartDate(date);
        }
        System.out.println(survey.getStartDate());
        System.out.println(survey.getEndDate());
        if(survey.getStartDate().after(survey.getEndDate())){
            msg.sendMessage(FacesMessage.SEVERITY_ERROR, "Pabaigos data yra ankstesnė nei pradžios");
            return false;
        }
        if (survey.getTitle().equals("")) {
            survey.setTitle("Be pavadinimo");
        }


        boolean isZeroQuestions = true;
        survey.getQuestionList().clear();
        boolean zeroPage = true;
        int page = 1;
        for (List<Question> lst : questions) {
            if (zeroPage) {
                zeroPage = false;
                continue;
            }
            int number = 1;
            for (Question q : lst) {
                q.setQuestionNumber(number);
                q.setPage(page);
                number++;
                isZeroQuestions = false;

                if (q.getQuestionText() == null || q.getQuestionText().isEmpty()) {
                    msg.sendMessage(FacesMessage.SEVERITY_ERROR, "Klausimas yra nenurodytas");
                    return false;
                }

                if (q.getType().equals(Question.QUESTION_TYPE.SCALE.toString())) {
                    OfferedAnswer offeredAnswer;

                    if(isEditMode || (isImported && survey.getSubmits()>0))
                    {
                        offeredAnswer = q.getPreviousScaleOfferedAnswer();
                        if (offeredAnswer == null) {
                            offeredAnswer = new OfferedAnswer();
                        }
                    }
                    else
                    {
                        offeredAnswer = new OfferedAnswer();
                    }
                    if (q.getOfferedAnswerList().get(0).getText() == null || q.getOfferedAnswerList().get(0).getText().isEmpty() ||
                            q.getOfferedAnswerList().get(1).getText() == null || q.getOfferedAnswerList().get(1).getText().isEmpty()) {
                        msg.sendMessage(FacesMessage.SEVERITY_ERROR, q.getPage()+"."+q.getQuestionNumber()+" "+q.getQuestionText() + "scale klausimo rėžiai nenurodyti");
                        return false;
                    }
                    int min, max;
                    min = Integer.parseInt(q.getOfferedAnswerList().get(0).getText());
                    max = Integer.parseInt(q.getOfferedAnswerList().get(1).getText());
                    if (min > max){
                        msg.sendMessage(FacesMessage.SEVERITY_ERROR, q.getPage()+"."+q.getQuestionNumber()+" "+q.getQuestionText() +" Scale klausimo rėžiai netinkami");
                        return false;
                    }
                    offeredAnswer.setText(q.getOfferedAnswerList().get(0).getText() + ";" + q.getOfferedAnswerList().get(1).getText());
                    offeredAnswer.setQuestionID(q);
                    q.getOfferedAnswerList().clear();
                    q.getOfferedAnswerList().add(offeredAnswer);
                }

                for (OfferedAnswer o : q.getOfferedAnswerList()) {
                    if (o.getQuestionID().getType().equals(Question.QUESTION_TYPE.TEXT.toString()))
                        continue;
                    if (o.getText() == null || o.getText().isEmpty()) {
                        msg.sendMessage(FacesMessage.SEVERITY_ERROR, "Nenurodytas klausimo pasirinkimas");
                        return false;
                    }
                }
            }
            if (isZeroQuestions) {
                msg.sendMessage(FacesMessage.SEVERITY_ERROR, "Reikalingas bent vieno klausimo pridėjimas");
                return false;
            }

            survey.getQuestionList().addAll(lst);
            page++;
        }

        return true;
    }

    public void changeQuestionType(final int questionIndex, final int page) {
        Question question = questions.get(page).get(questionIndex);
        if (question.getType().equals(Question.QUESTION_TYPE.TEXT.toString())) { //If was text
            if (question.getNewType().equals(Question.QUESTION_TYPE.SCALE.toString())) {
                addOfferedAnswer(questionIndex, page);
            }
        } else if (question.getType().equals(Question.QUESTION_TYPE.CHECKBOX.toString()) //If was checkbox or multiple
                || question.getType().equals(Question.QUESTION_TYPE.MULTIPLECHOICE.toString())) {
            for (OfferedAnswer offeredAnswer : questions.get(page).get(questionIndex).getOfferedAnswerList()) {
                for (Question q : offeredAnswer.getChildQuestions()) {
                    removeAllChildAndChildQuestions(q);
                }
            }
            if (question.getNewType().equals(Question.QUESTION_TYPE.TEXT.toString())) {
                removeAllOfferedAnswers(questionIndex, page);
                addOfferedAnswer(questionIndex, page);
            } else if (question.getNewType().equals(Question.QUESTION_TYPE.SCALE.toString())) {
                removeAllOfferedAnswers(questionIndex, page);
                addOfferedAnswer(questionIndex, page);
                addOfferedAnswer(questionIndex, page);
            }
        } else if (question.getType().equals(Question.QUESTION_TYPE.SCALE.toString())) { //If was scale
            removeAllOfferedAnswers(questionIndex, page);
            addOfferedAnswer(questionIndex, page);
        }
        question.setType(question.getNewType());
    }

    private void removeAllChildAndChildQuestions(Question question) {
        for (OfferedAnswer o : question.getOfferedAnswerList()) {
            for (Question childQuestion : o.getChildQuestions()) {
                removeAllChildAndChildQuestions(childQuestion);
            }
        }
        int i = 0;
        for (Question q : questions.get(question.getPage())) {
            if (q == question) {
                break;
            }
            i++;
        }
        questions.get(question.getPage()).remove(i);
    }

    public void importExcelFile(FileUploadEvent event) throws IOException {
        isImported = false;
        File tempFile = File.createTempFile("temp", "");
        tempFile.deleteOnExit();
        FileOutputStream out = new FileOutputStream(tempFile);
        InputStream in = event.getFile().getInputstream();
        IOUtils.copy(in, out);
        excelFile = tempFile;
        try {
            survey = excelSurveyImport.importSurveyIntoEntity(excelFile).get();
            mapQuestions();
            isImported = true;
        } catch (InterruptedException | InvalidFormatException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof InvalidFormatException) {
                FacesContext.getCurrentInstance().addMessage("messages",
                        new FacesMessage(e.getCause().getMessage()));
            } else {
                e.printStackTrace();
            }

        }
    }

}
