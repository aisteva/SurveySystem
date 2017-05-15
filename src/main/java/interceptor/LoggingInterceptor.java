package interceptor;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
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
//        String currentUser = context.getCallerPrincipal().getName();
        try{
//            String tracingName = currentUser + " " + originName;
        System.out.println(formatedDate.format(dateNow)+" : Entering Class: " + ctx.getTarget().getClass().getName()+" METHOD: " +
                ctx.getMethod().getName() + "    USER:: "+ originName);
            return ctx.proceed();
        }finally{

        }
    }
}
