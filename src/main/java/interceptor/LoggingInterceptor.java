package interceptor;

import org.hibernate.Session;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Aiste on 2017-05-15.
 */
@LogInterceptor
@Interceptor
public class LoggingInterceptor implements Serializable {

    @Resource
    private SessionContext context;

    @Inject private EntityManager entityManager;



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


        String originName = Thread.currentThread().getName();

        Session session = entityManager.unwrap(Session.class);
        System.out.println(session);




        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter("C://Users/Aiste/Desktop/logOutput.txt", true);
            System.out.println(System.getProperty("user.dir"));
            pw = new PrintWriter(fw);

            pw.println(formatedDate.format(dateNow)+" : Entering Class: " + ctx.getTarget().getClass().getName()+" METHOD: " +
                    ctx.getMethod().getName() + "    USER:: ");

            pw.close();
            fw.close();

        } catch (IOException ex) {
            Logger.getLogger(ctx.getTarget().getClass().getName()).log(Level.SEVERE, null, ex);
        }


        return ctx.proceed();
    }
}
