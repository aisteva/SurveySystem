package interceptor;

import lombok.extern.slf4j.Slf4j;
import userModule.SignInController;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
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
@Slf4j
public class LoggingInterceptor implements Serializable {


    @Inject
    private SignInController signInController;

    private String userEmail;
    private String userType;


    private static final long serialVersionUID = 813L;
    Date dateNow = new Date( );
    SimpleDateFormat formatedDate = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss a");

    @AroundInvoke
    public Object logMethodEntry(InvocationContext ctx) throws Exception {

        if(signInController == null){
            userEmail = "not Signed";;
            userType = "not Signed";
        }
        else{
            userEmail = signInController.getLoggedInPerson().getEmail();
            userType = signInController.getLoggedInPerson().getUserType();
        }

        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            // failas išsisaugo darbo direktorijoje
            // jeigu tokios nežinot savo kompiuteryje, išsiprintinkit šitą: System.getProperty("user.dir");
            fw = new FileWriter("logOutput.txt", true);
            System.out.println();
            pw = new PrintWriter(fw);

            pw.println(formatedDate.format(dateNow)+
                    ", USER: "+ userEmail +
                    ", USER TYPE: "+ userType +
                    ", ENTERING CLASS: " + ctx.getTarget().getClass().getName()+
                    ", METHOD: " + ctx.getMethod().getName()+";");

            pw.close();
            fw.close();
            return ctx.proceed();
        } catch (IOException ex) {
            log.info("Irasymas i faila nepavyko: " + ex.toString());
            return null;
        }



    }
}
