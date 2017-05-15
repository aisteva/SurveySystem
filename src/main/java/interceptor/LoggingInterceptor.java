package interceptor;

import userModule.SignInController;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Aiste on 2017-05-15.
 */
@LogInterceptor
@Interceptor
public class LoggingInterceptor implements Serializable {

    @Resource
    private SessionContext context;

    @Inject private EntityManager entityManager;

    @Inject
    private SignInController signInController;

    private static final long serialVersionUID = 813L;
    Date dateNow = new Date( );
    SimpleDateFormat formatedDate = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss a");


//    @AroundInvoke
//    public Object logMethodEntry(InvocationContext ctx) throws Exception {
//        System.out.println(formatedDate.format(dateNow)+" : Entering Class: " + ctx.getClass().getName()+" METHOD: " +
//                ctx.getMethod().getName());
//        return ctx.proceed();
//    }

    @AroundInvoke
    public Object logMethodEntry(InvocationContext ctx) throws Exception {
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter("/logOutput.txt", true);
            System.out.println();
            pw = new PrintWriter(fw);

            pw.println(formatedDate.format(dateNow)+
                    ", USER: "+ signInController.getLoggedInPerson().getEmail() +
                    ", USER TYPE: "+signInController.getLoggedInPerson().getUserType()+
                    ", ENTERING CLASS: " + ctx.getTarget().getClass().getName()+
                    ", METHOD: " + ctx.getMethod().getName()+";");

            pw.close();
            fw.close();

        } catch (IOException ex) {
            //REIKIA SUGAUTI EXCEPTION
        }


        return ctx.proceed();
    }
}
